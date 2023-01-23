package com.azazo1.ui;

import com.azazo1.Config;
import com.azazo1.base.PlayingMode;
import com.azazo1.game.GameMap;
import com.azazo1.game.session.GameSession;
import com.azazo1.game.tank.TankBase;
import com.azazo1.game.tank.TankGroup;
import com.azazo1.util.MyFrameSetting;
import com.azazo1.util.Tools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

public class GamePanel extends MyPanel {
    protected final String mode; // 游戏模式 @see PlayingMode
    private final MyFrameSetting originalFrameSetting;
    public KeyEventDispatcher quitKeyDispatcher;
    protected Box horizontalBox;
    protected Box verticalBox;
    protected final GameSession session;
    protected final AtomicBoolean attached = new AtomicBoolean(false);
    protected MyFrame frame;
    protected MyLabel FPSLabel;
    protected MyLabel mapSizeLabel;
    protected MyLabel tankNumLabel;
    protected MyLabel bulletNumLabel;
    protected Box sideBar; // 侧边栏
    protected final Vector<MyLabel> tankLabelList = new Vector<>(); // 在侧边栏显示坦克详细信息
    protected final WindowAdapter doOnCloseWindow = new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
            stop();
        }
    };

    /**
     * {@link GamePanel} 只需初始化即可设置为 {@link MyFrame} 的 contentPane, 不需要显式调用 {@link MyFrame#setContentPane(MyPanel)}
     */
    public GamePanel(GameSession.LocalSession session) {
        mode = PlayingMode.LOCAL;
        this.session = session;
        frame = MyFrame.getInstance();
        originalFrameSetting = new MyFrameSetting(frame);
        frame.setContentPane(this);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.addWindowListener(doOnCloseWindow);
    }

    /**
     * 结束游戏时, 由 {@link GamePanel#stop()} 调用, 请勿直接调用
     */
    protected void dispose() {
        setVisible(false);
        frame.removeWindowListener(doOnCloseWindow);
        originalFrameSetting.restore();
    }

    @Override
    public void setupUI() {
        if (attached.get()) {
            throw new IllegalStateException("GamePanel cannot be attached twice.");
        }
        // 设定退出键
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        quitKeyDispatcher = new KeyEventDispatcher() {
            final AtomicBoolean invoked = new AtomicBoolean(false); // dispatchKeyEvent 是否被调用过(调用过则说明游戏结束)

            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getKeyCode() == Config.QUIT_GAME_KEY) {
                    EventQueue.invokeLater(() -> {
                        if (!invoked.get()) {
                            stop();
                            invoked.set(true);
                        }
                    });
                    manager.removeKeyEventDispatcher(this);
                    return true;
                }
                return false;
            }
        };
        manager.addKeyEventDispatcher(quitKeyDispatcher);

        horizontalBox = Box.createHorizontalBox();
        verticalBox = Box.createVerticalBox();
        sideBar = Box.createVerticalBox();
        FPSLabel = new MyLabel();
        mapSizeLabel = new MyLabel();
        tankNumLabel = new MyLabel();
        bulletNumLabel = new MyLabel();
        MyLabel qTreeDepthLabel = new MyLabel();
        qTreeDepthLabel.setText(session.getGameMap().getWallGroup().getQTreeDepth());
        GameMap map = session.getGameMap();
        mapSizeLabel.setText(map.getWidth(), map.getHeight());


        session.setFrameListener((tanks, bulletNum) -> {
            tankNumLabel.setText(
                    session.getGameMap().getTankGroup().getLivingTankNum(),
                    session.getTotalTankNum(),
                    (TankGroup) null
            );
            bulletNumLabel.setText(bulletNum, null);
            FPSLabel.setText(Tools.getFPS(), Tools.getAverageFPS(), (Tools) null);
            Tools.tickFrame();
            if (session.isOver()) {
                stop();
            }
            // 输出所有坦克基本信息 顺序: 排名 由高到低
            for (int i = 0, tanksSize = tanks.size(); i < tanksSize; i++) {
                TankBase.TankInfo info = tanks.get(i);
                MyLabel label = tankLabelList.get(i);
                label.setText(info);
            }
        });
        horizontalBox.add(Box.createVerticalStrut(Config.MAP_HEIGHT));
        horizontalBox.add(verticalBox);
        verticalBox.add(Box.createHorizontalStrut(Config.MAP_WIDTH));

        add(horizontalBox);

        verticalBox.add(session.getGameMap());

        // 侧边栏
        horizontalBox.add(Box.createHorizontalStrut(5));
        horizontalBox.add(sideBar);
        sideBar.add(FPSLabel);
        sideBar.add(tankNumLabel);
        sideBar.add(bulletNumLabel);
        sideBar.add(mapSizeLabel);
        sideBar.add(qTreeDepthLabel);
        for (int i = 0; i < session.getTotalTankNum(); i++) {
            MyLabel tankLabel = new MyLabel();
            sideBar.add(tankLabel);
            tankLabelList.add(tankLabel);
        }

        attached.set(true);
    }

    /**
     * 启动 Session
     *
     * @apiNote 应该在{@link GamePanel#setupUI()}后调用
     */
    public void start() {
        setVisible(true);
        EventQueue.invokeLater(session::start);
    }

    /**
     * 关闭 Session, 并显示游戏结局 {@link ResultPanel}
     */
    public void stop() {
        dispose();
        new ResultPanel(frame, session.stop(), true, true);
    }
}
