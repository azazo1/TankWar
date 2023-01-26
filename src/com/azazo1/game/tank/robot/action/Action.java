package com.azazo1.game.tank.robot.action;

import com.azazo1.game.tank.robot.RobotTank;

/**
 * TWR 做的各种动作
 */
public abstract class Action {
    /**
     * 执行操作<br>
     * {@link Action#take(RobotTank)} 返回 false 后, {@link Action#take(RobotTank)} 稍后会再次被调用, 直到返回 true
     *
     * @return true: 此 Action 生命结束, false: 此 Action 生命未结束
     */
    public abstract boolean take(RobotTank twr);
}
