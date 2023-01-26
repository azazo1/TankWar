package com.azazo1.game.tank.robot.action;

import com.azazo1.game.tank.robot.RobotTank;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * TWR 连续开火
 */
public class MultipleFireAction extends Action {
    public final int targetTimes;
    public final AtomicInteger nowTimes = new AtomicInteger(0);

    /**
     * @param times 连续开火次数
     */
    public MultipleFireAction(int times) {
        targetTimes = times;
    }

    @Override
    public boolean take(@NotNull RobotTank twr) {
        if (twr.fireModule.fire()) {
            return nowTimes.getAndIncrement() >= targetTimes;
        }
        return false;
    }
}
