package com.azazo1.online.server.tank;

import com.azazo1.Config;
import com.azazo1.game.bullet.BulletBase;
import com.azazo1.game.tank.TankBase;
import com.azazo1.online.msg.GlobalEventMsg;
import com.azazo1.online.server.toclient.Server;
import com.azazo1.util.Tools;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;

public class ServerTank extends TankBase {
    {
        doPaint.set(false);
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
    public void update(Graphics g) {
        if (enduranceModule.isDead()) {
            throw new IllegalStateException(this + " has died.");
        }
        // 检测碰撞并更新位置状态
        collisionAndMotionModule.updateMotion();
        // 更新弹夹状态
        fireModule.updateFireState();
        // 更新生命状态
        enduranceModule.updateLivingTime();
    }

    /**
     * 改变按键按下状态
     */
    public void keyChange(boolean left, boolean right, boolean forward, boolean backward) {
        leftTurningKeyPressed.set(left);
        rightTurningKeyPressed.set(right);
        forwardGoingKeyPressed.set(forward);
        backwardGoingKeyPressed.set(backward);
    }

    public ServerTank(int seq) {
        super(seq);
        setActionKeyMap(null);
        enduranceModule = new EnduranceModule() {
            @Override
            public boolean makeAttack(int damage) {
                if (Tools.getFrameTimeInMillis() > Config.TANK_INJURED_INTERVAL_MILLIS + lastInjuredTime.get()) { // 过了受伤间隔
                    endurance.getAndAdd(-damage);
                    lastInjuredTime.set(Tools.getFrameTimeInMillis());
                    if (damage > 0) {
                        // 广播坦克受伤事件
                        Server.instance.broadcast(new GlobalEventMsg(GlobalEventMsg.Events.TANK_ATTACKED), false);
                    }
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

}
