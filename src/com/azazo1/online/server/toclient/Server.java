package com.azazo1.online.server.toclient;


import com.azazo1.Config;
import com.azazo1.util.Tools;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class Server {
    /**
     * 服务器状态: 等待客户端加入, 等待游戏开始
     */
    public static final String WAITING = "w";
    /**
     * 服务器状态: 游戏进行时
     */
    public static final String GAMING = "g";
    /**
     * 当前服务器状态
     */
    protected volatile String currentState = WAITING;
    /**
     * 客户端接收器
     */
    protected Timer acceptor = new Timer("acceptor", true);
    /**
     * 和客户端套接字进行数据交换的窗口
     */
    protected DataTransfer dataTransfer = new DataTransfer();
    /**
     * 客户端信息处理器, 用于和客户端进行交互
     */
    protected Timer handler = new Timer("handler", true);
    /**
     * 服务器套接字, 绑定了某一端口
     */
    protected ServerSocket socket = new ServerSocket(0) {{
        setSoTimeout(Config.SERVER_SOCKET_TIMEOUT);
    }};
    /**
     * 储存客户端套接字
     */
    protected Vector<ClientHandler> clients = new Vector<>();
    
    public Server() throws IOException {
        initHandler();
    }
    
    public static void main(String[] args) {
        // new Server().start();
    }
    
    /**
     * 初始化客户端信息处理器, 处理周期要略短于游戏事件周期
     */
    protected void initHandler() {
        handler.cancel();
        handler.schedule(new TimerTask() {
            @Override
            public void run() {
                for (int i = 0; i < clients.size(); i++) {
                    ClientHandler client = clients.get(i);
                    if (!client.handle()) {
                        clients.remove(i);
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
        acceptor.cancel();
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
        }, 0, 100);
    }
    
    /**
     * 接收客户端套接字
     */
    protected void acceptClient(Socket client) throws IOException {
        ClientHandler cHandler = new ClientHandler(this, client);
        if (dataTransfer.addClient(cHandler)) {
            clients.add(cHandler);
        }else {
            cHandler.close();
        }
        Tools.logLn("Get client: " + cHandler.getAddress());
    }
    
    /**
     * 结束 Server 的使用
     */
    public void close() {
        acceptor.cancel();
        try {
            socket.close();
        } catch (IOException ignore) {
        }
    }
    
    public DataTransfer getDataTransfer() {
        return dataTransfer;
    }
}
