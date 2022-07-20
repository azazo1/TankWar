import com.azazo1.Config;
import com.azazo1.game.GameMap;
import com.azazo1.game.bullet.BulletBase;
import com.azazo1.game.bullet.BulletGroup;
import com.azazo1.game.tank.TankBase;
import com.azazo1.game.tank.TankGroup;
import com.azazo1.game.wall.WallGroup;
import com.azazo1.util.Tools;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.IOException;

import static com.azazo1.util.Tools.resizeFrame;

class BulletTest {
    public static void main(String[] args) {
        GameMap map = new GameMap();
        map.setSize(Config.MAP_WIDTH, Config.MAP_HEIGHT);
        
        TankBase tank = new TankBase();
        TankBase tank2 = new TankBase();
        tank2.turn(Math.toRadians(45));
        tank2.go(50);
        tank2.turn(Math.toRadians(-45));
        tank2.go(50);
        TankGroup tankG = new TankGroup();
        
        WallGroup wallG;
        try {
            wallG = WallGroup.parseFromBitmap(ImageIO.read(Tools.getFileURL("wallmap/WallMap.mwal")), map.getWidth(), map.getHeight());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BulletGroup bulletG = new BulletGroup();
        
        bulletG.addBullet(new BulletBase(50, 50, Math.toRadians(45), 2)); // test Bullet
        tankG.addTank(tank);
        tankG.addTank(tank2);
        map.setTankGroup(tankG);
        map.setWallGroup(wallG);
        map.setBulletGroup(bulletG);
        
        JFrame frame = new JFrame();
        // GameMap 居中
        Box hBox = Box.createHorizontalBox();
        Box vBox = Box.createVerticalBox();
        vBox.add(Box.createHorizontalStrut(Config.MAP_WIDTH));
        hBox.add(Box.createVerticalStrut(Config.MAP_HEIGHT));
        hBox.add(vBox);
        vBox.add(map);
        frame.add(hBox);
        
        Timer t = new Timer((int) (1.0 / Config.FPS * 1000), (listener) -> {
            map.update(null);
            Tools.tickFrame();
        });
        t.setRepeats(true);
        t.start();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        resizeFrame(frame, Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);
        map.requestFocus();
        frame.setVisible(true);
    }
}