package com.azazo1.ui;

import com.azazo1.Config;
import com.azazo1.base.SingleInstance;
import com.azazo1.util.Tools;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

import static com.azazo1.util.Tools.resizeFrame;

public class MyFrame extends JFrame implements SingleInstance {
    protected static MyFrame instance;

    public MyFrame() throws IOException { // 这个构造函数只会被调用一次
        super();
        checkInstance();
        instance = this;
        setTitle(Config.translation.frameTitle);
        setIconImage(ImageIO.read(Tools.getFileURL("img/FrameIcon.png").url()));
        setContentPane(new MyPanel() { // 显示加载中画面
            @Override
            public void setupUI(MyFrame frame) {
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

    @Override
    public void checkInstance() {
        if (hasInstance()) {
            throw new IllegalStateException("MyFrame can be created only once.");
        }
    }

    @Override
    public boolean hasInstance() {
        return instance != null;
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
        if (!(contentPane instanceof MyPanel)) {
            throw new IllegalArgumentException("MyFrame supports MyPanel only");
        }
        setContentPane((MyPanel) contentPane);
    }

    public void setContentPane(MyPanel panel) {
        super.setContentPane(panel);
        panel.setupUI(this);
    }
}
