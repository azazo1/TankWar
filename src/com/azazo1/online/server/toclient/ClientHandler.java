package com.azazo1.online.server.toclient;

import com.azazo1.Config;
import com.azazo1.online.msg.*;
import com.azazo1.online.server.bullet.ServerBullet;
import com.azazo1.online.server.tank.ServerTank;
import com.azazo1.util.SeqModule;
import com.azazo1.util.Tools;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.text.DateFormat;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azazo1.online.msg.RegisterMsg.RegisterResponseMsg.SUCCEED;

public class ClientHandler implements Closeable {
    private static final SeqModule seqModule = new SeqModule();
    protected final int seq;
    /**
     * 客户端是否仍有效, 状态由 {@link DataTransfer} 修改
     */
    protected final AtomicBoolean alive = new AtomicBoolean(true);
    protected final Socket socket;
    protected final Server server;
    /**
     * 是否为房主
     */
    protected final AtomicBoolean _isHost = new AtomicBoolean(false);
    /**
     * 游戏模式是否为玩家, false 则为旁观者
     */
    protected volatile AtomicBoolean _isPlayer = null; // 用原子 null 原因: 有 null(未确定) false true 三种状态
    /**
     * 客户端昵称
     */
    protected volatile String name = null;

    public ClientHandler(Server server, Socket client) throws SocketException {
        this.server = server;
        socket = client;
        socket.setSoTimeout(Config.CLIENT_HANDLER_SOCKET_TIMEOUT);
        seqModule.use(seq = seqModule.next()); // 先赋值后标记使用
    }

    public void setIsHost(boolean host) {
        _isHost.set(host);
    }

    @Override
    public void close() {
        try {
            socket.close();
        } catch (IOException ignore) {
        }
        // seqModule.dispose(seq); 考虑到旁观者新加入, 不取消序号的占用
        alive.set(false);
    }

    /**
     * 处理客户端信息
     *
     * @return 客户端是否仍有效
     */
    public boolean handle() {
        if (!alive.get()) {
            return false;
        }
        DataTransfer dt = server.getDataTransfer();
        Object obj = dt.readObject(this);
        if (obj instanceof MsgBase msg) { // 同时检查了 !=null
            switch (server.getState()) {
                case Server.WAITING -> handleOnWaiting(dt, msg);
                case Server.GAMING -> handleOnGaming(dt, msg);
            }
        }
        return alive.get();
    }

    /**
     * 在游戏进行时的处理方法
     */
    protected void handleOnGaming(@NotNull DataTransfer dt, @NotNull MsgBase obj) {
        String shortClassName = obj.getShortTypeName();
        MsgBase toBeSent = null;
        if (Tools.getRealTimeInMillis() - obj.createdTime <= Config.MSG_OUTDATED_TIME) { // 在有效期内
            Tools.logLn("Got Msg(From " + seq + "): " + shortClassName + ", created on: " + obj.createdTime + " (" + DateFormat.getInstance().format(obj.createdTime) + ")");
            if (obj instanceof TankFireActionMsg) {
                server.letMeHandle(() -> server.getGameMap().getTankGroup().getTank(seq).fireModule.fire(ServerBullet.class));
            } else if (obj instanceof KeyPressChangeMsg msg) {
                server.letMeHandle(() -> {
                    ServerTank tank = (ServerTank) server.gameMap.getTankGroup().getTank(seq);
                    tank.keyChange(msg.leftTurningKeyPressed, msg.rightTurningKeyPressed, msg.forwardGoingKeyPressed, msg.backwardGoingKeyPressed);
                });
            }
        }
        if (toBeSent != null) {
            dt.sendObject(this, toBeSent);
            Tools.logLn("Sent Msg(To " + seq + "): " + toBeSent.getShortTypeName() + ", created on: " + toBeSent.createdTime + " (" + DateFormat.getInstance().format(toBeSent.createdTime) + ")");
        }
    }

    /**
     * 在等待玩家加入时的处理方法
     */
    protected void handleOnWaiting(@NotNull DataTransfer dt, @NotNull MsgBase obj) {
        String shortClassName = obj.getShortTypeName();
        MsgBase toBeSent = null;
        if (obj instanceof SyncTimeMsg) { // 同步时间, 不检查其有效期
            Tools.logLn("Got Msg(From " + seq + "): " + shortClassName);
            toBeSent = new SyncTimeMsg.SyncTimeResponseMsg();
        } else if (Tools.getRealTimeInMillis() - obj.createdTime <= Config.MSG_OUTDATED_TIME) { // 在有效期内
            Tools.logLn("Got Msg(From " + seq + "): " + shortClassName + ", created on: " + obj.createdTime + " (" + DateFormat.getInstance().format(obj.createdTime) + ")");
            if (obj instanceof FetchSeqMsg) { // 获取 seq
                toBeSent = new FetchSeqMsg.FetchSeqResponseMsg(seq);
            } else if (obj instanceof RegisterMsg msg) { // 注册
                int rst;
                if (msg.isPlayer) {
                    rst = server.registerPlayer(seq, msg.name); // 注册玩家模式
                } else {
                    rst = SUCCEED; // 申请旁观模式成功
                }
                if (rst == SUCCEED) { // 成功则记录信息
                    _isPlayer = new AtomicBoolean(msg.isPlayer);
                    name = msg.name;
                }
                toBeSent = new RegisterMsg.RegisterResponseMsg(rst, msg);
            } else if (obj instanceof FetchGameIntroMsg) {
                toBeSent = new FetchGameIntroMsg.FetchGameIntroResponseMsg(server.getGameSessionIntro());
            } else if (obj instanceof QueryClientsMsg) {
                toBeSent = new QueryClientsMsg.QueryClientsResponseMsg(server.getClientsInfo());
            } else if (obj instanceof PostGameIntroMsg msg) {
                if (isHost()) {
                    server.modifyGameSessionIntro(msg.intro);
                }
            } else if (obj instanceof ReqGameStartMsg) {
                if (isHost()) {
                    server.letMeHandle(server::startGame);
                    toBeSent = new ReqGameStartMsg.ReqGameStartMsgResponseMsg(true);
                } else {
                    toBeSent = new ReqGameStartMsg.ReqGameStartMsgResponseMsg(false);
                }
            }
            // todo 提供 WallMap 选择接口 (仅房主)
            // todo 给 Server 提供向客户端 (玩家/旁观者) 发送游戏信息 (Tank/Bullet/Msg) 方法
        }
        if (toBeSent != null) {
            dt.sendObject(this, toBeSent);
            Tools.logLn("Sent Msg(To " + seq + "): " + toBeSent.getShortTypeName() + ", created on: " + toBeSent.createdTime + " (" + DateFormat.getInstance().format(toBeSent.createdTime) + ")");
        }
    }

    /**
     * 获取客户端套接字的远程地址
     */
    public InetAddress getAddress() {
        return socket.getInetAddress();
    }

    /**
     * 获取客户端套接字的远程端口
     */
    public int getPort() {
        return socket.getPort();
    }

    /**
     * 获得此对象的基本信息
     */
    public ClientHandlerInfo getInfo() {
        return new ClientHandlerInfo(this);
    }

    public Socket getSocket() {
        return socket;
    }

    public int getSeq() {
        return seq;
    }

    public boolean getAlive() {
        return alive.get();
    }

    public boolean isHost() {
        return _isHost.get();
    }

    public AtomicBoolean isPlayer() {
        return _isPlayer;
    }

    public boolean isAlive() {
        return alive.get();
    }

    public String getName() {
        return name;
    }

    /**
     * {@link ClientHandler} 对象的基本信息
     */
    public static class ClientHandlerInfo implements Serializable {
        public final String name;
        public final int seq;
        public final boolean isHost;
        public final InetAddress address;
        public final int port;
        public final AtomicBoolean isPlayer;

        public ClientHandlerInfo(@NotNull ClientHandler c) {
            this.isHost = c.isHost();
            this.isPlayer = c._isPlayer;
            this.name = c.name;
            this.address = c.getAddress();
            this.port = c.getPort();
            this.seq = c.seq;
        }

        @Override
        public String toString() {
            return "ClientHandlerInfo{" + "name='" + name + '\'' + ", seq=" + seq + ", isHost=" + isHost + ", address=" + address + ", port=" + port + ", isPlayer=" + isPlayer + '}';
        }
    }
}
