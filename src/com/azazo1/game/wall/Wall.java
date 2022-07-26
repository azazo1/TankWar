package com.azazo1.game.wall;

import com.azazo1.base.CharWithRectangle;
import com.azazo1.util.Tools;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class Wall implements CharWithRectangle {
    public static final BufferedImage img;
    
    static {
        try {
            img = ImageIO.read(Tools.getFileURL("img/SingleWall.png").url());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected final AtomicBoolean doPaint = new AtomicBoolean(true); // 是否显示, 服务端子类设为 false
    protected final Rectangle rect = new Rectangle(0, 0, img.getWidth(), img.getHeight());
    
    /**
     * @param startX 左上角 x 坐标
     * @param startY 左上角 y 坐标
     */
    public Wall(int startX, int startY) {
        rect.translate(startX, startY);
    }
    
    /**
     * @param startX 左上角 x 坐标
     * @param startY 左上角 y 坐标
     */
    public Wall(int startX, int startY, int width, int height) {
        rect.translate(startX, startY);
        rect.setSize(width, height);
    }
    
    public void update(Graphics graphics) {
        paint(graphics);
    }
    
    public void paint(@NotNull Graphics graphics) {
        // 进行缩放
        if (doPaint.get()) {
            graphics.drawImage(img, rect.x, rect.y, (int) rect.getWidth(), (int) rect.getHeight(), null);
        }
    }
    
    public Rectangle getRect() {
        return new Rectangle(rect);
    }
}
