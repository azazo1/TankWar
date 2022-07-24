package com.azazo1.online.msg;

import com.azazo1.util.Tools;

import java.io.Serializable;

/**
 * 游戏中的消息
 */
public class MsgBase implements Serializable {
    public final long createdTime = Tools.getRealTimeInMillis();
    
    public MsgBase() {
    }
}
