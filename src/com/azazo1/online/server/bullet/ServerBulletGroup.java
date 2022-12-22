package com.azazo1.online.server.bullet;

import com.azazo1.game.bullet.BulletBase;
import com.azazo1.game.bullet.BulletGroup;

import java.awt.*;

public class ServerBulletGroup extends BulletGroup {
    @Override
    public void update(Graphics graphics) {
        for (int i = 0; i < bullets.size(); i++) {
            BulletBase bullet = bullets.get(i);
            if (bullet.isFinished()) {
                removeBullet(bullet);
                i--;
            }
        }
        if (bullets.isEmpty()) {
            return;
        }
        for (BulletBase bullet : bullets) {
            bullet.update(null);
        }
        // todo 发送子弹位置信息
    }
}
