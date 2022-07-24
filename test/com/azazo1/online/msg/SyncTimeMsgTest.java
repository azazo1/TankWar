package com.azazo1.online.msg;

import com.azazo1.online.Communicator;
import com.azazo1.util.Tools;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.fail;

class SyncTimeMsgTest {
    @Test
    void syncTime() throws IOException {
        Communicator comm = new Communicator(new Socket("localhost", 30043)); // 记得调整服务器地址
        long strangeBias = -16000000000000L;
        Tools.setTimeBias(strangeBias); // 先设置一个离谱的时间偏移值
        SyncTimeMsg localMsg = new SyncTimeMsg();
        long startTime = localMsg.createdTime;
        System.out.println("Local Msg Time = " + startTime);
        comm.sendObject(localMsg);
        Object get = comm.readObject();
        if (get instanceof SyncTimeMsg.SyncTimeResponseMsg msg) {
            long endTime = Tools.getRealTimeInMillis();
            System.out.println("Server Response Time: " + msg.createdTime);
            long bias = msg.createdTime - (endTime - startTime) / 2 - localMsg.createdTime;
            System.out.println("Bias: " + bias);
            System.out.println("Before Adjust: " + Tools.getRealTimeInMillis());
            Tools.setTimeBias(strangeBias + bias);
            System.out.println("After Adjust: " + Tools.getRealTimeInMillis());
        } else {
            fail();
        }
        comm.close();
    }
    
    @Test
    void toBase64String() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(new SyncTimeMsg());
        }
        System.out.println(Base64.getEncoder().encodeToString(bytes.toByteArray()));
    }
}