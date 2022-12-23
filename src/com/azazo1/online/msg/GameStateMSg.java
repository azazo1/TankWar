package com.azazo1.online.msg;

import com.azazo1.online.server.GameState;
import com.azazo1.online.server.toclient.Server;

/**
 * 服务器在 {@link Server#GAMING} 时不断广播游戏状态的消息
 */
public class GameStateMSg extends MsgBase {

    public GameStateMSg(GameState state) {
        super();
    }
}
