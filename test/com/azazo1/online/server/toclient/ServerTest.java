package com.azazo1.online.server.toclient;

import com.azazo1.online.client.Client;
import com.azazo1.online.msg.GameStartMsg;
import com.azazo1.online.msg.QueryClientsMsg;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.io.IOException;

public class ServerTest {
    @Test
    void testMsgDelay() {
        try (Client c = new Client("localhost", 60000)) {
            c.register(true, "hello");
            JFrame jFrame = new JFrame();
            while (c.getAlive()) {
                JOptionPane.showMessageDialog(jFrame, null);
                c.queryClients();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void registerAndWait() throws IOException, InterruptedException {
        try (Client c = new Client("localhost", 60000)) {
            c.register(true, "registerAndWait");
            c.queryClients();
            while (!(c.handle() instanceof QueryClientsMsg.QueryClientsResponseMsg)) {
                Thread.sleep(10);
            }
            while (c.getAlive()) {
                Thread.sleep(100);
            }
        }
    }

    @Test
    void sendKeyChange() throws IOException, InterruptedException {
        try (Client c = new Client("localhost", 60000)) {
            c.register(true, "recvGameState");
            Thread.sleep(5000);
            c.reqStartGame();
            while (!(c.handle() instanceof GameStartMsg)) {
                Thread.sleep(10);
            }
            JFrame jFrame = new JFrame();
            while (c.getAlive()) {
                JOptionPane.showMessageDialog(jFrame, null);
                c.changePressedKey(false, false, false, false);
                c.handle();
            }
        }
    }
}
