package com.azazo1.game.tank.robot.action;

import com.azazo1.game.tank.robot.RobotTank;
import com.azazo1.game.tank.robot.WayPoint;
import org.jetbrains.annotations.NotNull;

/**
 * 控制 TWR 一直前行到距离目的路径点一定距离
 */
public class GoingAction extends Action {
    protected final WayPoint targetPoint;
    protected final double distance;

    public GoingAction(WayPoint targetPoint, double distance) {
        this.targetPoint = targetPoint;
        this.distance = distance;
    }

    @Override
    public boolean take(@NotNull RobotTank twr) {
        if (targetPoint.distanceTo((int) twr.getRect().getCenterX(), (int) twr.getRect().getCenterY()) < distance) {
            twr.setGoingKeys(false, false);
            return true;
        }
        twr.setGoingKeys(true, false);
        return false;
    }
}
