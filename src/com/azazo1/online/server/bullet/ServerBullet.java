package com.azazo1.online.server.bullet;

import com.azazo1.game.bullet.BulletBase;
import com.azazo1.online.msg.MsgBase;
import com.azazo1.online.server.toclient.ClientHandler;
import com.azazo1.online.server.toclient.DataTransfer;

import java.awt.*;

public class ServerBullet extends BulletBase {
    {
        doPaint.set(false);
    }
    /**
     * 本构造方法由反射调用 {@link ClientHandler#handleOnGaming(DataTransfer, MsgBase)}
     * */
    public ServerBullet(int centerX, int centerY, double orientation) {
        super(centerX, centerY, orientation);
    }

    @Override
    public void update(Graphics graphics) {
        lifeModule.updateLife();
        if (!finished.get()) {
            Rectangle bakRect = new Rectangle(rect);
            rect.translate((int) (speed.get() * Math.cos(orientation.get())), (int) (speed.get() * Math.sin(orientation.get())));
            if (reflectionModule.updateReflection(bakRect)) {
                rect.setLocation(bakRect.x, bakRect.y);
            }
        }
    }

    public ServerBullet(int seq, int centerX, int centerY, double orientation) {
        super(seq, centerX, centerY, orientation);
    }

    public ServerBullet(int centerX, int centerY, double orientation, int speed) {
        super(centerX, centerY, orientation, speed);
    }
    // 服务端子弹不从客户端同步
}
