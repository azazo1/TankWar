package com.azazo1.online.msg;


import com.azazo1.online.server.toclient.ClientHandler;

import java.util.HashMap;

/**
 * 房主请求游戏开始
 */
public class ReqGameStartMsg extends MsgBase {
    public ReqGameStartMsg() {
        super();
    }

    public static final class ReqGameStartMsgResponse extends MsgBase {
        public final boolean successful;

        public ReqGameStartMsgResponse(boolean successful) {
            super();
            this.successful = successful;
        }
    }
}
