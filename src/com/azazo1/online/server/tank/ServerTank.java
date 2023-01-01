package com.azazo1.online.server.tank;

import com.azazo1.game.tank.TankBase;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class ServerTank extends TankBase {
    // todo 设置坦克开火时发射 ServerBullet
    {
        doPaint.set(false);
    }

    @Override
    public void update(Graphics g) {
        if (enduranceModule.isDead()) {
            throw new IllegalStateException(this + " has died.");
        }
        // 检测碰撞并更新位置状态
        collisionAndMotionModule.updateMotion();
        // 更新弹夹状态
        fireModule.updateFireState();
        // 更新生命状态
        enduranceModule.updateLivingTime();
    }

    /**
     * 改变按键按下状态
     */
    public void keyChange(boolean left, boolean right, boolean forward, boolean backward) {
        leftTurningKeyPressed.set(left);
        rightTurningKeyPressed.set(right);
        forwardGoingKeyPressed.set(forward);
        backwardGoingKeyPressed.set(backward);
    }

    public ServerTank(int seq) {
        super(seq);
        setActionKeyMap(null);
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
