package com.azazo1.online.msg;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

/**
 * 客户端向服务器注册的消息
 * <p>
 * 客户端可以选择注册为玩家 ({@link #isPlayer} == true), 或旁观者 ({@link #isPlayer} == false)
 * </p>
 */
public final class RegisterMsg extends MsgBase {
    public final boolean isPlayer;
    public final String name;
    
    public RegisterMsg(boolean isPlayer, String name) {
        super();
        this.isPlayer = isPlayer;
        this.name = name;
    }
    
    /**
     * 注册结果
     */
    public static final class RegisterResponseMsg extends MsgBase {
        public static final int PLAYER_MAXIMUM = 1;
        public static final int NAME_OR_SEQ_OCCUPIED = 2;
        public static final int SUCCEED = 0;
        /**
         * 对应的原始消息, 此 {@link RegisterResponseMsg} 是 origin 消息的返回值
         */
        public final RegisterMsg origin;
        
        public RegisterResponseMsg(@MagicConstant(intValues = {PLAYER_MAXIMUM, NAME_OR_SEQ_OCCUPIED, SUCCEED})
                                   int code, @NotNull RegisterMsg origin) {
            super(code);
            this.origin = origin;
        }
    }
}
