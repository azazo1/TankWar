package com.azazo1.ui;

import com.azazo1.Config;

import javax.swing.*;
import java.awt.*;

public class MyFrame extends JFrame {
    protected static MyFrame instance;
    
    public MyFrame() { // 这个构造函数只会被调用一次
        super();
        if (instance != null) {
            throw new IllegalStateException("MyFrame can be created only once.");
        }
        instance = this;
        consumeFontLoadingTime();
    }
    
    public static MyFrame getInstance() {
        return instance;
    }
    
    /**
     * 先尝试加载字体, 防止游戏启动时卡顿
     */
    public void consumeFontLoadingTime() {
        Timer timer = new Timer((int) (1000.0 / Config.FPS), null);
        timer.setRepeats(true);
        timer.addActionListener((e) -> {
            try {
                Graphics2D g2d = ((Graphics2D) getContentPane().getGraphics());
                g2d.setFont(Config.TANK_CLIP_FONT);
                g2d.drawString("", 0, 0);
                g2d.dispose();
                timer.stop();
            } catch (NullPointerException ignored) {
            }
        });
        timer.start();
    }
    
    @Override
    public Component add(Component comp) {
        throw new IllegalCallerException("This frame supports content panel only.");
    }
    
    @Override
    public void setContentPane(Container contentPane) {
        throw new IllegalArgumentException("MyFrame supports MyPanel only");
    }
    
    public void setContentPane(MyPanel panel) {
        super.setContentPane(panel);
        panel.setupUI();
    }
}
