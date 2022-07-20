package com.azazo1.game.tank;

import com.azazo1.game.GameMap;
import com.azazo1.game.bullet.BulletGroup;
import com.azazo1.game.wall.WallGroup;
import com.azazo1.util.Tools;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TankBaseTest {
    
    @Test
    void getInfo() {
        TankBase tank = new TankBase();
        TankGroup tankGroup = new TankGroup();
        tankGroup.addTank(tank);
        GameMap map = new GameMap();
        map.setTankGroup(tankGroup);
        map.setSize(100, 100);
        map.setWallGroup(new WallGroup(1));
        map.setBulletGroup(new BulletGroup());
        
        Tools.tickFrame();
        Tools.tickFrame();
        Tools.tickFrame();
        Tools.tickFrame();
        Tools.tickFrame();
        Tools.tickFrame();
        Tools.tickFrame();
        BufferedImage image;
        try {
            image = ImageIO.read(Tools.getFileURL("wallmap/wallmap.mwal"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        tank.update(image.getGraphics());
        TankBase.TankInfo info = tank.getInfo();
        assertEquals(-1, info.getRank()); // info 和 tank 不会再产生数据关联
        info.rank = 100;
        tank.update(image.getGraphics());
        Tools.tickFrame();
        tank.update(image.getGraphics());
        assertNotEquals(100, tank.getInfo().getRank()); // info 和 tank 不会再产生数据关联
    }
}