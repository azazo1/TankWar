package com.azazo1.online.client;

import com.azazo1.Config;
import com.azazo1.game.session.ServerGameSessionIntro;
import com.azazo1.online.Communicator;
import com.azazo1.online.msg.*;
import com.azazo1.online.server.toclient.ClientHandler;
import com.azazo1.online.server.toclient.Server;
import com.azazo1.util.Tools;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azazo1.online.msg.RegisterMsg.RegisterResponseMsg.*;


public class Client implements Closeable {
    protected final Socket socket;
    protected final DataTransfer dataTransfer;
    protected final ServerGameSessionIntro intro = new ServerGameSessionIntro();
    private final AtomicBoolean alive = new AtomicBoolean(true);
    private final AtomicInteger seq = new AtomicInteger(-1);
    private final AtomicBoolean _isHost = new AtomicBoolean(false);
    
    public Client(String host, int port) throws IOException {
        socket = new Socket(host, port);
        socket.setSoTimeout(Config.CLIENT_SOCKET_TIMEOUT);
        dataTransfer = new DataTransfer(this, new Communicator(socket));
        syncTime();
        fetchSeq();
    }
    
    /**
     * 处理来自服务端的信息
     *
     * @return 被处理的 {@link MsgBase}
     */
    public @Nullable MsgBase handle() {
        Object obj = dataTransfer.readObject(false);
        if (obj instanceof FetchSeqMsg.FetchSeqResponseMsg msg) {
            seq.set(msg.seq);
        } else if (obj instanceof RegisterMsg.RegisterResponseMsg msg) {
            System.out.println(msg.code);
            switch (msg.code) {
                case SUCCEED -> {/*todo 提醒用户*/}
                case PLAYER_MAXIMUM -> {/*todo 提醒用户更换游戏模式*/}
                case NAME_OR_SEQ_OCCUPIED -> {/*todo 提醒用户改名*/}
            }
        } else if (obj instanceof FetchGameIntroMsg.FetchGameIntroResponseMsg msg) {
            intro.copyFrom(msg.intro);
        } else if (obj instanceof QueryClientsMsg.QueryClientsResponseMsg msg) {
            // todo 处理所有客户端信息, 注意重点标记显示自身
            ClientHandler.ClientHandlerInfo thisInfo = msg.multiInfo.get(seq.get());
            _isHost.set(thisInfo.isHost);
            System.out.println(msg.multiInfo);
        } else { // 此处表示 obj 为 null 或不是可被客户端处理的 Msg
            return null;
        }
        return (MsgBase) obj;
    }
    
    /**
     * 向服务端同步时间
     */
    public void syncTime() {
        SyncTimeMsg localMsg = new SyncTimeMsg();
        dataTransfer.sendObject(localMsg);
        long startTime = localMsg.createdTime;
        Object obj = dataTransfer.readObject(true); // 由于用到时间差, syncTime 不在 handle 中获取返回值
        long endTime = Tools.getRealTimeInMillis();
        if (obj instanceof SyncTimeMsg.SyncTimeResponseMsg msg) {
            long bias = msg.createdTime - (endTime - startTime) / 2 - localMsg.createdTime; // 获得时间偏差
            Tools.setTimeBias(bias);
        }
    }
    
    /**
     * 向服务器请求客户端序号
     */
    public void fetchSeq() {
        FetchSeqMsg msg = new FetchSeqMsg();
        dataTransfer.sendObject(msg);
    }
    
    /**
     * 向服务器注册
     */
    public void register(boolean isPlayer, String name) {
        RegisterMsg msg = new RegisterMsg(isPlayer, name);
        dataTransfer.sendObject(msg);
    }
    
    /**
     * 向服务器请求游戏基本信息
     */
    public void fetchGameIntro() {
        FetchGameIntroMsg msg = new FetchGameIntroMsg();
        dataTransfer.sendObject(msg);
    }
    
    /**
     * 向服务器查询服务器连接下所有客户端信息
     */
    public void queryClients() {
        QueryClientsMsg msg = new QueryClientsMsg();
        dataTransfer.sendObject(msg);
    }
    
    /**
     * 向服务器请求修改本局游戏配置 ({@link Server#WAITING} 时, 仅房主可用)
     */
    public void postGameIntro() {
        if (_isHost.get()) {
            PostGameIntroMsg msg = new PostGameIntroMsg(intro);
        } else {
            // todo 提示用户
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        try {
            socket.close();
        } catch (IOException ignore) {
        }
        dataTransfer.close();
        alive.set(false);
    }
    
    public boolean getAlive() {
        return alive.get();
    }
    
    /**
     * 获取客户端序号, 在 {@link #fetchSeq()} 返回值到来前调用将会得到错误的值 (-1)
     */
    public int getSeq() {
        return seq.get();
    }
// todo 接收游戏信息 (Tank/Bullet/Msg) 方法
}
