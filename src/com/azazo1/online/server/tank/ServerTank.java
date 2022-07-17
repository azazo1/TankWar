package com.azazo1.online.server.tank;

import com.azazo1.game.tank.TankBase;
import org.jetbrains.annotations.NotNull;

public class ServerTank extends TankBase {
    {
        doPaint.set(false);
    }
    
    public ServerTank() {
        super();
    }
    
    public void adaptFromInfo(@NotNull TankInfo info) {
        if (info.getSeq() != getSeq()) {
            throw new IllegalArgumentException("TankInfo doesn't have the same seq as tank.");
        }
        orientationModule.setOrientation(info.getOrientation());
        rect.setRect(info.getRect());
        // 服务端不从客户端同步血量
    }
}
