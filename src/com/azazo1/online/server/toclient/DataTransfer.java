package com.azazo1.online.server.toclient;

import com.azazo1.Config;
import com.azazo1.online.Communicator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.net.SocketTimeoutException;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 异步地传输数据
 */
public class DataTransfer implements Closeable {
    protected final HashMap<ClientHandler, Communicator> communicators = new HashMap<>();
    /**
     * 接收到但是未被提取的对象
     */
    protected final HashMap<ClientHandler, LinkedTransferQueue<Serializable>> received = new HashMap<>();
    /**
     * 等待被发送的对象
     */
    protected final HashMap<ClientHandler, LinkedTransferQueue<Serializable>> toBeSent = new HashMap<>();
    private final AtomicBoolean alive = new AtomicBoolean(true);
    
    public DataTransfer() {
        readingThread.start();
        sendingThread.start();
    }
    
    /**
     * 将可序列化对象添加到待发送列表
     *
     * @param client 指定要被发送的客户端
     * @param obj    将要被发送的对象
     */
    public void sendObject(@NotNull ClientHandler client, @NotNull Serializable obj) {
        if (!alive.get()) {
            throw new IllegalStateException("DataTransfer has closed.");
        }
        toBeSent.get(client).add(obj);
    }
    
    /**
     * 提取待提取列表中的已读信息
     *
     * @return 若待提取列表为空则返回 null
     */
    public @Nullable Serializable readObject(@NotNull ClientHandler client) {
        if (!alive.get()) {
            throw new IllegalStateException("DataTransfer has closed.");
        } else if (!client.getAlive()) {
            throw new IllegalArgumentException("Closed clientHandler");
        }
        return received.get(client).poll();
    }
    
    /**
     * 添加客户端
     *
     * @return 是否成功添加
     */
    public boolean addClient(@NotNull ClientHandler client) {
        if (!alive.get()) {
            throw new IllegalStateException("DataTransfer has closed.");
        } else if (!client.getAlive()) {
            throw new IllegalArgumentException("Closed clientHandler");
        }
        Communicator comm;
        try {
            comm = new Communicator(client.getSocket());
        } catch (IOException e) {
            return false;
        }
        communicators.put(client, comm);
        received.put(client, new LinkedTransferQueue<>());
        toBeSent.put(client, new LinkedTransferQueue<>());
        return true;
        
    }
    
    /**
     * 结束使用
     */
    @Override
    public void close() {
        for (Communicator c : communicators.values()) {
            c.close();
        }
        communicators.clear();
        received.clear();
        toBeSent.clear();
        readingThread.interrupt();
        sendingThread.interrupt();
        alive.set(false);
    }
    
    public boolean getAlive() {
        return alive.get();
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
                    for (ClientHandler client : communicators.keySet()) {
                        Communicator comm = communicators.get(client);
                        try {
                            Serializable get = comm.readObject(); // 每个连接每次只读取一个对象
                            if (get != null) {
                                received.get(client).add(get);
                            }
                        } catch (NullPointerException e) { // 连接断开
                            client.close();
                        } catch (SocketTimeoutException ignore) {
                        } catch (IOException e) {
                            client.close();
                        }
                        
                    }
                    try {
                        //noinspection BusyWait
                        Thread.sleep((long) (550.0 / Config.FPS)); // 要以略快于游戏事件循环的速度进行
                    } catch (InterruptedException e) {
                        close();
                        break;
                    }
                } catch (ConcurrentModificationException ignore) { // 客户端加入或被移除
                }
            }
        }
    };
    
    
    /**
     * 持续发送对象
     *
     * @apiNote 对于特定一个客户端的信息, 由于超时影响, 发送先后顺序可能不确定
     */
    protected final Thread sendingThread = new Thread() {
        {
            setDaemon(true);
            setName("sending");
        }
        
        @Override
        public void run() {
            while (!this.isInterrupted()) {
                try {
                    for (ClientHandler client : communicators.keySet()) {
                        Communicator comm = communicators.get(client);
                        Serializable obj;
                        LinkedTransferQueue<Serializable> targets = toBeSent.get(client);
                        // 由于异步问题, 新客户端加入时 targets 可能为 null, 但后来就会被赋予相应的值
                        if (targets != null && (obj = targets.poll()) != null) { // 每个连接每次只发送一个对象
                            try {
                                comm.sendObject(obj);
                            } catch (SocketTimeoutException e) {
                                targets.add(obj);
                            } catch (IOException e) {
                                client.close();
                            }
                        }
                    }
                    try {
                        //noinspection BusyWait
                        Thread.sleep((long) (550.0 / Config.FPS)); // 要以略快于游戏事件循环的速度进行
                    } catch (InterruptedException e) {
                        close();
                        break;
                    }
                } catch (ConcurrentModificationException ignore) { // 客户端加入或被移除
                }
            }
        }
    };
}
