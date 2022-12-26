package com.azazo1.online.server.tank;

import com.azazo1.game.tank.TankBase;
import com.azazo1.game.tank.TankGroup;

import java.awt.*;

public class ServerTankGroup extends TankGroup {
    @Override
    public void update(Graphics g) {
        // todo 发送坦克信息
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
            tank.update(null); // 更新坦克, 传入一个副本
        }
    }
}
