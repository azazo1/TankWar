package com.azazo1;

import com.azazo1.ui.MenuPanel;
import com.azazo1.ui.MyFrame;

import javax.swing.*;
import java.io.IOException;

import static com.azazo1.util.Tools.init;
import static com.azazo1.util.Tools.resizeFrame;

public class Main {// todo 设置界面
    public static void main(String[] args) throws IOException {
        init();
        MyFrame f = new MyFrame();
        MenuPanel m = new MenuPanel(f);
        f.setResizable(false);
        resizeFrame(f, Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);
        f.setVisible(true);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
