package com.azazo1.ui;

import java.io.IOException;

public class OnlineWaitingRoomTest {
    public static void main(String[] args) throws IOException {
        MyFrame f = new MyFrame();
        OnlineWaitingRoomPanel a = new OnlineWaitingRoomPanel(f, "127.0.0.1", 60000);
    }
}
