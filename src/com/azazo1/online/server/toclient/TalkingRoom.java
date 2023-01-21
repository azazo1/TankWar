package com.azazo1.online.server.toclient;

import com.azazo1.Config;
import com.azazo1.online.msg.TalkMsg;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Vector;

public class TalkingRoom {
    /**
     * 讲话的内容
     */
    public static class TalkContent implements Serializable {
        public final int from_seq;
        public final String content;
        public final String from_name;

        public TalkContent(@NotNull ClientHandler client, String content) {
            from_seq = client.getSeq();
            from_name = client.getName();
            this.content = content;
        }

        public TalkContent(int seq, String name, String content) {
            this.from_seq = seq;
            this.from_name = name;
            this.content = content;
        }

        @Override
        public String toString() {
            return "%s(%d):\"%s\"".formatted(from_name, from_seq, content);
        }
    }

    protected final Vector<TalkContent> whatTheySaid = new Vector<>();

    public TalkingRoom() {
    }

    /**
     * 某个客户端尝试说话
     */
    public boolean talk(@NotNull ClientHandler client, String content) {
        if (client.isPlayer() == null || content.length() > Config.TALKING_WORDS_LIMIT || content.isBlank()) {
            return false;
        }
        TalkContent talkContent = new TalkContent(client, content);
        whatTheySaid.add(talkContent);
        Server.instance.broadcast(new TalkMsg.TalkBroadcastMsg(talkContent), false);
        return true;
    }

    /**
     * 查询倒数 n 条说话内容
     *
     * @apiNote 通常在新客户端新加入时会获取这些内容
     */
    public Vector<TalkContent> query(int n) {
        return new Vector<>(whatTheySaid.subList(Math.max(0, whatTheySaid.size() - n), whatTheySaid.size()));
    }
}
