package com.azazo1.online.msg;


/**
 * 房主请求游戏开始
 */
public class ReqGameStartMsg extends MsgBase {
    public ReqGameStartMsg() {
        super();
    }

    public static final class ReqGameStartMsgResponseMsg extends MsgBase {
        public final boolean successful;

        public ReqGameStartMsgResponseMsg(boolean successful) {
            super();
            this.successful = successful;
        }
    }
}
