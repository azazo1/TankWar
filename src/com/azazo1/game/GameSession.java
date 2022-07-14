package com.azazo1.game;

import com.azazo1.Config;
import com.azazo1.game.bullet.BulletGroup;
import com.azazo1.game.tank.TankBase;
import com.azazo1.game.tank.TankGroup;
import com.azazo1.game.wall.WallGroup;
import com.azazo1.util.Tools;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * 此类用于管理一局游戏
 */
public abstract class GameSession {
    protected int totalTankNum = 0; // 总共的坦克数量
    protected GameMap gameMap;
    protected Timer timer;
    protected FrameListener listener;
    
    protected GameSession() {
        gameMap = new GameMap();
        gameMap.setSize(Config.MAP_WIDTH, Config.MAP_HEIGHT);
    }
    
    public void setFrameListener(FrameListener frameListener) {
        listener = frameListener;
    }
    
    public GameMap getGameMap() {
        return gameMap;
    }
    
    /**
     * 开启游戏事件调度
     */
    public void start() {
        timer = new Timer((int) (1000.0 / Config.FPS), (e) -> {
            gameMap.update(null);
            if (listener != null) {
                listener.tick(gameMap.getTankGroup().getLivingTankNum(), gameMap.getBulletGroup().getBulletNum());
            }
        });
        timer.setRepeats(true);
        timer.start();
    }
    
    /**
     * 停止游戏事件调度, 获得游戏信息
     */
    public GameMap.GameInfo stop() {
        timer.stop();
        return gameMap.getInfo();
    }
    
    /**
     * 根据特定条件判断游戏进程是否结束(如:有坦克胜利则则为结束)<br>
     * 一般此方法由 {@link FrameListener} 调用检查游戏进程
     */
    public abstract boolean isOver();
    
    public int getTotalTankNum() {
        return totalTankNum;
    }
    
    
    /**
     * 游戏回调函数
     */
    public interface FrameListener {
        /**
         * 每次帧刷新({@link GameMap#update(Graphics)})都要被调用
         *
         * @param tankNum   现存坦克数量
         * @param bulletNum 现在的子弹数量
         * @apiNote 记得调用 {@link Tools#tickFrame()} 来推动游戏进程
         */
        void tick(int tankNum, int bulletNum);
    }
    
    public static class LocalSession extends GameSession {
        protected LocalSession() {
            super();
        }
        
        /**
         * 创建一局本地游戏会话
         *
         * @param tankNum 坦克(玩家)数量
         * @param wallMap 游戏墙图文件, 用于读取产生 {@link WallGroup}
         */
        public static @NotNull LocalSession createLocalSession(int tankNum, File wallMap) throws IOException {
            LocalSession session = new LocalSession();
            
            TankGroup tankG = new TankGroup();
            session.totalTankNum = tankNum;
            for (int i = 0; i < tankNum; i++) {
                tankG.addTank(new TankBase());
            }
            session.gameMap.setTankGroup(tankG);
            
            WallGroup wallG = WallGroup.parseFromBitmap(ImageIO.read(wallMap));
            session.gameMap.setWallGroup(wallG);
            
            session.gameMap.setBulletGroup(new BulletGroup());
            
            return session;
        }
        
        @Override
        public boolean isOver() {
            return gameMap.getTankGroup().getLivingTankNum() <= 1;
        }
    }
}
