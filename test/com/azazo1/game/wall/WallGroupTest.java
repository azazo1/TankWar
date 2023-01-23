package com.azazo1.game.wall;

import com.azazo1.util.Tools;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class WallGroupTest {
    
    public static void main(String[] args) throws IOException {
        WallGroup g = WallGroup.parseFromBitmap(ImageIO.read(Tools.getFileURL("wallmap/WallMap.mwal").url()), 500, 500);
        WallGroup g1 = WallGroup.parseFromWallExpression("110\n011", 500, 500, 3);
        Tools.logLn(""+g);
        Tools.logLn(""+g1);
    }
    
    @Test
    void setAlpha() {
        // 设置墙图对应的四叉树深度
        try {
            File file = new File("D:\\Program_Projects\\Java_Projects\\TankWar\\res\\wallmap\\TestMap.mwal"); // 这里不修改 Jar 内文件, 不用 Tools.getFileURL
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