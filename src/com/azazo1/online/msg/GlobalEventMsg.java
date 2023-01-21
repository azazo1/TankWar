package com.azazo1.online.msg;

/**
 * 服务端向客户端发送全局事件 (坦克发射, 坦克受伤, 坦克随机传送) 消息
 * */
public class GlobalEventMsg extends MsgBase{
    public enum Events {
        TANK_FIRE, TANK_ATTACKED, TANK_RANDOMLY_TELEPORT
    }
    public final Events event;

    public GlobalEventMsg(Events event) {
        this.event = event;
    }
}
