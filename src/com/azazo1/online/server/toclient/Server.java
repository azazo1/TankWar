package com.azazo1.online.server.toclient;


import com.azazo1.Config;
import com.azazo1.util.Tools;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

/**
 * 服务器对象的所有操作都在子线程异步进行
 */
public class Server implements Closeable {
    /**
     * 服务器状态: 等待客户端加入, 等待游戏开始
     */
    public static final String WAITING = "w";
    /**
     * 服务器状态: 游戏进行时
     */
    public static final String GAMING = "g";
    /**
     * 和客户端套接字进行数据交换的窗口
     */
    protected final DataTransfer dataTransfer = new DataTransfer();
    /**
     * 服务器套接字, 绑定了某一端口
     */
    protected final ServerSocket socket = new ServerSocket(0) {{
        setSoTimeout(Config.SERVER_SOCKET_TIMEOUT);
    }};
    /**
     * 储存客户端套接字
     */
    protected final Vector<ClientHandler> clients = new Vector<>();
    /**
     * 客户端接收器, WAITING 时接收玩家和旁观者, GAMING 时接收旁观者
     */
    protected Timer acceptor;
    /**
     * 客户端信息处理器, 用于和客户端进行交互
     */
    protected Timer handler;
    /**
     * 当前服务器状态
     */
    protected volatile String currentState = WAITING;
    
    public Server() throws IOException {
        initHandler();
        Tools.logLn("Server Opened at Port: " + socket.getLocalPort());
    }
    
    public static void main(String[] args) throws IOException {
        Server server = new Server();
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
                for (int i = 0; i < clients.size(); i++) {
                    ClientHandler client = clients.get(i);
                    if (!client.handle()) {
                        client.close();
                        clients.remove(i); // todo 检查移除是否对游戏产生影响
                        Tools.logLn("Client: \"" + client.getSeq() + "\" was removed.");
                        i--;
                    }
                }
            }
        }, 0, (long) (650.0 / Config.FPS)); // 要以略快于游戏事件循环的速度进行
    }
    
    protected void changeToWaitingState() {
        resetClients();
        initAcceptor();
        currentState = WAITING;
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
            clients.add(cHandler);
            Tools.logLn("Get client: " + cHandler.getAddress());
        } else {
            cHandler.close();
        }
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
    }
    
    public DataTransfer getDataTransfer() {
        return dataTransfer;
    }
    
    public String getState() {
        return currentState;
    }
    
    
}
