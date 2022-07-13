package com.azazo1.game.wall;

import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Wall {
    public static final BufferedImage img;
    
    static {
        try {
            img = ImageIO.read(new File("res/SingleWall.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
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
        graphics.drawImage(img, rect.x, rect.y, (int) rect.getWidth(), (int) rect.getHeight(), null);
    }
    
    public Rectangle getRect() {
        return new Rectangle(rect);
    }
}
