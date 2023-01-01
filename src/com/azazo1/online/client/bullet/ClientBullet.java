package com.azazo1.online.client.bullet;

import com.azazo1.game.bullet.BulletBase;

public class ClientBullet extends BulletBase {

    public ClientBullet(int seq, int centerX, int centerY, double orientation) {
        super(seq, centerX, centerY, orientation);
        getSeqModule().init(); // 禁用 seq 管理模组
    }

    public ClientBullet(int centerX, int centerY, double orientation, int speed) {
        super(centerX, centerY, orientation, speed);
        getSeqModule().init(); // 禁用 seq 管理模组
    }

    public ClientBullet(int centerX, int centerY, double orientation) {
        super(centerX, centerY, orientation);
        getSeqModule().init(); // 禁用 seq 管理模组
    }
}
