package com.azazo1.online.msg;

import java.util.Vector;

/**
 * 客户端向服务器查询连接到服务器的所有客户端的消息
 */
public class QueryClientsMsg extends MsgBase {
    public QueryClientsMsg() {
        super();
    }
    
    public static final class QueryClientsResponseMsg extends MsgBase {
        public QueryClientsResponseMsg() {
            super();
        }
    }
    
}
