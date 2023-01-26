package com.azazo1.game.tank.robot.action;

import com.azazo1.game.tank.robot.RobotTank;
import org.jetbrains.annotations.NotNull;

/**
 * 改变 TWR 方向直到贴近目标方向
 */
public class ChangeOrientationAction extends Action {
    protected final double targetOrientation;

    public ChangeOrientationAction(double targetOrientation) {
        while (targetOrientation < 0) { // 调整到正数区间
            targetOrientation += Math.PI * 2;
        }
        this.targetOrientation = targetOrientation % (2 * Math.PI); // 调整到 [0, 2 * pi]
    }

    @Override
    public boolean take(@NotNull RobotTank twr) {
        double delta = targetOrientation - twr.orientationModule.getOrientation();
        if (Math.abs(delta) < Math.PI / 7) {
            twr.setTurningKeys(false, false);
            return true;
        }
        if (delta > Math.PI) {
            twr.setTurningKeys(true, false);
        } else if (delta < -Math.PI) {
            twr.setTurningKeys(false, true);
        } else if (delta > 0) {
            twr.setTurningKeys(false, true);
        } else {
            twr.setTurningKeys(true, false);
        }
        return false;
    }
}
