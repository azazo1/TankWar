package com.azazo1.game.session;

import com.azazo1.Config;
import com.azazo1.online.server.ServerGameMap;
import com.azazo1.online.server.bullet.ServerBulletGroup;
import com.azazo1.online.server.tank.ServerTank;
import com.azazo1.online.server.tank.ServerTankGroup;
import com.azazo1.online.server.toclient.Server;
import com.azazo1.online.server.wall.ServerWallGroup;
import com.azazo1.util.Tools;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.HashMap;

/**
 * 写着写着被我忘记了，其功能直接被我在 {@link Server#initServerHandlerLoop()} 中实现了
 */
@Deprecated
public class ServerSession extends GameSession {
    protected ServerSession() {
        super();
        gameMap = new ServerGameMap();
        gameMap.setSize(Config.MAP_WIDTH, Config.MAP_HEIGHT);
    }

    /**
     * 创建一局在线服务端游戏会话<br>
     * 该方法应在所有玩家都加入游戏并注册后被调用
     *
     * @param config 游戏配置
     */
    public static @NotNull ServerSession createServerSession(@NotNull ServerGameSessionIntro config) throws IOException {
        GameSession.clearInstance();
        ServerSession session = new ServerSession();

        ServerWallGroup wallG = ServerWallGroup.parseFromBitmap(ImageIO.read(Tools.getFileURL(config.getWallMapFile()).url()));
        session.gameMap.setWallGroup(wallG);

        session.gameMap.setBulletGroup(new ServerBulletGroup());

        ServerTankGroup tankG = new ServerTankGroup();
        session.gameMap.setTankGroup(tankG);
        HashMap<Integer, String> tanks = config.getTanks();
        session.totalTankNum.set(tanks.size());
        for (int seq : tanks.keySet()) {
            ServerTank tank = new ServerTank(seq);
            tank.setName(tanks.get(seq));
            tankG.addTank(tank);
            tank.randomlyTeleport(); // 要在其他内容都设置完毕后调用
        }
        return session;
    }

    @Override
    public boolean isOver() {
        return gameMap.getTankGroup().getLivingTankNum() <= 1;
    }
}
