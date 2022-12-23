package com.azazo1.online.msg;

/**
 * 玩家切换按键是否按下的信息，用于坦克移动（不包括射击）
 */
public class KeyPressChangeMsg extends MsgBase {
    public final boolean leftTurningKeyPressed;
    public final boolean rightTurningKeyPressed;
    public final boolean forwardGoingKeyPressed;
    public final boolean backwardGoingKeyPressed;

    public KeyPressChangeMsg(boolean left, boolean right, boolean forward, boolean backward) {
        super();
        leftTurningKeyPressed = left;
        rightTurningKeyPressed = right;
        forwardGoingKeyPressed = forward;
        backwardGoingKeyPressed = backward;
    }

}
