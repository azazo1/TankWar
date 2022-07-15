package com.azazo1.ui;

import com.azazo1.Config;
import com.azazo1.base.PlayingMode;
import com.azazo1.game.GameMap;
import com.azazo1.game.GameSession;
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
    protected GameSession session;
    protected AtomicBoolean attached = new AtomicBoolean(false);
    protected MyLabel FPSLabel;
    protected MyLabel mapSizeLabel;
    protected MyLabel tankNumLabel;
    protected MyLabel bulletNumLabel;
    protected Box sideBar; // 侧边栏
    protected Vector<MyLabel> tankList = new Vector<>(); // 顺序: 排名 由高到低
    private MyLabel qTreeDepthLabel;
    
    public GamePanel(GameSession.LocalSession session) {
        mode = PlayingMode.LOCAL;
        this.session = session;
    }
    
    @Override
    public void setupUI() {
        if (attached.get()) {
            throw new IllegalStateException("GamePanel cannot be attached twice.");
        }
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
        qTreeDepthLabel = new MyLabel();
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
            // 输出所有坦克基本信息
            for (int i = 0, tanksSize = tanks.size(); i < tanksSize; i++) {
                TankBase.TankInfo info = tanks.get(i);
                MyLabel label = tankList.get(i);
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
            tankList.add(tankLabel);
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
        MyFrame.getInstance().setContentPane(new ResultPanel(session.stop()));
    }
}
