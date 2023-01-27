package com.azazo1.game.tank.robot;

import com.azazo1.Config;
import com.azazo1.game.GameMap;
import com.azazo1.game.bullet.BulletBase;
import com.azazo1.game.tank.TankBase;
import com.azazo1.game.tank.TankGroup;
import com.azazo1.game.wall.Wall;
import com.azazo1.game.wall.WallGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class BulletSimulator {
    public static class TimeSimulator {
        protected AtomicInteger counter = new AtomicInteger(0);

        public long getTime() {
            return (long) (counter.get() * (1000.0 / Config.FPS));
        }

        public void tickTime() {
            counter.getAndIncrement();
        }
    }

    /**
     * 模拟当前坦克位置和角度能否击中别的坦克
     *
     * @param twr                 当前坦克机器人
     * @param simulateOrientation 要进行模拟的发射角度
     */
    public boolean simulate(@NotNull RobotTank twr, double simulateOrientation) {
        TankGroup tankGroup = twr.getTankGroup();
        GameMap map = tankGroup.getGameMap();
        WallGroup wg = map.getWallGroup();
        Rectangle rect = twr.getRect();
        Vector<TankBase> livingTanks = tankGroup.getLivingTanks();
        int cx = (int) (rect.getCenterX() + Math.cos(simulateOrientation) * rect.width);
        int cy = (int) (rect.getCenterY() + Math.sin(simulateOrientation) * rect.width);
        TimeSimulator timeSimulator = new TimeSimulator();
        // 进行基础子弹的飞行模拟
        var bullet = new BulletBase(cx, cy, simulateOrientation) {
            public final AtomicBoolean hit = new AtomicBoolean(false);

            @Override
            public void update(Graphics graphics) {
                lifeModule.updateLife();
                if (!finished.get()) {
                    Rectangle bakRect = new Rectangle(rect);
                    rect.translate((int) (speed.get() * Math.cos(orientation.get())), (int) (speed.get() * Math.sin(orientation.get())));
                    if (reflectionModule.updateReflection(bakRect)) {
                        rect.setLocation(bakRect.x, bakRect.y);
                    }
                }
                // 模拟是否碰撞坦克

                for (TankBase tank : livingTanks) {
                    if (tank.getRect().intersects(rect)) {
                        hit.set(twr.getSeq() != tank.getSeq()); // 若碰到 twr 自身则不算击中
                        finish();
                    }
                }
                timeSimulator.tickTime();
            }

            {
                lifeModule = new LifeModule() {
                    {
                        createdTime.set(0);
                    }

                    @Override
                    public void updateLife() {
                        int width = map.getWidth();
                        int height = map.getHeight();
                        double cx = rect.getCenterX();
                        double cy = rect.getCenterY();
                        if (reflectionModule.getReflectionTimes() > MAX_REFLECTION_TIMES // 反射过多
                                || timeSimulator.getTime() > existingTime.get() + createdTime.get() // 存在过久
                                || cx > width || cx < 0 || cy > height || cy < 0) { // 超出边界
                            finish();
                        }
                    }
                };
                reflectionModule = new ReflectionModule() {
                    @Override
                    protected @Nullable Rectangle detectCollision() {
                        Vector<Wall> walls = wg.getWalls((int) rect.getCenterX(), (int) rect.getCenterY());
                        if (walls == null) { // 四叉树查找不到则全局检测
                            walls = wg.getWalls();
                        }
                        Rectangle rst = null;
                        for (Wall w : walls) {
                            Rectangle wRect = w.getRect();
                            if (rect.intersects(wRect)) {
                                if (rst == null) {
                                    rst = wRect;
                                } else if (rst.intersects(wRect)) {
                                    rst = rst.intersection(wRect);
                                }
                            }
                        }
                        return rst;
                    }
                };
            }
        };
        while (!bullet.isFinished()) {
            bullet.update(null);
        }
        return bullet.hit.get();
    }

    /**
     * 以 twr 当前方向进行子弹射击模拟
     */
    public boolean simulate(@NotNull RobotTank twr) {
        return simulate(twr, twr.orientationModule.getOrientation());
    }

    /**
     * 从弧度 0 (水平向右, 正为顺时针) 开始 按照一定步长增加发射角度, 依次进行子弹射击模拟
     *
     * @return 能够成功射击到别的坦克的方向; -1: 无法找到能打击到别的坦克的方向
     */
    public double simulateInAllOrientation(@NotNull RobotTank twr, double incrementStep) {
        double angle = 0;
        while (angle >= 0 && angle <= Math.PI * 2) {
            if (simulate(twr, angle)) {
                return angle;
            }
            angle += incrementStep;
        }
        return -1;
    }
}
