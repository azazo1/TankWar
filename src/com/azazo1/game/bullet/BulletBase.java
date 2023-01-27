package com.azazo1.game.bullet;

import com.azazo1.base.CharWithRectangle;
import com.azazo1.game.tank.TankBase;
import com.azazo1.game.wall.Wall;
import com.azazo1.game.wall.WallGroup;
import com.azazo1.util.AtomicDouble;
import com.azazo1.util.Tools;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class BulletBase implements CharWithRectangle {
    protected final AtomicInteger existingTime = new AtomicInteger(5000); // 子弹飞行时长
    protected final static int MAX_REFLECTION_TIMES = 10; // 子弹最大反弹次数
    public static final String imgFile = "img/Bullet.png";
    protected volatile BufferedImage rawImg = Tools.loadImg(imgFile);


    protected final AtomicInteger speed = new AtomicInteger(3);
    protected final AtomicBoolean finished = new AtomicBoolean(false); // 子弹是否已经击中目标或到达飞行时间上限
    protected final AtomicDouble orientation = new AtomicDouble(0); // 0 向右,顺时针为正向
    protected final Rectangle rect = new Rectangle(rawImg.getWidth(), rawImg.getHeight());
    protected volatile LifeModule lifeModule = new LifeModule();
    protected volatile ReflectionModule reflectionModule = new ReflectionModule();
    protected final AtomicInteger damage = new AtomicInteger(1); // 对坦克造成的伤害
    protected volatile BulletGroup bulletGroup;

    public LifeModule getLifeModule() {
        return lifeModule;
    }


    /**
     * 子代都应继承这个构造函数
     */
    public BulletBase(int centerX, int centerY, double orientation) {
        rect.translate(centerX, centerY);
        rect.translate(-rect.width / 2, -rect.height / 2);
        this.orientation.set(orientation % (Math.PI * 2));
    }

    /**
     * @implNote 当为 Online 模式时, 客户端子类无 seq 参构造函数要禁用, 因为 seq 由服务端控制
     * @deprecated 速度不作为初始化参数
     */
    public BulletBase(int centerX, int centerY, double orientation, int speed) {
        this(centerX, centerY, orientation);
        setSpeed(speed);
    }

    public Rectangle getRect() {
        return new Rectangle(rect);
    }

    /**
     * 子弹滴任务完成啦！
     */
    public void finish() {
        finished.set(true);
    }

    /**
     * @param tank 击中的坦克，可覆盖此方法以实现特殊子弹效果
     */
    public void finish(TankBase tank) {
        finish();
    }

    public void setSpeed(int speed) {
        this.speed.set(speed);
    }

    public void update(Graphics graphics) {
        lifeModule.updateLife();
        if (!finished.get()) {
            Rectangle bakRect = new Rectangle(rect);
            rect.translate((int) (speed.get() * Math.cos(orientation.get())), (int) (speed.get() * Math.sin(orientation.get())));
            if (reflectionModule.updateReflection(bakRect)) {
                rect.setLocation(bakRect.x, bakRect.y);
            }
            paint(graphics);
        }
    }

    protected void paint(Graphics graphics) {
        if (graphics != null) {
            graphics.translate(rect.x, rect.y);
            ((Graphics2D) graphics).rotate(orientation.get());
            graphics.drawImage(rawImg, -rect.width / 2, -rect.height / 2, rect.width, rect.height, null);
        }
    }

    public boolean isFinished() {
        return finished.get();
    }

    /**
     * 设置本子弹所属的 group, 本操作不保证真的从 group 对象中添加本子弹
     * 本方法应由 {@link BulletGroup#addBullet(BulletBase)} 调用
     */
    public void setBulletGroup(BulletGroup bulletGroup) {
        this.bulletGroup = bulletGroup;
    }

    /**
     * 清除本子弹所属的 group, 本操作不保证真的从 group 对象中移除本子弹
     * 本方法应由 {@link BulletGroup#removeBullet(BulletBase)} 调用
     */
    public void clearBulletGroup() {
        this.bulletGroup = null;

    }

    public int getDamage() {
        return damage.get();
    }

    public BulletInfo getInfo() {
        return new BulletInfo(this);
    }

    /**
     * 储存子弹信息，用于序列化子弹
     */
    public static class BulletInfo implements Serializable {
        protected final Class<? extends BulletBase> bulletType;
        protected final double orientation;
        protected final Rectangle rect;
        protected final long createdTime;

        protected BulletInfo(@NotNull BulletBase bullet) {
            orientation = bullet.orientation.get();
            rect = new Rectangle(bullet.rect);
            createdTime = bullet.lifeModule.createdTime.get();
            bulletType = bullet.getClass();
        }

        public double getOrientation() {
            return orientation;
        }

        public Rectangle getRect() {
            return new Rectangle(rect);
        }

        public long getCreatedTime() {
            return createdTime;
        }

        public Class<? extends BulletBase> getBulletType() {
            return bulletType;
        }
    }

    /**
     * 子弹生命模块
     */
    protected class LifeModule {
        protected final AtomicLong createdTime = new AtomicLong(Tools.getFrameTimeInMillis()); // 便于 BunshinBullet 继承修改

        public void updateLife() {
            int width = bulletGroup.getGameMap().getWidth();
            int height = bulletGroup.getGameMap().getHeight();
            double cx = rect.getCenterX();
            double cy = rect.getCenterY();
            if (reflectionModule.getReflectionTimes() > MAX_REFLECTION_TIMES // 反射过多
                    || Tools.getFrameTimeInMillis() > existingTime.get() + createdTime.get() // 存在过久
                    || cx > width || cx < 0 || cy > height || cy < 0) { // 超出边界
                finish();
            }
        }
    }

    /**
     * 子弹（轨迹）反射模块
     */
    protected class ReflectionModule {
        protected final AtomicInteger reflectionTimes = new AtomicInteger(0); // 发生发射次数

        public int getReflectionTimes() {
            return reflectionTimes.get();
        }

        /**
         * 用下一帧的位置查找子弹是否和墙发生碰撞
         *
         * @return 与子弹碰撞的所有墙合并形成的矩形
         */
        @Nullable
        protected Rectangle detectCollision() {
            WallGroup wg = bulletGroup.getGameMap().getWallGroup();
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

        /**
         * 用下一帧的位置计算碰撞重叠区域
         *
         * @return 与 {@link Wall#getRect()} 对象重叠区域的中心位置
         */
        @NotNull
        protected Point getIntersectionCenterPoint(@NotNull Rectangle wallRect) {
            Rectangle intersection = rect.intersection(wallRect);
            return new Point((int) intersection.getCenterX(), (int) intersection.getCenterY());
        }

        /**
         * 反射, 法线只有水平和垂直两种<br>
         * 通过判断 重叠中心点(intersectionCenterPoint) 处在子弹矩形的四个方位 (U R D L) 中的一个方位来判断法线<br>
         * 四个方位划分如下(1为分界线):<br>
         * \_U_/<br>
         * _\_/_<br>
         * L_·_R<br>
         * _/_\_<br>
         * /_D_\<br>
         *
         * @param intersectionCenterPoint 参阅 {@link #getIntersectionCenterPoint(Rectangle)} 返回值
         */
        protected void reflect(@NotNull Point intersectionCenterPoint, @NotNull Rectangle bakRect) {
            double tx = bakRect.getCenterX();
            double ty = bakRect.getCenterY();
            double dx = intersectionCenterPoint.getX();
            double dy = intersectionCenterPoint.getY();
            double X = dx - tx, Y = dy - ty;
            boolean horizontal = Math.abs(X) < Math.abs(Y); // 法线是否是水平的, 当重叠中心点在 U D 区域时法线就是水平的
            double orientation = BulletBase.this.orientation.get();
            double theta = horizontal ? 0 : Math.PI / 2;

            double dstOrientation = theta - (orientation - theta); // 反向
            BulletBase.this.orientation.set(dstOrientation % (2 * Math.PI));
        }

        /**
         * 尝试进行反射
         *
         * @param bakRect 此帧时子弹位置
         * @return 是否发生了反射
         * @apiNote 应该在子弹进行了位置变换之后调用
         */
        public boolean updateReflection(@NotNull Rectangle bakRect) {
            Rectangle wallRect = detectCollision();
            if (wallRect != null) { // 到此真正发生反射
                reflect(getIntersectionCenterPoint(wallRect), bakRect);
                reflectionTimes.incrementAndGet();
                return true;
            }
            return false;
        }
    }
}
