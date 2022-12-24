package com.azazo1.online.server.toclient;


import com.azazo1.Config;
import com.azazo1.base.SingleInstance;
import com.azazo1.game.GameMap;
import com.azazo1.game.session.ServerGameSessionIntro;
import com.azazo1.online.msg.*;
import com.azazo1.online.server.GameState;
import com.azazo1.online.server.ServerGameMap;
import com.azazo1.online.server.bullet.ServerBulletGroup;
import com.azazo1.online.server.tank.ServerTank;
import com.azazo1.online.server.tank.ServerTankGroup;
import com.azazo1.online.server.wall.ServerWallGroup;
import com.azazo1.util.Tools;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azazo1.online.msg.RegisterMsg.RegisterResponseMsg.*;

/**
 * 服务器对象的所有操作都在子线程异步进行
 */
public class Server implements Closeable, SingleInstance {
    /**
     * 服务器状态: 等待客户端加入, 等待游戏开始
     */
    public static final String WAITING = "w";
    /**
     * 服务器状态: 游戏进行时
     */
    public static final String GAMING = "g";
    /**
     * 服务器状态：游戏结束（只供游戏结果查询）
     */
    public static final String OVER = "o";
    /**
     * 服务器实例
     */
    public static Server instance;
    /**
     * 和客户端套接字进行数据交换的窗口
     */
    protected final DataTransfer dataTransfer = new DataTransfer();
    /**
     * 服务器套接字, 绑定了某一端口
     */
    protected final ServerSocket socket;
    /**
     * 储存客户端套接字
     */
    protected final Vector<ClientHandler> clients = new Vector<>();
    private final AtomicBoolean alive = new AtomicBoolean(true);
    /**
     * 游戏配置
     */
    protected volatile ServerGameSessionIntro intro;
    /**
     * 房主, 可以:
     * <ol>
     *     <li>控制游戏开始</li>
     *     <li>todo 踢出玩家</li>
     *     <li>选择游戏墙图</li>
     * </ol>
     */
    protected volatile ClientHandler host = null;
    /**
     * 客户端接收器, WAITING 时接收玩家和旁观者, GAMING 时接收旁观者
     */
    protected Timer acceptor;
    /**
     * 客户端信息处理器, 用于和客户端进行交互
     */
    protected Timer handler;
    /**
     * 待执行，在 handler 循环中被执行
     */
    protected Vector<Runnable> handleList = new Vector<>();
    /**
     * 当前服务器状态
     */
    protected volatile String currentState = WAITING;

    protected ServerGameMap gameMap;
    /**
     * 游戏结果，在游戏结束前都是null
     */
    protected GameMap.GameInfo gameResult;

    /**
     * @param port 端口号, 为 0 则自动分配
     */
    public Server(int port) throws IOException {
        checkInstance();
        instance = this;
        socket = new ServerSocket(port) {{
            setSoTimeout(Config.SERVER_SOCKET_TIMEOUT);
        }};
        initHandler();
        Tools.logLn("Server Opened at Port: " + socket.getLocalPort());
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server(60000);
        server.changeToWaitingState();
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String get = scanner.nextLine();
            if (get.equalsIgnoreCase("q") || get.equals("quit")) {
                server.close();
                break;
            }
        }
    }

    /**
     * 设置房主客户端
     */
    public void setHost(@Nullable ClientHandler host) {
        if (this.host != null) {
            this.host.setIsHost(false); // 取消之前的房主
        }
        this.host = host;
        if (host != null) {
            host.setIsHost(true);
            Tools.logLn("Host changed to: " + host.getInfo());
        } else {
            Tools.logLn("Host lost.");
        }
    }

    /**
     * 初始化客户端信息处理器, 处理周期要略短于游戏事件周期
     *
     * @apiNote 此方法只能调用一次
     */
    protected void initHandler() {
        if (handler != null) {
            handler.cancel();
        }
        handler = new Timer("handler", true);
        // 不用 schedule, 防止处理过慢
        handler.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (alive.get()) {
                    // 执行 handle list, 一下执行空, 防止请求量多于循环速度导致堵塞
                    while (!handleList.isEmpty()) {
                        handleList.remove(0).run();
                    }
                    // 检查客户端状态
                    for (int i = 0; i < clients.size(); i++) {
                        ClientHandler client = clients.get(i);
                        if (!client.handle()) {
                            removeClient(i);
                            i--; // 该元素被移除了后来的元素顶替该位置
                            if (clients.size() == 0 && currentState.equals(OVER)) { // 游戏结束，所有用户退出，服务段生命走到尽头
                                close();
                            }
                        }
                    }
                    if (currentState.equals(GAMING)) {
                        gameMap.update(null);
                        Tools.tickFrame();
                        broadcast(new GameStateMsg(new GameState(
                                Tools.getFrameTimeInMillis(),
                                gameMap.getTankGroup().getTanksInfo(),
                                gameMap.getBulletGroup().getBulletInfo()
                        )), false);
                        if (gameMap.getTankGroup().getLivingTankNum() <= 1) { // 游戏结束
                            Tools.logLn("Game Over.");
                            gameResult = gameMap.getInfo();
                            broadcast(new GameOverMsg(gameResult), false);
                            changeToOverState();
                        }
                    }
                }
            }
        }, 0, (long) (650.0 / Config.FPS)); // 要以略快于游戏事件循环的速度进行
    }


    private void removeClient(int i) {
        ClientHandler client = clients.get(i);
        client.close();
        clients.remove(i); // todo 检查移除是否对游戏产生影响
        Tools.logLn("Client: \"" + client.getSeq() + "\" was removed.");
        if (client.isHost()) { // 重新设置房主, 如果 clients 为空, 则变为 null, 会在新客户端加入时重新设置
            if (clients.isEmpty()) {
                setHost(null);
            } else {
                setHost(clients.get(0));
            }
        }
        if (client.isPlayer() != null && client.isPlayer().get()) {
            unregisterPlayer(client.getSeq());
        }
    }

    protected void changeToOverState() {
        currentState = OVER;
    }

    protected void changeToWaitingState() {
        resetClients();
        initAcceptor();
        initGameConfig();
        currentState = WAITING;
    }

    protected void changeToGamingState() throws IOException {
        acceptor.cancel();
        initGameContent();
        currentState = GAMING;
    }

    protected void initGameContent() throws IOException {
        gameMap = new ServerGameMap();
        gameMap.setSize(Config.MAP_WIDTH, Config.MAP_HEIGHT);
        ServerTankGroup serverTankGroup = new ServerTankGroup();
        gameMap.setTankGroup(serverTankGroup);
        gameMap.setBulletGroup(new ServerBulletGroup());
        gameMap.setWallGroup(ServerWallGroup.parseFromBitmap(ImageIO.read(Tools.getFileURL(intro.getWallMapFile()).url())));
        HashMap<Integer, String> tanks = intro.getTanks();
        tanks.forEach((seq, name) -> {
            ServerTank tank = new ServerTank(seq);
            tank.setName(name);
            serverTankGroup.addTank(tank);
            tank.randomlyTeleport();
        });
    }

    /**
     * 重置客户端套接字组
     */
    protected void resetClients() {
        for (ClientHandler client : clients) {
            client.close();
        }
        clients.clear();
    }

    /**
     * 初始化客户端接收器
     */
    protected void initAcceptor() {
        if (acceptor != null) {
            acceptor.cancel();
        }
        acceptor = new Timer("acceptor", true);
        acceptor.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    acceptClient(socket.accept());
                } catch (SocketTimeoutException ignore) {
                } catch (IOException e) {
                    close();
                }
            }
        }, 0, 100); // socket.accept() 已有阻塞
    }

    /**
     * 接收客户端套接字
     */
    protected void acceptClient(Socket client) throws IOException {
        ClientHandler cHandler = new ClientHandler(this, client);
        if (dataTransfer.addClient(cHandler)) {
            Tools.logLn("Get client: " + cHandler.getInfo());
            if (clients.isEmpty()) {
                setHost(cHandler); // 设定为房主
            }
            clients.add(cHandler);
        } else {
            cHandler.close();
        }
    }

    /**
     * 注册玩家
     *
     * @return 状态码 见: {@link RegisterMsg.RegisterResponseMsg} 的常量
     */
    @MagicConstant(intValues = {SUCCEED, NAME_OR_SEQ_OCCUPIED, PLAYER_MAXIMUM})
    public int registerPlayer(int seq, String name) {
        try {
            intro.addTank(seq, name);
            return SUCCEED;
        } catch (IllegalArgumentException e) {
            return NAME_OR_SEQ_OCCUPIED;
        } catch (IllegalStateException e) {
            return PLAYER_MAXIMUM;
        }
    }

    /**
     * 取消注册玩家
     */
    public void unregisterPlayer(int seq) {
        intro.removeTank(seq);
    }

    /**
     * 从 {@link #intro} 获得单局游戏配置备份
     */
    public ServerGameSessionIntro getGameSessionIntro() {
        return new ServerGameSessionIntro(intro);
    }

    /**
     * 房主设置单局游戏配置, (tanks 无法更改)
     */
    public void modifyGameSessionIntro(@NotNull ServerGameSessionIntro intro) {
        this.intro.setWallMapFile(intro.getWallMapFile());
        // 以后可能还有其他配置
    }

    /**
     * 结束 Server 的使用
     * <p>{@inheritDoc}
     */
    @Override
    public void close() {
        handler.cancel();
        acceptor.cancel();
        dataTransfer.close();
        try {
            socket.close();
        } catch (IOException ignore) {
        }
        alive.set(false);
    }

    public DataTransfer getDataTransfer() {
        return dataTransfer;
    }

    public String getState() {
        return currentState;
    }

    public GameMap.GameInfo getGameResult() {
        return gameResult;
    }

    public HashMap<Integer, ClientHandler.ClientHandlerInfo> getClientsInfo() {
        HashMap<Integer, ClientHandler.ClientHandlerInfo> rst = new HashMap<>();
        for (ClientHandler c : clients) {
            rst.put(c.getSeq(), c.getInfo());
        }
        return rst;
    }

    @Override
    public void checkInstance() {
        if (instance != null) {
            throw new IllegalStateException("Server can be created only once");
        }
    }

    @Override
    public boolean hasInstance() {
        return instance != null;
    }

    /**
     * 初始化游戏配置
     */
    protected void initGameConfig() {
        intro = new ServerGameSessionIntro();
        intro.setWallMapFile("wallmap/WallMap.mwal");
    }

    /**
     * 开始游戏
     */
    public boolean startGame() {
        try {
            changeToGamingState();
            broadcast(new GameStartMsg(), false);
            Tools.logLn("Game Start.");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 广播到所有客户端
     *
     * @param onlyPlayer 是否只发送到玩家客户端上
     */
    public void broadcast(MsgBase msg, boolean onlyPlayer) {
        for (ClientHandler c : clients) {
            if ((!onlyPlayer || c.isPlayer().get()) && c.isAlive()) {
                dataTransfer.sendObject(c, msg);
            }
        }
    }

    public ServerGameMap getGameMap() {
        return gameMap;
    }

    /**
     * 解决多线程下方法调用后结果在内存不同步的问题，
     */
    public void letMeHandle(Runnable runnable) {
        handleList.add(runnable);
    }
}
