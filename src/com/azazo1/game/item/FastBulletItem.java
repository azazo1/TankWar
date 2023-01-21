package com.azazo1.game.item;

import com.azazo1.game.bullet.FastBullet;
import com.azazo1.game.tank.TankBase;
import com.azazo1.util.Tools;

public class FastBulletItem extends ItemBase {
    public FastBulletItem(int centerX, int centerY) {
        super(centerX, centerY);
    }

    public static final String imgFile = "img/FastBulletItem.png";

    {
        img = Tools.loadImg(imgFile);
    }

    @Override
    public void finishAndEffect(TankBase tank) {
        super.finishAndEffect(tank);
        tank.fireModule.setNextBullet(FastBullet.class);
    }
}
