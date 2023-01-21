package com.azazo1.game.bullet;

import com.azazo1.game.tank.TankBase;
import com.azazo1.util.Tools;

/**
 * 恢复子弹, 击中坦克可以使其回血
 */
public class RecoverBullet extends BulletBase {
    public static final String imgFile = "img/RecoverBullet.png";

    {
        damage.set(-1); // 恢复的血量
        rawImg = Tools.loadImg(imgFile);
    }

    public RecoverBullet(int centerX, int centerY, double orientation) {
        super(centerX, centerY, orientation);
    }

    @Override
    public void finish(TankBase tank) {
        finish();
    }

}
