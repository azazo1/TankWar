package com.azazo1.online.msg;

import com.azazo1.Config;
import com.azazo1.online.server.toclient.TalkingRoom;
import org.jetbrains.annotations.NotNull;

/**
 * 已注册的客户端向服务端发送说话内容
 *
 * @apiNote 未注册的客户端不被允许说话
 */
public class TalkMsg extends MsgBase {
    public final String content;

    public TalkMsg(@NotNull String content) {
        super();
        if (content.length() > Config.TALKING_WORDS_LIMIT) {
            throw new IllegalArgumentException("content can't be too long.(max: %d characters)".formatted(Config.TALKING_WORDS_LIMIT));
        } else {
            this.content = content;
        }
    }

    /**
     * 服务端向客户端广播某个客户端发送的消息
     */
    public static final class TalkBroadcastMsg extends TalkMsg {
        public final TalkingRoom.TalkContent content;

        public TalkBroadcastMsg(TalkingRoom.@NotNull TalkContent content) {
            super(content.content);
            this.content = content;
        }
    }

    /**
     * 服务端告知客户端说话情况
     * code: 0表示失败, 1表示成功
     */
    public static final class TalkResponseMsg extends MsgBase {
        public TalkResponseMsg(int code) {
            super(code);
        }
    }
}
