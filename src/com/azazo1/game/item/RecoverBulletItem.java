package com.azazo1.game.item;

import com.azazo1.game.bullet.RecoverBullet;
import com.azazo1.game.tank.TankBase;
import com.azazo1.util.Tools;

/**
 * 加血子弹的物品
 */
public class RecoverBulletItem extends ItemBase {
    public static final String imgFile = "img/RecoverBulletItem.png";

    {
        img = Tools.loadImg(imgFile);
    }

    public RecoverBulletItem(int centerX, int centerY) {
        super(centerX, centerY);
    }

    @Override
    public void finishAndEffect(TankBase tank) {
        super.finishAndEffect(tank);
        tank.fireModule.setNextBullet(RecoverBullet.class);
    }
}
