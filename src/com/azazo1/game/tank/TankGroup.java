package com.azazo1.game.tank;

import com.azazo1.game.GameMap;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

public class TankGroup {
    protected GameMap map;
    protected Vector<TankBase> tanks = new Vector<>();
    protected Vector<TankBase> tankDeathSequence = new Vector<>(); // 用于储存坦克死亡顺序, 索引 0 为最先死亡
    protected KeyListener keyListener = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            for (TankBase tank : tanks) {
                tank.pressKey(e.getKeyCode());
            }
        }
        
        @Override
        public void keyReleased(KeyEvent e) {
            for (TankBase tank : tanks) {
                tank.releaseKey(e.getKeyCode());
            }
        }
    };
    
    public TankGroup() {
    }
    
    public GameMap getGameMap() {
        return map;
    }
    
    /**
     * 不保证真的能从 map 中添加,本方法应由 {@link GameMap#setTankGroup(TankGroup)} 调用
     */
    public void setGameMap(GameMap map) {
        this.map = map;
        this.getGameMap().addKeyListener(keyListener);
    }
    
    /**
     * 不保证真的能从 map 中去除,本方法应由 {@link GameMap#setTankGroup(TankGroup)} 调用
     */
    public void clearGameMap() {
        this.getGameMap().removeKeyListener(keyListener);
        this.map = null;
    }
    
    public void addTank(TankBase tank) {
        tanks.add(tank);
        tank.setTankGroup(this);
    }
    
    /**
     * 移除坦克
     * 此方法不会在游戏进行时被调用
     */
    public void removeTank(TankBase tank) {
        tanks.remove(tank);
        tankDeathSequence.remove(tank);
        tank.clearTankGroup();
        tank.getEnduranceManager().makeDie(); // 强行销毁坦克
    }
    
    public void update(Graphics g) {
        if (tanks.isEmpty()) {
            return;
        }
        for (int i = 0; i < tanks.size(); i++) {
            TankBase tank = tanks.get(i);
            if (tank.getEnduranceManager().isDead()) {
                continue;
            }
            tank.update(g.create()); // 更新坦克, 传入一个副本
            if (tank.getEnduranceManager().isDead()) {
                // 不对死亡坦克进行移除 (removeTank)
                tankDeathSequence.add(tank);
                i--; // 移除后后来的元素向前, 对应 i 向前
            }
        }
    }
    
    /**
     * 获得现在存活的坦克数量
     */
    public int getLivingTankNum() {
        return tanks.size() - tankDeathSequence.size();
    }
    
    /**
     * 获得坦克死亡顺序
     */
    public Vector<TankBase> getTankDeathSequence() {
        return new Vector<>(tankDeathSequence);
    }
    
    /**
     * 获得所有坦克信息
     */
    public Vector<TankBase.TankInfo> getTanksInfo() {
        Vector<TankBase.TankInfo> multiInfo = new Vector<>();
        for (TankBase tank : tanks) {
            multiInfo.add(tank.getInfo());
        }
        return multiInfo;
    }
}
