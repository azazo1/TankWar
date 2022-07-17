package com.azazo1.online.server;

import com.azazo1.game.GameSession;
import com.azazo1.online.server.bullet.ServerBulletGroup;
import com.azazo1.online.server.tank.ServerTank;
import com.azazo1.online.server.tank.ServerTankGroup;
import com.azazo1.online.server.wall.ServerWallGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class ServerSession extends GameSession {
    protected ServerSession() {
        super();
    }
    
    /**
     * 创建一局在线服务端游戏会话<br>
     * 该方法将会在所有玩家都加入游戏并注册后
     *
     * @param tankNum   坦克(玩家)数量
     * @param tankNames 各个坦克名称, 但如果不为 null, 其长度要符合 tankNum
     * @param wallMap   游戏墙图文件, 用于读取产生 {@link ServerWallGroup}
     */
    public static @NotNull ServerSession createServerSession(int tankNum, @Nullable String[] tankNames, @NotNull File wallMap) throws IOException {
        GameSession.clearInstance();
        ServerSession session = new ServerSession();
        
        ServerWallGroup wallG = ServerWallGroup.parseFromBitmap(ImageIO.read(wallMap));
        session.gameMap.setWallGroup(wallG);
        
        session.gameMap.setBulletGroup(new ServerBulletGroup());
        
        ServerTankGroup tankG = new ServerTankGroup();
        session.gameMap.setTankGroup(tankG);
        session.totalTankNum = tankNum;
        for (int i = 0; i < tankNum; i++) {
            ServerTank tank = new ServerTank();
            if (tankNames != null) {
                tank.setName(tankNames[i]);
            }
            tankG.addTank(tank);
            tank.randomlyTeleport(); // 要在其他内容都设置完毕后调用
        }
        return session;
    }
    
    @Override
    public boolean isOver() {
        return gameMap.getTankGroup().getLivingTankNum() <= 1;
    }
    
    @Override
    public void start() {
        timer = new Timer(0, (e) -> {
            gameMap.update(null);
            if (listener != null) {
                listener.tick(gameMap.getTankGroup().getTanksInfo(), gameMap.getBulletGroup().getBulletNum());
            }
        });
        timer.setRepeats(true);
        timer.start();
    }
}
