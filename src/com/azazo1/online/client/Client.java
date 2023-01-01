package com.azazo1.online.client;

import com.azazo1.Config;
import com.azazo1.game.session.ServerGameSessionIntro;
import com.azazo1.online.Communicator;
import com.azazo1.online.msg.*;
import com.azazo1.online.server.toclient.ClientHandler;
import com.azazo1.online.server.toclient.Server;
import com.azazo1.util.Tools;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class Client implements Closeable {
    protected final Socket socket;
    protected final DataTransfer dataTransfer;
    protected final ServerGameSessionIntro intro = new ServerGameSessionIntro();
    private final AtomicBoolean alive = new AtomicBoolean(true);
    private final AtomicInteger seq = new AtomicInteger(-1);
    private final AtomicBoolean _isHost = new AtomicBoolean(false);

    public boolean isHost() {
        return _isHost.get();
    }

    /**
     * 创建 Client 时会自动同步时间
     */
    public Client(String host, int port) throws IOException {
        socket = new Socket(host, port);
        socket.setSoTimeout(Config.CLIENT_SOCKET_TIMEOUT);
        dataTransfer = new DataTransfer(this, new Communicator(socket));
        syncTime();
        fetchSeq();
    }

    /**
     * 处理来自服务端的信息(一个)
     * (可通过返回值自行处理, 故不设回调)
     *
     * @return 被处理的 {@link MsgBase}
     */
    public @Nullable MsgBase handle(boolean wait) {
        Object obj = dataTransfer.readObject(wait);
        if (obj instanceof FetchSeqMsg.FetchSeqResponseMsg msg) {
            seq.set(msg.seq);
        } else if (obj instanceof FetchGameIntroMsg.FetchGameIntroResponseMsg msg) {
            intro.copyFrom(msg.intro);
        } else if (obj instanceof QueryClientsMsg.QueryClientsResponseMsg msg) {
            //  处理所有客户端信息, 注意重点标记显示自身
            ClientHandler.ClientHandlerInfo thisInfo = msg.multiInfo.get(seq.get());
            _isHost.set(thisInfo.isHost);
        } else if (!(obj instanceof MsgBase)) {
            return null;// 此处表示 obj 为 null 或不是 MsgBase
        }
        // 其他情况, 若此句放在 else if 中则会被抢夺而被忽视
        return (MsgBase) obj;
    }

    public @Nullable MsgBase handle() {
        return handle(false);
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
     * 向服务器请求启动本局游戏
     */
    public void reqStartGame() {
        dataTransfer.sendObject(new ReqGameStartMsg());
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
     * 向服务器查询服务器连接下所有客户端信息
     */
    public void queryGameResult() {
        QueryGameResultMsg msg = new QueryGameResultMsg();
        dataTransfer.sendObject(msg);
    }

    /**
     * 向服务器提交编辑本剧游戏配置的请求(仅房主可完成, 仅在{@link Server#WAITING} 时可用)
     * 服务器只会读取intro内的部分内容,不会更改游戏玩家列表
     */
    public void editGameIntro(String wallMap, Rectangle mapSize) {
        ServerGameSessionIntro intro = new ServerGameSessionIntro();
        intro.setWallMapFile(wallMap);
        intro.setMapSize(mapSize);
        dataTransfer.sendObject(new PostGameIntroMsg(intro));
    }

    /**
     * 向服务端请求更改坦克动作状态
     */
    public void changePressedKey(boolean left, boolean right, boolean forward, boolean backward) {
        dataTransfer.sendObject(new KeyPressChangeMsg(left, right, forward, backward));
    }

    /**
     * 向服务器请求开火
     */
    public void fire() {
        dataTransfer.sendObject(new TankFireActionMsg());
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
}
