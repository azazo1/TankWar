package com.azazo1.game.bullet;

import com.azazo1.game.tank.TankBase;
import com.azazo1.util.Tools;
/**
 * 急速子弹, 飞行速度极快
 * */
public class FastBullet extends BulletBase {
    public static final String imgFile = "img/FastBullet.png";

    {
        rawImg = Tools.loadImg(imgFile);
        setSpeed(10);
    }
    public FastBullet(int centerX, int centerY, double orientation) {
        super(centerX, centerY, orientation);
    }

    @Override
    public void finish(TankBase tank) {
        finish();
    }
}
