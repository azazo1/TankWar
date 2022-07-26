package com.azazo1.online.msg;

import com.azazo1.game.session.ServerGameSessionIntro;
import com.azazo1.online.server.toclient.Server;

/**
 * 房主客户端向服务器请求更改单局游戏配置的消息
 * <p>此消息只能在 {@link Server#WAITING} 中起作用, 且 {@link #intro} 的 {@link ServerGameSessionIntro} 的 tanks 不会起作用
 * <p>此消息没有返回值, 客户端要通过判断自身是否是房主来知晓是否操作成功, 操作结果可以通过 {@link FetchGameIntroMsg} 查看
 */
public class PostGameIntroMsg extends MsgBase {
    public final ServerGameSessionIntro intro;
    
    public PostGameIntroMsg(ServerGameSessionIntro intro) {
        super();
        this.intro = intro;
    }
}
