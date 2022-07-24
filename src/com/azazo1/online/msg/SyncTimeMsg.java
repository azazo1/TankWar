package com.azazo1.online.msg;

/**
 * 客户端向服务端请求时间同步的消息
 *
 * @apiNote 发起者: 客户端
 */
public final class SyncTimeMsg extends MsgBase {
    public SyncTimeMsg() {
        super();
    }
    
    /**
     * 服务端向客户端发送的回复消息
     *
     * @apiNote 发起者: 服务端
     */
    public static final class SyncTimeResponseMsg extends MsgBase {
        public SyncTimeResponseMsg() {
            super();
        }
    }
}
