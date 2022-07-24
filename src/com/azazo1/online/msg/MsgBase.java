package com.azazo1.online.msg;

import com.azazo1.util.Tools;

import java.io.Serializable;

/**
 * 游戏中的消息基类
 */
public abstract class MsgBase implements Serializable {
    /**
     * 创建时间
     */
    public final long createdTime = Tools.getRealTimeInMillis();
    /**
     * 此消息代码, 用于子类判断消息详情, 需注意 -1 表示无效代码, 但不代表无效消息
     */
    public final int code;
    
    public MsgBase() {
        this(-1);
    }
    
    public MsgBase(int code) {
        this.code = code;
    }
}
