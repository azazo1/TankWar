package com.azazo1.online.server.tank;

import com.azazo1.game.tank.TankBase;
import com.azazo1.game.tank.TankGroup;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerTankGroup extends TankGroup {
    @Override
    public void update(Graphics g) {
        if (tanks.isEmpty()) {
            return;
        }
        for (TankBase tank : tanks.values()) {
            if (tank.getEnduranceManager().isDead()) {
                if (livingTanks.contains(tank.getSeq())) {
                    tankDeathSequence.add(tank.getSeq());
                    livingTanks.remove(tank.getSeq());
                }
                continue;
            }
            tank.update(null); // 更新坦克
        }
    }

    public int getLivingPlayerAmount() {
        AtomicInteger amount = new AtomicInteger();
        getLivingTanks().forEach((tankBase) -> {
            if (tankBase instanceof ServerPlayerTank) {
                amount.addAndGet(1);
            }
        });
        return amount.get();
    }
}
