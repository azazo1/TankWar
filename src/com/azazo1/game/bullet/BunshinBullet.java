package com.azazo1.game.bullet;

import com.azazo1.util.Tools;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Random;

/**
 * 分身子弹, 碰到墙有一定概率分身, 但所有分身都会在同一时间消失(除非有的分身撞墙太多了)
 */
public class BunshinBullet extends BulletBase {
    public static final String imgFile = "img/BunshinBullet.png";

    {
        rawImg = Tools.loadImg(imgFile);
        existingTime.set(1500);
    }

    {
        reflectionModule = new ReflectionModule() {
            @Override
            public boolean updateReflection(@NotNull Rectangle bakRect) {
                double rawOrientation = orientation.get();
                if (super.updateReflection(bakRect)) {
                    // 复制
                    EventQueue.invokeLater(() -> {
                        synchronized (bulletGroup.bullets) {
                            if (new Random().nextDouble() > 0.65) { // 复制有一定概率, 不然太多了
                                return;
                            }
                            bulletGroup.addBullet(
                                    new BunshinBullet((int) rect.getCenterX(), (int) rect.getCenterY(),
                                            rawOrientation + Math.PI / 3, lifeModule.createdTime.get())
                            );
                        }
                    });
                    return true;
                }
                return false;
            }
        };
    }

    public BunshinBullet(int centerX, int centerY, double orientation) {
        super(centerX, centerY, orientation);
    }

    public BunshinBullet(int centerX, int centerY, double orientation, long createdTime) {
        super(centerX, centerY, orientation);
        lifeModule.createdTime.set(createdTime);
    }

}
