package com.azazo1.online.msg;

import com.azazo1.game.GameMap;

/**
 * 服务器广播游戏结束
 */
public class GameOverMsg extends MsgBase {
    public final GameMap.GameInfo info;

    public GameOverMsg(GameMap.GameInfo info) {
        super();
        this.info = info;
    }
}
