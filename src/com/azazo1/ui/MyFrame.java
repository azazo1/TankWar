package com.azazo1.ui;

import com.azazo1.Config;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

import static com.azazo1.util.Tools.resizeFrame;

public class MyFrame extends JFrame {
    protected static MyFrame instance;
    
    public MyFrame() throws IOException { // 这个构造函数只会被调用一次
        super();
        if (instance != null) {
            throw new IllegalStateException("MyFrame can be created only once.");
        }
        instance = this;
        setTitle(Config.translation.frameTitle);
        setIconImage(ImageIO.read(new File("res/FrameIcon.png")));
        setContentPane(new MyPanel() { // 显示加载中画面
            @Override
            public void setupUI() {
                setLayout(new BorderLayout());
                add(new JLabel(Config.translation.loading, SwingConstants.CENTER), BorderLayout.CENTER);
            }
        });
        resizeFrame(this, Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);
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
