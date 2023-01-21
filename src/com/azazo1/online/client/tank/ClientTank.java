package com.azazo1.online.client.tank;

import com.azazo1.Config;
import com.azazo1.game.bullet.BulletBase;
import com.azazo1.game.tank.TankBase;
import com.azazo1.util.Tools;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 提供更改坦克状态的 api
 */
public class ClientTank extends TankBase {
    public final AtomicInteger rank = new AtomicInteger(-1);
    public final boolean you;

    /**
     * <h3>创建 ClientTank 实例</h3>
     * seq 管理模块被禁用
     *
     * @param you 这个坦克是否是 "你", 即客户端对应的坦克
     */
    public ClientTank(int seq, boolean you) {
        super(seq);
        getSeqModule().init(); // 禁用 seq 管理模块
        setActionKeyMap(null);
        this.you = you;
        enduranceModule = new EnduranceModule() {
            @Override
            public boolean makeAttack(int damage) {
                if (Tools.getFrameTimeInMillis() > Config.TANK_INJURED_INTERVAL_MILLIS + lastInjuredTime.get()) { // 过了受伤间隔
                    endurance.getAndAdd(-damage);
                    lastInjuredTime.set(Tools.getFrameTimeInMillis());
                    // 此处不发声
                    return true;
                }
                if (endurance.get() <= 0) {
                    makeDie();
                }
                return false;
            }
        };
        fireModule = new FireModule() {
            @Override
            public boolean fire(@NotNull Class<? extends BulletBase> T) {
                if (spareBulletNum.get() <= 0) {
                    return false;
                }
                try {
                    Constructor<? extends BulletBase> constructor = T.getConstructor(int.class, int.class, double.class);
                    double orientation = orientationModule.getOrientation();
                    // rect.width 是坦克炮筒所在边
                    int cx = (int) (rect.getCenterX() + Math.cos(orientation) * rect.width);
                    int cy = (int) (rect.getCenterY() + Math.sin(orientation) * rect.width);
                    BulletBase bullet = constructor.newInstance(cx, cy, orientation);

                    spareBulletNum.getAndDecrement();
                    tankGroup.getGameMap().getBulletGroup().addBullet(bullet);
                    // 此处不发声
                    return true;
                } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                         IllegalAccessException e) {
                    throw new RuntimeException(e); // 一般不会到达此处
                }
            }
        };
    }

    public void turnTo(double orientation) {
        orientationModule.setOrientation(orientation);
    }

    public void goTo(@NotNull Rectangle rect) {
        this.rect.setRect(rect);
    }

    public void setEndurance(int nowEndurance) {
        enduranceModule.setEndurance(nowEndurance);
    }

    public void setLivingTime(long livingTime) {
        enduranceModule.setLivingTime(livingTime);
    }

    public void setRank(int rank) {
        this.rank.set(rank);
    }

    @Override
    protected void paint(Graphics g) {
        if (doPaint.get()) {
            Graphics2D g2d = (Graphics2D) g;
            double dx = rect.getCenterX();
            double dy = rect.getCenterY();
            g2d.translate(dx, dy); // 移动初始位置
            Graphics2D g2dBak = (Graphics2D) g2d.create(); // 未旋转的备份

            g2d.rotate(orientationModule.getOrientation());
            // 注意负值使图像中点落在初始位置上, 使旋转锚点正确在图像中央
            g.drawImage(enduranceModule.adjustEnduranceImage(), -rect.width / 2, -rect.height / 2, rect.width, rect.height, null);
            // 在坦克身上标注昵称
            if (you) {
                g2dBak.setColor(Config.TANK_NAME_YOU_COLOR);
            } else {
                g2dBak.setColor(Config.TANK_NAME_COLOR);
            }
            g2dBak.setFont(Config.TANK_SEQ_FONT);
            g2dBak.drawString(name + "", -rect.width / 2, -rect.height / 2);

            // 显示坦克子弹数, 不显示其他坦克的子弹数量
            if (you) {
                g2dBak.setColor(Config.TANK_CLIP_YOU_COLOR);
                g2dBak.setFont(Config.TANK_CLIP_FONT); // 降低字体大小
                g2dBak.drawString(Config.translation.hasBulletChar.repeat(fireModule.getSpareBulletNum()) + Config.translation.emptyBulletChar.repeat(fireModule.getUsedBulletNum()), -rect.width / 4, -rect.height / 4);
            }

        }
    }

    public void setSpareBullet(int spareBulletNum) {
        fireModule.setSpareBulletNum(spareBulletNum);
    }

    /**
     * 设置坦克正在执行的操作
     */
    public void setActions(boolean @NotNull [] pressedActions) {
        leftTurningKeyPressed.set(pressedActions[0]);
        rightTurningKeyPressed.set(pressedActions[1]);
        forwardGoingKeyPressed.set(pressedActions[2]);
        backwardGoingKeyPressed.set(pressedActions[3]);
    }
}
