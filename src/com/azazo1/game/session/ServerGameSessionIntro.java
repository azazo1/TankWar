package com.azazo1.game.session;

import com.azazo1.Config;
import com.azazo1.online.server.toclient.Server;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 服务端一局游戏的配置, 由 {@link Server} 管理
 */
public class ServerGameSessionIntro implements Serializable {
    /**
     * 游戏画布尺寸 (只会用到宽和高)
     */
    private final Rectangle mapSize = new Rectangle(0, 0, Config.MAP_WIDTH, Config.MAP_HEIGHT);
    /**
     * 机器人 (TWR) 数量
     */
    private final AtomicInteger robotAmount = new AtomicInteger(0);
    /**
     * 坦克的:
     * seq -> name
     */
    private final HashMap<Integer, String> tanks = new HashMap<>();
    /**
     * 墙图文件
     */
    private volatile String wallMapFile;

    /**
     * 拷贝 {@link ServerGameSessionIntro}, 若参数为 null 则与默认初始化无异
     */
    public ServerGameSessionIntro(@Nullable ServerGameSessionIntro intro) {
        if (intro != null) {
            copyFrom(intro);
        }
    }

    public ServerGameSessionIntro() {
        this(null);
    }

    public void copyFrom(@NotNull ServerGameSessionIntro intro) {
        tanks.clear();
        tanks.putAll(intro.getTanks());
        setRobotAmount(intro.getRobotAmount());
        wallMapFile = intro.getWallMapFile();
        setMapSize(intro.mapSize);
    }

    public void setMapSize(int width, int height) {
        mapSize.setSize(width, height);
    }

    public void setMapSize(@NotNull Rectangle size) {
        setMapSize(size.width, size.height);
    }

    public Rectangle getMapSize() {
        return mapSize;
    }

    public void setRobotAmount(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount can't be less than 0");
        }
        robotAmount.set(amount);
    }

    public int getRobotAmount() {
        return robotAmount.get();
    }

    public HashMap<Integer, String> getTanks() {
        return tanks;
    }

    public String getWallMapFile() {
        return wallMapFile;
    }


    /**
     * 墙图文件在 Jar 内的路径
     * e.g. "wallmap/WallMap.mwal"
     */
    public void setWallMapFile(String filePath) {
        wallMapFile = filePath;
    }

    /**
     * 添加坦克(玩家)
     *
     * @throws IllegalArgumentException 待添加的坦克 seq 或 name 已存在
     * @throws IllegalStateException    玩家数量过多
     */
    public void addTank(int seq, String name) {
        if (tanks.containsKey(seq) || tanks.containsValue(name)) {
            throw new IllegalArgumentException("Information collision.");
        } else if (tanks.size() >= Config.MAX_SERVER_SESSION_PLAYER_NUM) {
            throw new IllegalStateException("Too many players.");
        }
        tanks.put(seq, name);
    }

    /**
     * 移除坦克(玩家)
     */
    public void removeTank(int seq) {
        tanks.remove(seq);
    }

    @Override
    public String toString() {
        return "ServerGameSessionIntro{" +
                "mapSize=" + mapSize +
                ", robotAmount=" + robotAmount +
                ", tanks=" + tanks +
                ", wallMapFile='" + wallMapFile + '\'' +
                '}';
    }
}