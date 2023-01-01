package com.azazo1.online.msg;

import com.azazo1.game.session.ServerGameSessionIntro;

/**
 * 服务器通知各个玩家（包括房主）游戏开始
 */
public class GameStartMsg extends MsgBase {
    /**
     * 当局游戏配置
     */
    public final ServerGameSessionIntro intro;

    public GameStartMsg(ServerGameSessionIntro intro) {
        super();
        this.intro = intro;
    }
}
