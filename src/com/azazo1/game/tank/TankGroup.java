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
    
    public void removeTank(TankBase tank) {
        tanks.remove(tank);
        tank.clearTankGroup();
    }
    
    public void update(Graphics g) {
        if (tanks.isEmpty()) {
            return;
        }
        for (int i = 0; i < tanks.size(); i++) {
            TankBase tank = tanks.get(i);
            tank.update(g.create()); // 更新坦克, 传入一个副本
            if (tank.getEnduranceManager().isDead()) {
                removeTank(tank); // 移除死亡坦克
                i--; // 移除后后来的元素向前, 对应 i 向前
            }
        }
    }
}
