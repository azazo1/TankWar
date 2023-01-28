package com.azazo1.online.server.tank;

import com.azazo1.Config;
import com.azazo1.game.bullet.BulletBase;
import com.azazo1.game.tank.robot.RobotTank;
import com.azazo1.game.tank.robot.WayPoint;
import com.azazo1.online.msg.GlobalEventMsg;
import com.azazo1.online.server.toclient.Server;
import com.azazo1.util.Tools;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;

public class ServerRobotTank extends RobotTank {
    {
        setName("Robot");
        doPaint.set(false);
        setActionKeyMap(null);
        enduranceModule = new EnduranceModule() {
            @Override
            public boolean makeAttack(int damage) {
                if (damage > 0) {
                    becomeAngry(true); // 碰到子弹迅速生气
                }
                if (damage <= 0 // 恢复效果则不被忽略
                        || Tools.getFrameTimeInMillis() > Config.TANK_INJURED_INTERVAL_MILLIS + lastInjuredTime.get()) { // 过了受伤间隔
                    endurance.getAndAdd(-damage);
                    endurance.set(Math.min(maxEndurance, endurance.get())); // 防止超出最大生命值
                    lastInjuredTime.set(Tools.getFrameTimeInMillis());
                    if (damage > 0) {
                        // 广播坦克受伤事件
                        Server.instance.broadcast(new GlobalEventMsg(GlobalEventMsg.Events.TANK_ATTACKED), false);
                    }
                    if (endurance.get() <= 0) {
                        makeDie();
                        if (endurance.get() <= 0) { // 死亡时清空路径点缓存
                            WayPoint.clearWayPoint(); // 已经被拓展过, 保存在 TWR 中的的路径点不受影响, 防止本图影响下一局游戏
                        }
                    }
                    return true;
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
                    // 广播坦克开火事件
                    Server.instance.broadcast(new GlobalEventMsg(GlobalEventMsg.Events.TANK_FIRE), false);
                    return true;
                } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                         IllegalAccessException e) {
                    throw new RuntimeException(e); // 一般不会到达此处
                }
            }
        };
    }

    @Override
    public void randomlyTeleport() {
        Rectangle rectBak = new Rectangle(rect);
        double orientationBak = orientationModule.getOrientation();
        Random random = new Random();
        int mapWidth = tankGroup.getGameMap().getWidth();
        int mapHeight = tankGroup.getGameMap().getHeight();
        int newX = random.nextInt(mapWidth), newY = random.nextInt(mapHeight);
        rect.setLocation(newX, newY);
        orientationModule.setOrientation(random.nextDouble(0, Math.PI * 2));
        if (!collisionAndMotionModule.detectAllCollision().isEmpty()) {
            rect.setRect(rectBak);
            orientationModule.setOrientation(orientationBak);
            randomlyTeleport(); // 重试直到没有发生碰撞
        } else {
            // 成功传送时广播消息
            Server.instance.broadcast(new GlobalEventMsg(GlobalEventMsg.Events.TANK_RANDOMLY_TELEPORT), false);
        }
    }

    @Override
    protected void paint(Graphics g) {
    }

    public ServerRobotTank(int seq) {
        super(seq);
    }

    public ServerRobotTank() {
        super();
    }
}
