package com.azazo1.game.bullet;

import com.azazo1.game.GameMap;
import com.azazo1.game.tank.TankBase;

import java.awt.*;
import java.util.Vector;

/**
 * 管理子弹, 子弹不需手动写代码添加, 一般由 {@link TankBase} 调用
 */
public class BulletGroup {
    protected final Vector<BulletBase> bullets = new Vector<>();
    protected GameMap map;

    public void addBullet(BulletBase bulletBase) {
        bullets.add(bulletBase);
        bulletBase.setBulletGroup(this);
    }

    public Vector<BulletBase> getBullets() {
        return new Vector<>(bullets);
    }

    public void update(Graphics graphics) {
        synchronized (bullets) {
            for (int i = 0; i < bullets.size(); i++) {
                BulletBase bullet = bullets.get(i);
                if (bullet.isFinished()) {
                    removeBullet(bullet);
                    i--;
                }
            }
            if (bullets.isEmpty()) {
                return;
            }
            for (BulletBase bullet : bullets) {
                try {
                    graphics.create();
                } catch (NullPointerException e) {
                    bullet.update(null);
                    return;
                }
                bullet.update(graphics.create());
            }
        }
    }

    public void removeBullet(BulletBase bullet) {
        bullets.remove(bullet);
        bullet.clearBulletGroup();
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

    /**
     * 获得子弹数
     */
    public int getBulletNum() {
        return bullets.size();
    }

    public Vector<BulletBase.BulletInfo> getBulletInfo() {
        Vector<BulletBase.BulletInfo> v = new Vector<>();
        for (BulletBase bullet : bullets) {
            v.add(bullet.getInfo());
        }
        return v;
    }
}
