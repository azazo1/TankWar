package com.azazo1.online.msg;

/**
 * 客户端向服务器申请获取游戏基本信息 (非 GameInfo (-> 游戏结局信息)) 的消息
 */
public class FetchGameIntroMsg extends MsgBase {
    public FetchGameIntroMsg() {
        super();
    }
    
    public static final class FetchGameIntroResponseMsg extends MsgBase {
        public final String wallMapFilePath;
        
        /**
         * @param wallMapFilePath 墙图文件在 Jar 内的路径
         */
        public FetchGameIntroResponseMsg(String wallMapFilePath) {
            super();
            this.wallMapFilePath = wallMapFilePath;
        }
    }
}
