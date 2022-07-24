package com.azazo1.online;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.Socket;
import java.util.Base64;

/**
 * 进行 Java 对象的传输 (兼顾对象和字符串), 将 Java 对象通过 ObjectOutputStream 序列化后再进行 Base64 编码, 然后传输
 */
public class Communicator implements Closeable {
    private final BufferedReader in;
    private final PrintWriter out;
    
    public Communicator(@NotNull Socket soc) throws IOException {
        super();
        out = new PrintWriter(soc.getOutputStream());
        in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
    }
    
    /**
     * 发送可序列化对象, 可以是字符串
     *
     * @param obj 将要被发送的对象
     */
    public void sendObject(@NotNull Serializable obj) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream objOut = new ObjectOutputStream(bytes)) {
            objOut.writeObject(obj);
        }
        out.println(Base64.getEncoder().encodeToString(bytes.toByteArray()));
        out.flush();
    }
    
    /**
     * 读取一个对象
     *
     * @throws NullPointerException 连接已经断开
     */
    public @Nullable Serializable readObject() throws IOException {
        try {
            String line = in.readLine();
            ByteArrayInputStream bytes = new ByteArrayInputStream(Base64.getDecoder().decode(line));
            try (ObjectInputStream objIn = new ObjectInputStream(bytes)) {
                return (Serializable) objIn.readObject();
            }
        } catch (ClassNotFoundException e) {
            return null; // 一般不会到这
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        out.close();
        try {
            in.close();
        } catch (IOException ignore) {
        }
    }
}
