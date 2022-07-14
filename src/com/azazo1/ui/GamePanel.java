package com.azazo1.ui;

import com.azazo1.Config;
import com.azazo1.base.PlayingMode;
import com.azazo1.game.GameMap;
import com.azazo1.game.GameSession;
import com.azazo1.game.tank.TankGroup;
import com.azazo1.util.Tools;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.awt.GridBagConstraints.REMAINDER;

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
    protected JPanel sideBar; // 侧边栏
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
        horizontalBox = Box.createHorizontalBox();
        verticalBox = Box.createVerticalBox();
        sideBar = new JPanel();
        FPSLabel = new MyLabel();
        mapSizeLabel = new MyLabel();
        tankNumLabel = new MyLabel();
        bulletNumLabel = new MyLabel();
        qTreeDepthLabel = new MyLabel();
        qTreeDepthLabel.setText(session.getGameMap().getWallGroup().getQTreeDepth());
        
        
        session.setFrameListener((tankNum, bulletNum) -> {
            tankNumLabel.setText(tankNum, session.getTotalTankNum(), (TankGroup) null);
            bulletNumLabel.setText(bulletNum, null);
            GameMap map = session.getGameMap();
            mapSizeLabel.setText(map.getWidth(), map.getHeight());
            FPSLabel.setText(Tools.getFPS(), Tools.getAverageFPS(), (Tools) null);
            Tools.tickFrame();
        });
        horizontalBox.add(Box.createVerticalStrut(Config.MAP_HEIGHT));
        horizontalBox.add(verticalBox);
        verticalBox.add(Box.createHorizontalStrut(Config.MAP_WIDTH));
        
        add(horizontalBox);
        
        verticalBox.add(session.getGameMap());
        
        // 侧边栏
        horizontalBox.add(Box.createHorizontalStrut(5));
        horizontalBox.add(sideBar);
        sideBar.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = REMAINDER;
        sideBar.add(FPSLabel, constraints);
        sideBar.add(mapSizeLabel, constraints);
        sideBar.add(tankNumLabel, constraints);
        sideBar.add(bulletNumLabel, constraints);
        sideBar.add(qTreeDepthLabel, constraints);
        
        attached.set(true);
    }
    
    /**
     * 启动 Session
     * <p>
     * 应该在{@link GamePanel#setupUI()}后调用
     */
    public void start() {
        EventQueue.invokeLater(session::start);
    }
}
