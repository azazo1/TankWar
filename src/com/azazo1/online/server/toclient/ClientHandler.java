package com.azazo1.online.server.toclient;

import com.azazo1.Config;
import com.azazo1.util.SeqModule;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientHandler {
    private static final SeqModule seqModule = new SeqModule();
    protected final int seq;
    /**
     * 客户端是否仍有效, 状态由 {@link DataTransfer} 修改
     */
    protected final AtomicBoolean alive = new AtomicBoolean(true);
    protected Socket socket;
    protected Server server;
    
    public ClientHandler(Server server, Socket client) throws SocketException {
        this.server = server;
        socket = client;
        socket.setSoTimeout(Config.CLIENT_HANDLER_SOCKET_TIMEOUT);
        seqModule.use(seq = seqModule.next()); // 先赋值后标记使用
    }
    
    public void close() {
        try {
            socket.close();
        } catch (IOException ignore) {
        }
        alive.set(false);
    }
    
    /**
     * 处理客户端信息
     *
     * @return 客户端是否仍有效
     */
    public boolean handle() {
        DataTransfer dt = server.getDataTransfer();
        // todo
        return alive.get();
    }
    
    public InetAddress getAddress() {
        return socket.getInetAddress();
    }
    
    public Socket getSocket() {
        return socket;
    }
    // todo 给客户端提供 同步时间接口
    // todo 给客户端提供 注册玩家接口 (接收玩家昵称, 返回玩家 seq)
    // todo 给客户端提供 注册观战接口
    // todo 给客户端提供 Config 信息读取接口
    // todo 给客户端提供 墙图文件名读取接口
    // todo 给客户端提供 所有玩家信息读取接口
    // todo 给 Server 提供向客户端 (玩家/旁观者) 发送游戏信息 (Tank/Bullet/Msg) 方法
}
