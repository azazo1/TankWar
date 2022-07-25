package com.azazo1.online.msg;

import com.azazo1.online.server.toclient.ClientHandler;

import java.util.Vector;

/**
 * 客户端向服务器查询连接到服务器的所有客户端的消息
 */
public class QueryClientsMsg extends MsgBase {
    public QueryClientsMsg() {
        super();
    }
    
    public static final class QueryClientsResponseMsg extends MsgBase {
        public final Vector<ClientHandler.ClientHandlerInfo> multiInfo;
        
        public QueryClientsResponseMsg(Vector<ClientHandler.ClientHandlerInfo> clientsInfo) {
            super();
            multiInfo = clientsInfo;
        }
    }
    
}
