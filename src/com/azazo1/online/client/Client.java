package com.azazo1.online.client;

import com.azazo1.Config;
import com.azazo1.online.Communicator;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

public class Client implements Closeable {
    protected Socket socket;
    protected Communicator communicator;
    
    public Client(String host, int port) throws IOException {
        socket = new Socket(host, port);
        socket.setSoTimeout(Config.CLIENT_SOCKET_TIMEOUT);
        communicator = new Communicator(socket);
    }
    
    public void syncTime() {
    }
    
    @Override
    public void close() {
        try {
            socket.close();
        } catch (IOException ignore) {
        }
    }

// todo 同步时间
// todo 注册玩家 (发送玩家昵称, 接收玩家 seq)
// todo 注册观战
// todo Config 信息读取
// todo 墙图文件名读取
// todo 所有玩家信息读取
// todo 接收游戏信息 (Tank/Bullet/Msg) 方法
}
