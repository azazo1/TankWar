package com.azazo1.game.tank.robot.action;

import com.azazo1.game.tank.robot.RobotTank;
import com.azazo1.game.tank.robot.Route;
import com.azazo1.game.tank.robot.WayPoint;
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
        Route route = twr.getRoute();
        WayPoint secondPoint = route.getPoint(1);
        double cx = twr.getRect().getCenterX(), cy = twr.getRect().getCenterY();
        double angleToSecondPoint = Math.atan2(secondPoint.y - cy, secondPoint.x - cx);
        while (angleToSecondPoint < 0) { // 调整到正数区间
            angleToSecondPoint += Math.PI * 2;
        }
        // 方向偏移量
        double deltaOrientation = Math.abs(angleToSecondPoint - twr.orientationModule.getOrientation());
        if (deltaOrientation > Math.PI) {
            deltaOrientation = Math.PI * 2 - deltaOrientation; // 转换到 [0, 2*pi]
        }
        if (deltaOrientation > Math.PI * 2 / 3) {
            twr.setGoingKeys(false, true);
        } else if (deltaOrientation > Math.PI / 3 && deltaOrientation < Math.PI * 2 / 3) {
            twr.setGoingKeys(false, false);
        } else {
            twr.setGoingKeys(true, false);
        }
        return false;
    }
}
