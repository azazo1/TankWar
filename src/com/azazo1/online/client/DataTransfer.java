package com.azazo1.online.client;

import com.azazo1.Config;
import com.azazo1.online.Communicator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.net.SocketTimeoutException;
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
                    throw new RuntimeException(e);
                }
            }
            return received.poll(); // 不会再空了
        } else {
            try {
                return received.poll(Config.CLIENT_SOCKET_TIMEOUT, TimeUnit.MILLISECONDS);
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
        }
        
        @Override
        public void run() {
            while (!this.isInterrupted()) {
                try {
                    Serializable get = communicator.readObject(); // 每个连接每次只读取一个对象
                    if (get != null) {
                        received.add(get);
                    }
                } catch (NullPointerException e) { // 连接断开
                    close();
                    client.close();
                } catch (SocketTimeoutException ignore) {
                } catch (IOException e) {
                    close();
                    client.close();
                    break;
                }
                try {
                    //noinspection BusyWait
                    Thread.sleep((long) (550.0 / Config.FPS)); // 要以略快于游戏事件循环的速度进行
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
        }
        
        @Override
        public void run() {
            while (!this.isInterrupted()) {
                Serializable obj;
                // 由于异步问题, 新客户端加入时 targets 可能为 null, 但后来就会被赋予相应的值
                if ((obj = toBeSent.poll()) != null) { // 每个连接每次只发送一个对象
                    try {
                        communicator.sendObject(obj);
                    } catch (SocketTimeoutException e) {
                        toBeSent.add(obj);
                    } catch (IOException e) {
                        client.close();
                    }
                }
                try {
                    //noinspection BusyWait
                    Thread.sleep((long) (550.0 / Config.FPS)); // 要以略快于游戏事件循环的速度进行
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    };
    
    
}
