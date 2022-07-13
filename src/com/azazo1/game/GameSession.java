package com.azazo1.game;

import com.azazo1.Config;
import com.azazo1.game.bullet.BulletGroup;
import com.azazo1.game.tank.TankBase;
import com.azazo1.game.tank.TankGroup;
import com.azazo1.game.wall.WallGroup;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class GameSession {
    protected GameMap gameMap;
    
    protected GameSession() {
        gameMap = new GameMap();
        gameMap.setSize(Config.MAP_WIDTH, Config.MAP_HEIGHT);
    }
    
    public void start() {
    
    }
    
    
    public static class LocalSession extends GameSession {
        protected LocalSession() {
            super();
        }
        
        public static LocalSession createLocalSession(int tankNum, File wallMap) throws IOException {
            LocalSession session = new LocalSession();
            
            TankGroup tankG = new TankGroup();
            for (int i = 0; i < tankNum; i++) {
                tankG.addTank(new TankBase());
            }
            session.gameMap.setTankGroup(tankG);
            
            WallGroup wallG = WallGroup.parseFromBinaryBitmap(ImageIO.read(wallMap));
            session.gameMap.setWallGroup(wallG);
    
            session.gameMap.setBulletGroup(new BulletGroup());
            
            return null;
        }
    }
}
