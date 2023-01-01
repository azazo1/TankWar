package com.azazo1.ui;

import com.azazo1.Config;
import com.azazo1.base.PlayingMode;
import com.azazo1.game.GameMap;
import com.azazo1.game.session.GameSession;
import com.azazo1.game.tank.TankBase;
import com.azazo1.game.tank.TankGroup;
import com.azazo1.util.Tools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

public class GamePanel extends MyPanel {
    protected final String mode; // 游戏模式 @see PlayingMode
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

    public GamePanel(GameSession.LocalSession session) {
        mode = PlayingMode.LOCAL;
        this.session = session;
    }

    @Override
    public void setupUI(MyFrame frame) {
        if (attached.get()) {
            throw new IllegalStateException("GamePanel cannot be attached twice.");
        }
        this.frame = frame;
        // 设定退出键
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getKeyCode() == Config.QUIT_GAME_KEY) {
                    EventQueue.invokeLater(() -> {
                        stop();
                        manager.removeKeyEventDispatcher(this);
                    });
                    return true;
                }
                return false;
            }
        });

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
        setVisible(false);
        new ResultPanel(frame, session.stop(), true, true);
    }
}
