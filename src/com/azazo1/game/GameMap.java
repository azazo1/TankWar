package com.azazo1.game;

import com.azazo1.Config;
import com.azazo1.game.bullet.BulletGroup;
import com.azazo1.game.tank.TankGroup;
import com.azazo1.game.wall.WallGroup;
import com.azazo1.util.Tools;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.font.TextLayout;

/**
 * 游戏画面
 */
public class GameMap extends Canvas {
    protected TankGroup tankGroup;
    protected WallGroup wallGroup;
    protected BulletGroup bulletGroup;
    
    public GameMap() {
        super();
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
        Image buffer = createImage(getWidth(), getHeight()); // 二级缓冲
        Graphics bufferGraphics = buffer.getGraphics();
        Graphics2D g2d = (Graphics2D) bufferGraphics;
        bufferGraphics.setColor(Config.BACKGROUND_COLOR);
        bufferGraphics.fillRect(0, 0, getWidth(), getHeight());
        
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
}