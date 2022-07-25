package com.azazo1.game.session;

import com.azazo1.Config;
import com.azazo1.online.server.toclient.Server;

import java.util.HashMap;

/**
 * 服务端一局游戏的配置, 由 {@link Server} 管理
 */
public class ServerSessionConfig {
    /**
     * 坦克的:
     * seq -> name
     */
    private final HashMap<Integer, String> tanks = new HashMap<>();
    private volatile String wallMapFile = null;
    
    public ServerSessionConfig() {
    }
    
    public HashMap<Integer, String> getTanks() {
        return tanks;
    }
    
    public String getWallMapFile() {
        return wallMapFile;
    }
    
    /**
     * 墙图文件在 Jar 内的路径
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
}