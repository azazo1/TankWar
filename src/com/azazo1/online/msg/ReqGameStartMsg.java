package com.azazo1.online.msg;


import org.intellij.lang.annotations.MagicConstant;

/**
 * 房主请求游戏开始
 */
public class ReqGameStartMsg extends MsgBase {
    /**
     * 启动游戏操作结果: 成功
     */
    public static final int START_GAME_SUCCESSFULLY = 0;
    /**
     * 启动游戏操作结果: 失败, 游戏玩家不足
     */
    public static final int START_GAME_NOT_ENOUGH_PLAYER = 1;
    /**
     * 启动游戏操作结果: 失败, 没有对应的游戏墙图文件
     */
    public static final int START_GAME_NO_WALL_MAP_FILE = 2;
    /**
     * 启动游戏操作结果: 失败, 服务器状态不是 等待 状态
     */
    public static final int START_GAME_INCORRECT_STATE = 3;
    /**
     * 启动游戏操作结果: 失败, 不是房主
     */
    public static final int START_GAME_NOT_HOST = 4;

    public ReqGameStartMsg() {
        super();
    }

    public static final class ReqGameStartMsgResponseMsg extends MsgBase {
        @MagicConstant(intValues = {START_GAME_NOT_HOST, START_GAME_NOT_ENOUGH_PLAYER, START_GAME_SUCCESSFULLY, START_GAME_NO_WALL_MAP_FILE, START_GAME_INCORRECT_STATE})
        public final int rst;

        public ReqGameStartMsgResponseMsg(int rst) {
            super();
            this.rst = rst;
        }
    }
}
