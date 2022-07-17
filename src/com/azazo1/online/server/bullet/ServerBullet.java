package com.azazo1.online.server.bullet;

import com.azazo1.game.bullet.BulletBase;

public class ServerBullet extends BulletBase {
    {
        doPaint.set(false);
    }
    
    public ServerBullet(int seq, int centerX, int centerY, double orientation) {
        super(seq, centerX, centerY, orientation);
    }
    
    public ServerBullet(int centerX, int centerY, double orientation, int speed) {
        super(centerX, centerY, orientation, speed);
    }
    // 服务端子弹不从客户端同步
}
