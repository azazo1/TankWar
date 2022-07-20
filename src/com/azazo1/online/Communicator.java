package com.azazo1.online;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

/**
 * 兼顾对象和字符串输出
 */
public class Communicator {
    private final ObjectInputStream objIn;
    private final ObjectOutputStream objOut;
    
    public Communicator(@NotNull Socket soc) throws IOException {
        super();
        objOut = new ObjectOutputStream(soc.getOutputStream());
        objIn = new ObjectInputStream(soc.getInputStream());
    }
    
    /**
     * 发送可序列化对象, 可以是字符串
     *
     * @param obj 将要被发送的对象
     */
    public void sendObject(@NotNull Serializable obj) throws IOException {
        objOut.writeObject(obj);
        objOut.flush();
    }
    
    /**
     * 读取一个对象
     */
    public @Nullable Serializable readObject() throws IOException {
        try {
            return (Serializable) objIn.readObject();
        } catch (ClassNotFoundException e) {
            return null; // 一般不会到这
        }
    }
    
    public void close() {
        try {
            objOut.close();
        } catch (IOException ignore) {
        }
        try {
            objIn.close();
        } catch (IOException ignore) {
        }
    }
}
