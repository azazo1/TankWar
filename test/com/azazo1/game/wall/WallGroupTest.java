package com.azazo1.game.wall;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class WallGroupTest {
    
    public static void main(String[] args) throws IOException {
        WallGroup g = WallGroup.parseFromBitmap(ImageIO.read(new File("res/WallMap.mwal")), 500, 500);
        WallGroup g1 = WallGroup.parseFromWallExpression("110\n011", 500, 500, 3);
        System.out.println(g);
        System.out.println(g1);
    }
    
    @Test
    void setAlpha() {
        try {
            File file = new File("res/WallMap.mwal");
            int depth = 3;
            WallGroup.setBitmapQTreeDepth(file, depth);
            
            BufferedImage img = ImageIO.read(file);
            assertEquals(depth, 255 - img.getData().getPixel(0, 0, (int[]) null)[3]);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }
}