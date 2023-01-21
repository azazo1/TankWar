package com.azazo1.game.item;

import com.azazo1.game.bullet.BunshinBullet;
import com.azazo1.game.tank.TankBase;
import com.azazo1.util.Tools;

public class BunshinBulletItem extends ItemBase {
    public static final String imgFile = "img/BunshinBulletItem.png";

    {
        img = Tools.loadImg(imgFile);
    }

    @Override
    public void finishAndEffect(TankBase tank) {
        super.finishAndEffect(tank);
        tank.fireModule.setNextBullet(BunshinBullet.class);
    }

    public BunshinBulletItem(int centerX, int centerY) {
        super(centerX, centerY);
    }
}
