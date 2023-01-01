package com.azazo1.online.client;

import com.azazo1.Config;
import com.azazo1.online.Communicator;
import com.azazo1.online.msg.GameStateMsg;
import com.azazo1.online.msg.MsgBase;
import com.azazo1.util.Tools;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.net.SocketTimeoutException;
import java.text.DateFormat;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 异步传输数据
 */
public class DataTransfer implements Closeable {
    protected final Communicator communicator;
    /**
     * 接收到但是未被提取的对象
     */
    protected final LinkedTransferQueue<Serializable> received = new LinkedTransferQueue<>();
    /**
     * 等待被发送的对象
     */
    protected final LinkedTransferQueue<Serializable> toBeSent = new LinkedTransferQueue<>();
    /**
     * 对客户端的引用
     */
    protected final Client client;
    private final AtomicBoolean alive = new AtomicBoolean(true);

    public DataTransfer(@NotNull Client client, @NotNull Communicator communicator) {
        this.client = client;
        this.communicator = communicator;
        readingThread.start();
        sendingThread.start();
    }

    /**
     * 将可序列化对象添加到待发送列表
     *
     * @param obj 将要被发送的对象
     */
    public void sendObject(@NotNull Serializable obj) {
        if (!alive.get()) {
            throw new IllegalStateException("DataTransfer has closed.");
        }
        toBeSent.add(obj);
        if (obj instanceof MsgBase msg) {
            Tools.logLn("Sent Msg: " + msg.getShortTypeName() + ", created on: " + msg.createdTime + " (" + DateFormat.getInstance().format(msg.createdTime) + ")");
        }
    }

    /**
     * 提取待提取列表中的已读信息
     *
     * @param wait 是否等待
     * @return 若待提取列表为空则返回 null
     */
    public @Nullable Serializable readObject(boolean wait) {
        if (!alive.get()) {
            throw new IllegalStateException("DataTransfer has closed.");
        }
        if (wait) {
            while (received.isEmpty()) { // 有可能会永远卡在这
                try {
                    //noinspection BusyWait
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    close();
                    client.close();
                    throw new RuntimeException(e);
                }
            }
            Serializable poll = received.poll();
            if (poll instanceof MsgBase msg) {
                if (poll instanceof GameStateMsg) { // 该消息压缩显示
                    Tools.log("Got Msg: " + msg.getShortTypeName() + ", created on: " + msg.createdTime + " (" + DateFormat.getInstance().format(msg.createdTime) + ")\r");
                } else {
                    Tools.logLn("Got Msg: " + msg.getShortTypeName() + ", created on: " + msg.createdTime + " (" + DateFormat.getInstance().format(msg.createdTime) + ")");
                }
            }
            return poll; // 不会再空了
        } else {
            try {
                Serializable poll = received.poll(Config.CLIENT_SOCKET_TIMEOUT, TimeUnit.MILLISECONDS);
                if (poll instanceof MsgBase msg) {
                    if (poll instanceof GameStateMsg) { // 该消息压缩显示
                        Tools.log("Got Msg: " + msg.getShortTypeName() + ", created on: " + msg.createdTime + " (" + DateFormat.getInstance().format(msg.createdTime) + ")\r");
                    } else {
                        Tools.logLn("Got Msg: " + msg.getShortTypeName() + ", created on: " + msg.createdTime + " (" + DateFormat.getInstance().format(msg.createdTime) + ")");
                    }
                }
                return poll;
            } catch (InterruptedException e) {
                return null;
            }
        }
    }

    /**
     * 结束使用
     */
    @Override
    public void close() {
        communicator.close();
        received.clear();
        toBeSent.clear();
        readingThread.interrupt();
        sendingThread.interrupt();
        alive.set(false);
    }

    /**
     * 持续接收对象
     */
    protected final Thread readingThread = new Thread() {
        {
            setDaemon(true);
            setName("reading");
        }

        @Override
        public void run() {
            while (!this.isInterrupted()) {
                try {
                    Serializable get = communicator.readObject(); // 每个连接每次只读取一个对象
                    if (get != null) {
                        received.add(get);
                    }
                } catch (NullPointerException e) {// 连接断开
                    if (client.getAlive()) {
                        e.printStackTrace();
                    }
                    close();
                    client.close();
                    break;
                } catch (SocketTimeoutException ignore) {
                } catch (IOException e) {
                    if (client.getAlive()) {
                        e.printStackTrace();
                    }
                    close();
                    client.close();
                    break;
                }
                try {
                    //noinspection BusyWait
                    Thread.sleep(1); // 要以快于游戏事件循环的速度进行
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    };


    /**
     * 持续发送对象
     */
    protected final Thread sendingThread = new Thread() {
        {
            setDaemon(true);
            setName("sending");
        }

        @Override
        public void run() {
            while (!this.isInterrupted()) {
                Serializable obj;
                if ((obj = toBeSent.poll()) != null) { // 每个连接每次只发送一个对象
                    try {
                        communicator.sendObject(obj);
                    } catch (SocketTimeoutException e) {
                        toBeSent.add(obj);
                    } catch (IOException e) {
                        if (client.getAlive()) {
                            e.printStackTrace();
                        }
                        client.close();
                        close();
                        break;
                    }
                }
                try {
                    //noinspection BusyWait
                    Thread.sleep(1); // 要以快于游戏事件循环的速度进行
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    };


}
