package com.azazo1.game.bullet;

import com.azazo1.util.AtomicDouble;
import com.azazo1.util.Tools;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Random;

/**
 * 分身子弹, 碰到墙有一定概率分身, 但所有分身都会在同一时间消失(除非有的分身撞墙太多了/击中坦克)
 */
public class BunshinBullet extends BulletBase {
    public static final String imgFile = "img/BunshinBullet.png";
    protected final AtomicDouble theta = new AtomicDouble(0); // 不断递增用于形成旋转效果
    public final int maxBunshinDepth = 3; // 最大分身深度
    protected final int depth; // 不断递增用于形成旋转效果

    {
        rawImg = Tools.loadImg(imgFile);
        existingTime.set(2000);
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
                            if (depth >= maxBunshinDepth) {
                                return;
                            }
                            bulletGroup.addBullet(
                                    new BunshinBullet((int) rect.getCenterX(), (int) rect.getCenterY(),
                                            rawOrientation + Math.PI / 3, lifeModule.createdTime.get(), depth+1)
                            );
                        }
                    });
                    return true;
                }
                return false;
            }
        };
    }

    @Override
    protected void paint(Graphics graphics) {
        double rawOrientation = orientation.get();
        orientation.set(rawOrientation + theta.getAndSet(theta.get() + 15 * Math.PI / 360)); // 旋转效果
        super.paint(graphics);
        orientation.set(rawOrientation);
    }

    public BunshinBullet(int centerX, int centerY, double orientation) {
        super(centerX, centerY, orientation);
        depth = 0;
    }

    /**
     * @param depth 要创建的子弹所处 分身深度
     */
    public BunshinBullet(int centerX, int centerY, double orientation, long createdTime, int depth) {
        super(centerX, centerY, orientation);
        lifeModule.createdTime.set(createdTime);
        this.depth = depth;
    }

}
