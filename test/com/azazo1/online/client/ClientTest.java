package com.azazo1.online.client;

import org.junit.jupiter.api.Test;

import java.io.IOException;

class ClientTest {
    
    @Test
    void syncTime() throws IOException {
        // 目标: 不卡死
        try (Client c = new Client("localhost", 60000)) { // 初始化时 syncTime
            c.syncTime(); // 第二次 syncTime
        }
    }
    
    @Test
    void requestSeq() throws IOException, InterruptedException {
        try (Client c = new Client("localhost", 60000)) { // 初始化时 syncTime
            while (c.getSeq() == -1) {
                c.handle();
                Thread.sleep(1);
            }
            System.out.println("requestSeq succeed: " + c.getSeq());
        }
    }
}