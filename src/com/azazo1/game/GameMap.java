package com.azazo1.game;

import com.azazo1.Config;
import com.azazo1.game.bullet.BulletGroup;
import com.azazo1.game.tank.TankBase;
import com.azazo1.game.tank.TankGroup;
import com.azazo1.game.wall.WallGroup;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.font.TextLayout;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 游戏画面
 */
public class GameMap extends Canvas {
    protected final AtomicBoolean doPaint = new AtomicBoolean(true); // 是否显示, 服务端子类设为 false
    protected TankGroup tankGroup;
    protected WallGroup wallGroup;
    protected BulletGroup bulletGroup;

    public GameMap() {
        super();
    }

    /**
     * 废弃 GameMap, 释放内存
     */
    public void dispose() {
        if (tankGroup != null) {
            tankGroup.clearGameMap();
            tankGroup = null;
        }
        if (wallGroup != null) {
            wallGroup.clearGameMap();
            wallGroup = null;
        }
        if (bulletGroup != null) {
            bulletGroup.clearGameMap();
            bulletGroup = null;
        }
    }

    /**
     * 更新 {@link GameMap} 对象状态
     *
     * @apiNote 此方法要在 {@link #tankGroup} {@link #wallGroup} {@link #bulletGroup} 都被设置后才能调用
     */
    @Override
    public void update(Graphics g) {
        setSize(Config.MAP_WIDTH, Config.MAP_HEIGHT);
        if (g == null) {
            g = getGraphics();
        }
        paint(g);
    }

    @Override
    public void paint(Graphics g) {
        boolean doPaint = this.doPaint.get();

        if (doPaint) {
            Image buffer = createImage(getWidth(), getHeight()); // 二级缓冲
            Graphics bufferGraphics = buffer.getGraphics();
            Graphics2D g2d = null;
            g2d = (Graphics2D) bufferGraphics;
            bufferGraphics.setColor(Config.BACKGROUND_COLOR);
            bufferGraphics.fillRect(0, 0, getWidth(), getHeight());
            // 更新游戏信息, 服务端不会取消此过程
            getTankGroup().update(bufferGraphics.create()); // 传入副本
            getWallGroup().update(bufferGraphics.create()); // 传入副本
            getBulletGroup().update(bufferGraphics.create()); // 传入副本
            if (!hasFocus()) {
                bufferGraphics.setColor(Config.TEXT_COLOR);
                TextLayout text = new TextLayout(Config.translation.clickToFocusHint, Config.TEXT_FONT, g2d.getFontRenderContext());
                text.draw(g2d, 50, 50);
            }
            // g.fillRect(0, 0, getWidth(), getHeight()); 不需要再清除内容,因为后来的图片直接覆盖
            g.drawImage(buffer, 0, 0, getWidth(), getHeight(), null);
            bufferGraphics.dispose();
        } else {
            getTankGroup().update(null);
            getWallGroup().update(null);
            getBulletGroup().update(null);
        }
    }

    public TankGroup getTankGroup() {
        return tankGroup;
    }

    public void setTankGroup(@NotNull TankGroup tankGroup) {
        if (this.tankGroup != null && tankGroup != this.tankGroup) {
            this.tankGroup.clearGameMap();
        }
        this.tankGroup = tankGroup;
        tankGroup.setGameMap(this);
    }

    public WallGroup getWallGroup() {
        return wallGroup;
    }

    public void setWallGroup(@NotNull WallGroup wallGroup) {
        if (this.wallGroup != null && wallGroup != this.wallGroup) {
            this.wallGroup.clearGameMap();
        }
        this.wallGroup = wallGroup;
        wallGroup.setGameMap(this);
    }

    public BulletGroup getBulletGroup() {
        return bulletGroup;
    }

    public void setBulletGroup(@NotNull BulletGroup bulletGroup) {
        if (this.bulletGroup != null && this.bulletGroup != bulletGroup) {
            this.bulletGroup.clearGameMap();
        }
        this.bulletGroup = bulletGroup;
        bulletGroup.setGameMap(this);
    }

    public GameInfo getInfo() {
        return new GameInfo(this);
    }

    public static class GameInfo {
        protected final Vector<TankBase.TankInfo> tanksInfo;
        protected final int mapHeight;
        protected final int mapWidth;

        protected GameInfo(@NotNull GameMap map) {
            mapHeight = map.getHeight();
            mapWidth = map.getWidth();
            tanksInfo = map.tankGroup.getTanksInfo();
        }

        public Vector<TankBase.TankInfo> getTanksInfo() {
            return tanksInfo;
        }

        public int getMapHeight() {
            return mapHeight;
        }

        public int getMapWidth() {
            return mapWidth;
        }

        @Override
        public String toString() {
            return "GameInfo{" +
                    "tanksInfo=" + tanksInfo.toString() +
                    ", mapHeight=" + mapHeight +
                    ", mapWidth=" + mapWidth +
                    '}';
        }
    }
}
