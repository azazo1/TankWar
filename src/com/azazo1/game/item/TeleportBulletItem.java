package com.azazo1.game.item;

import com.azazo1.game.bullet.TeleportBullet;
import com.azazo1.game.tank.TankBase;
import com.azazo1.util.Tools;

public class TeleportBulletItem extends ItemBase{
    public static final String imgFile = "img/TeleportBulletItem.png";

    {
        img = Tools.loadImg(imgFile);
    }

    public TeleportBulletItem(int centerX, int centerY) {
        super(centerX, centerY);
    }

    @Override
    public void finishAndEffect(TankBase tank) {
        super.finishAndEffect(tank);
        tank.fireModule.setNextBullet(TeleportBullet.class);
    }
}
