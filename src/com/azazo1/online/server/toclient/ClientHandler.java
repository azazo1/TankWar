package com.azazo1.online.server.toclient;

import com.azazo1.Config;
import com.azazo1.online.msg.*;
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
    /**
     * 游戏模式是否为玩家, false 则为旁观者
     */
    protected volatile AtomicBoolean isPlayer = null; // 用原子原因: 有 null(未确定) false true 三种状态
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
    
    }
    
    /**
     * 在等待玩家加入时的处理方法
     */
    protected void handleOnWaiting(@NotNull DataTransfer dt, @NotNull MsgBase obj) {
        String tName = obj.getClass().getName();
        if (obj instanceof SyncTimeMsg) { // 同步时间, 不检查其有效期
            Tools.logLn("Got Msg: " + tName);
            dt.sendObject(this, new SyncTimeMsg.SyncTimeResponseMsg());
        } else if (Tools.getRealTimeInMillis() - obj.createdTime > Config.MSG_OUTDATED_TIME) { // 在有效期内
            Tools.logLn("Got Msg: " + tName + ", created on: " + obj.createdTime + " (" + DateFormat.getInstance().format(obj.createdTime) + ")");
            if (obj instanceof FetchSeqMsg) { // 获取 seq
                dt.sendObject(this, new FetchSeqMsg.FetchSeqResponseMsg(seq));
            } else if (obj instanceof RegisterMsg msg) { // 注册
                //todo 注册
                isPlayer = new AtomicBoolean(msg.isPlayer);
                name = msg.name;
                dt.sendObject(this, new RegisterMsg.RegisterResponseMsg(RegisterMsg.RegisterResponseMsg.SUCCEED, msg)); // todo 修改返回值
            } else if (obj instanceof FetchGameIntroMsg) {
                // todo 获取游戏信息
                dt.sendObject(this, new FetchGameIntroMsg.FetchGameIntroResponseMsg("wallmap/WallMap.mwal")); // todo 修改返回值
            }
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
    
    public Socket getSocket() {
        return socket;
    }
    
    public int getSeq() {
        return seq;
    }
    
    public boolean getAlive() {
        return alive.get();
    }
    // todo 创建 clientHandler info
    // todo 给客户端提供 所有客户端信息读取接口 (读取所有clientHandler info)
    // todo 给 Server 提供向客户端 (玩家/旁观者) 发送游戏信息 (Tank/Bullet/Msg) 方法
}
