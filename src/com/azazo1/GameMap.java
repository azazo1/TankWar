package com.azazo1;

import com.azazo1.base.Config;
import com.azazo1.bullet.BulletGroup;
import com.azazo1.tank.TankGroup;
import com.azazo1.util.Tools;
import com.azazo1.wall.WallGroup;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameMap extends Canvas {
    protected AtomicBoolean firstFocused = new AtomicBoolean(false); // 是否被聚焦过(用于提示用户点击来聚焦)
    protected TankGroup tankGroup;
    protected WallGroup wallGroup;
    protected BulletGroup bulletGroup;
    
    public GameMap() {
        super();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                firstFocused.set(true);
            }
        });
    }
    
    @Override
    public void update(Graphics g) {
        setSize(Config.MAP_WIDTH, Config.MAP_HEIGHT);
        if (g == null) {
            g = getGraphics();
        }
        paint(g);
        Tools.tickFrame();
    }
    
    @Override
    public void paint(Graphics g) {
        Image buffer = createImage(getWidth(), getHeight()); // 二级缓冲
        Graphics bufferGraphics = buffer.getGraphics();
        bufferGraphics.setColor(Config.BACKGROUND_COLOR);
        bufferGraphics.fillRect(0, 0, getWidth(), getHeight());
        
        getTankGroup().update(bufferGraphics.create()); // 传入副本
        getWallGroup().update(bufferGraphics.create()); // 传入副本
        getBulletGroup().update(bufferGraphics.create()); // 传入副本
        if (!firstFocused.get()) {
            bufferGraphics.setColor(Config.TEXT_COLOR);
            char[] c = "Click Here To Focus".toCharArray();
            bufferGraphics.drawChars(c, 0, c.length, 30, 30);
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
