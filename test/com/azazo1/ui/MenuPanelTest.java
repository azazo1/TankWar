package com.azazo1.ui;

import com.azazo1.Config;

import javax.swing.*;

import static com.azazo1.util.Tools.resizeFrame;

class MenuPanelTest {
    
    public static void main(String[] args) {
        JFrame f = new JFrame();
        MenuPanel m = new MenuPanel();
        f.setContentPane(m);
        m.setupUI();
        f.setResizable(false);
        resizeFrame(f, Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);
        f.setVisible(true);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}