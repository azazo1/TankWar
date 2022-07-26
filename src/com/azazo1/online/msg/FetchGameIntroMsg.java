package com.azazo1.online.msg;

import com.azazo1.game.session.ServerGameSessionIntro;

/**
 * 客户端向服务器申请获取单局游戏基本配置 (非 GameInfo (-> 游戏结局信息)) 的消息
 */
public class FetchGameIntroMsg extends MsgBase {
    public FetchGameIntroMsg() {
        super();
    }
    
    public static final class FetchGameIntroResponseMsg extends MsgBase {
        public final ServerGameSessionIntro intro;
        
        /**
         * @param intro 单局游戏配置
         */
        public FetchGameIntroResponseMsg(ServerGameSessionIntro intro) {
            super();
            this.intro = intro;
        }
    }
}
