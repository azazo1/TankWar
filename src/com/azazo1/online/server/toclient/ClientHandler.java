package com.azazo1.online.server.toclient;

import com.azazo1.Config;
import com.azazo1.online.msg.MsgBase;
import com.azazo1.online.msg.SyncTimeMsg;
import com.azazo1.util.SeqModule;
import com.azazo1.util.Tools;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.text.DateFormat;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientHandler implements Closeable {
    private static final SeqModule seqModule = new SeqModule();
    protected final int seq;
    /**
     * 客户端是否仍有效, 状态由 {@link DataTransfer} 修改
     */
    protected final AtomicBoolean alive = new AtomicBoolean(true);
    protected final Socket socket;
    protected final Server server;
    
    public ClientHandler(Server server, Socket client) throws SocketException {
        this.server = server;
        socket = client;
        socket.setSoTimeout(Config.CLIENT_HANDLER_SOCKET_TIMEOUT);
        seqModule.use(seq = seqModule.next()); // 先赋值后标记使用
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
    
    }
    
    /**
     * 在等待玩家加入时的处理方法
     */
    protected void handleOnWaiting(@NotNull DataTransfer dt, @NotNull MsgBase obj) {
        String tName = obj.getClass().getName();
        Tools.logLn("Got Msg: " + tName + ", created on: " + obj.createdTime + " (" + DateFormat.getInstance().format(obj.createdTime) + ")");
        if (tName.equals(SyncTimeMsg.class.getName())) {
            dt.sendObject(this, new SyncTimeMsg.SyncTimeResponseMsg());
        } else if (true /*todo*/) {
        }
    }
    
    public InetAddress getAddress() {
        return socket.getInetAddress();
    }
    
    public Socket getSocket() {
        return socket;
    }
    
    public int getSeq() {
        return seq;
    }
    // 给客户端提供 同步时间接口
    // todo 给客户端提供 注册玩家接口 (接收玩家昵称, 返回玩家 seq)
    // todo 给客户端提供 注册观战接口
    // todo 给客户端提供 Config 信息读取接口 (同时启动游戏 (GAMING) 时再次发送)
    // todo 给客户端提供 墙图文件名读取接口 (同时启动游戏 (GAMING) 时再次发送)
    // todo 给客户端提供 所有玩家信息读取接口
    // todo 给 Server 提供向客户端 (玩家/旁观者) 发送游戏信息 (Tank/Bullet/Msg) 方法
}
