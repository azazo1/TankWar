package com.azazo1.online.msg;

/**
 * 客户端向服务器请求客户端序号消息
 */
public final class FetchSeqMsg extends MsgBase {
    public FetchSeqMsg() {
        super();
    }
    
    public static final class FetchSeqResponseMsg extends MsgBase {
        public final int seq;
        
        public FetchSeqResponseMsg(int seq) {
            super();
            this.seq = seq;
        }
    }
}
