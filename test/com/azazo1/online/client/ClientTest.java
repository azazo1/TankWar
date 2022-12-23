package com.azazo1.online.client;

import com.azazo1.online.msg.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.Assert.fail;

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

    @Test
    void queryClients() throws IOException, InterruptedException {
        try (Client c = new Client("localhost", 60000)) {
            c.queryClients();
            while (!(c.handle() instanceof QueryClientsMsg.QueryClientsResponseMsg)) {
                Thread.sleep(1);
            }
        }
    }

    @Test
    void register() throws IOException, InterruptedException {
        try (Client c = new Client("localhost", 60000)) {
            c.register(true, "hello");
            c.queryClients();
            while (!(c.handle() instanceof QueryClientsMsg.QueryClientsResponseMsg)) {
                Thread.sleep(1);
            }
        }
    }

    @Test
    void reqStartGame() throws IOException, InterruptedException {
        try (Client c = new Client("localhost", 60000)) {
            c.register(true, "hello");
            // 检查自身状态
            c.queryClients();
            c.handle(true);
            c.handle(true);
            c.handle(true);
            c.reqStartGame();
            if (c.isHost()) {
                while (!(c.handle() instanceof GameStartMsg)) {
                    Thread.sleep(10);
                }
            } else {
                fail();
            }
        }
    }

    @Test
    void tankFire() throws IOException, InterruptedException {
        try (Client c = new Client("localhost", 60000)) {
            c.register(true, "hello");
            c.reqStartGame();
            while (!(c.handle() instanceof GameStartMsg)) {
                Thread.sleep(10);
            }
            Thread.sleep(10000);
            c.dataTransfer.sendObject(new TankFireActionMsg());
            Thread.sleep(10000);
        }
    }

    @Test
    void tankMotion() throws IOException, InterruptedException {
        try (Client c = new Client("localhost", 60000)) {
            c.register(true, "hello");
            c.reqStartGame();
            while (!(c.handle() instanceof GameStartMsg)) {
                Thread.sleep(10);
            }
            Thread.sleep(10000);
            c.dataTransfer.sendObject(new KeyPressChangeMsg(true, false, true, false));
            Thread.sleep(10000);
        }
    }
}