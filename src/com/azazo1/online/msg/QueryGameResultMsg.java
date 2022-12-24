package com.azazo1.online.msg;

import com.azazo1.game.GameMap;

/**
 * 客户端向服务端查询游戏结果
 */
public class QueryGameResultMsg extends MsgBase {
    public QueryGameResultMsg() {
        super();
    }

    public static final class QueryGameResultResponseMsg extends MsgBase {
        public final GameMap.GameInfo info;

        public QueryGameResultResponseMsg(GameMap.GameInfo info) {
            super();
            this.info = info;
        }
    }
}
