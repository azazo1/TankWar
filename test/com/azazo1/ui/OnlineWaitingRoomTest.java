package com.azazo1.ui;

import com.azazo1.Config;

import javax.swing.*;
import java.io.IOException;

import static com.azazo1.util.Tools.resizeFrame;

public class OnlineWaitingRoomTest {
    public static void main(String[] args) throws IOException {
        JFrame f = new JFrame();
        OnlineWaitingRoomPanel a = new OnlineWaitingRoomPanel("127.0.0.1", 60000);
        f.setContentPane(a.panel);
//        f.setResizable(false);
        resizeFrame(f, 850, 500);
        f.setVisible(true);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
