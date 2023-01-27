package com.azazo1.game.tank.robot.action;

import com.azazo1.game.tank.robot.RobotTank;
import org.jetbrains.annotations.NotNull;

/**
 * 控制 TWR 一直前行到 {@link RobotTank#getRoute()} 长度小于一定长度, 若 {@link RobotTank#getRoute()} 为 null 则停止行进
 */
public class GoingAction extends Action {
    protected final double distance;

    public GoingAction(double distance) {
        this.distance = distance;
    }

    @Override
    public boolean take(@NotNull RobotTank twr) {
        if (twr.getRoute().getTotalDistance() < distance) {
            twr.setGoingKeys(false, false);
            return true;
        }
        twr.setGoingKeys(true, false);
        return false;
    }
}
