package com.azazo1.bullet;

import com.azazo1.GameMap;

import java.awt.*;
import java.util.Vector;

public class BulletGroup {
    protected Vector<BulletBase> bullets = new Vector<>();
    protected GameMap map;
    
    public void addBullet(BulletBase bulletBase) {
        bullets.add(bulletBase);
        bulletBase.setBulletGroup(this);
    }
    
    public Vector<BulletBase> getBullets() {
        return bullets;
    }
    
    public void update(Graphics graphics) {
        for (int i = 0; i < bullets.size(); i++) {
            BulletBase bullet = bullets.get(i);
            if (bullet.isFinished()) {
                removeBullet(bullet);
                i--;
            }
        }
        paint(graphics);
    }
    
    public void removeBullet(BulletBase bullet) {
        bullets.remove(bullet);
        bullet.clearBulletGroup();
    }
    
    protected void paint(Graphics graphics) {
        if (bullets.isEmpty()) {
            return;
        }
        for (BulletBase bullet : bullets) {
            bullet.update(graphics.create());
        }
    }
    
    public GameMap getGameMap() {
        return map;
    }
    
    /**
     * 不保证真的能从 map 中添加,本方法应由 {@link GameMap#setBulletGroup(BulletGroup)} 调用
     */
    public void setGameMap(GameMap map) {
        this.map = map;
    }
    
    /**
     * 不保证真的能从 map 中去除,本方法应由 {@link GameMap#setBulletGroup(BulletGroup)} 调用
     */
    public void clearGameMap() {
        this.map = null;
    }
}
