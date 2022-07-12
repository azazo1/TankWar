package com.azazo1.bullet;

import com.azazo1.util.AtomicDouble;
import com.azazo1.util.Tools;
import com.azazo1.wall.Wall;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class BulletBase {
    protected final static int EXISTING_DURATION_IN_MILLIS = 10000; // 子弹飞行时长
    protected final static int MAX_REFLECTION_TIMES = 10; // 子弹最大反弹次数
    protected static BufferedImage img;
    
    static {
        try {
            img = ImageIO.read(new File("res/Bullet.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected final AtomicInteger speed = new AtomicInteger(5);
    protected final AtomicBoolean finished = new AtomicBoolean(false); // 子弹是否已经击中目标或到达飞行时间上限
    protected final AtomicDouble orientation = new AtomicDouble(0); // 0 向右,顺时针为正向
    protected final Rectangle rect = new Rectangle(img.getWidth(), img.getHeight());
    protected final LifeModule lifeModule = new LifeModule();
    protected final ReflectionModule reflectionModule = new ReflectionModule();
    protected BulletGroup bulletGroup;
    
    /**
     * 子代都应继承这个构造函数
     */
    public BulletBase(int centerX, int centerY, double orientation) {
        rect.translate(centerX, centerY);
        rect.translate(-rect.width / 2, -rect.height / 2);
        this.orientation.set(orientation % (Math.PI * 2));
    }
    
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
    
    public void setSpeed(int speed) {
        this.speed.set(speed);
    }
    
    public void update(Graphics graphics) {
        if (!finished.get()) {
            lifeModule.updateLife();
            reflectionModule.updateReflection();
            rect.translate((int) (speed.get() * Math.cos(orientation.get())), (int) (speed.get() * Math.sin(orientation.get())));
            paint(graphics);
        } else {
            throw new IllegalStateException(this + " has finished.");
        }
    }
    
    protected void paint(@NotNull Graphics graphics) {
        graphics.drawImage(img, rect.x, rect.y, rect.width, rect.height, null); // 通常子弹是圆的,不需要旋转
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
    
    /**
     * 子弹生命模块
     */
    protected class LifeModule {
        protected final long createdTime = Tools.getFrameTimeInMillis();
        
        public void updateLife() {
            if (reflectionModule.getReflectionTimes() > MAX_REFLECTION_TIMES && Tools.getFrameTimeInMillis() > EXISTING_DURATION_IN_MILLIS + createdTime) {
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
         * 查找子弹是否和墙发生碰撞
         *
         * @return 与子弹碰撞的 一 个墙
         */
        @Nullable
        protected Wall detectCollision() {
            for (Wall w : bulletGroup.getGameMap().getWallGroup().getWalls()) {
                if (rect.intersects(w.getRect())) {
                    return w;
                }
            }
            return null;
        }
        
        /**
         * @return 与 {@link Wall} 对象重叠区域的中心位置
         */
        @NotNull
        protected Point getIntersectionCenterPoint(@NotNull Wall wall) {
            Rectangle intersection = rect.intersection(wall.getRect());
            return new Point((int) intersection.getCenterX(), (int) intersection.getCenterY());
        }
        
        /**
         * 反射
         *
         * @param intersectionCenterPoint 参阅 {@link #getIntersectionCenterPoint(Wall)} 返回值
         */
        protected void reflect(Point intersectionCenterPoint) {
            double tx = rect.getCenterX();
            double ty = rect.getCenterY();
            double dx = intersectionCenterPoint.getX();
            double dy = intersectionCenterPoint.getY();
            // 类法线: intersectionCenterPoint 和 本子弹中心点连线
            double theta = Math.atan2(dy - ty, dx - tx); // 类法线所在角度
            theta -= Math.PI / 2; // 该角度垂直于类法线
            double orientation = BulletBase.this.orientation.get();
            double dstOrientation = theta - (orientation - theta);
            BulletBase.this.orientation.set(dstOrientation % (2 * Math.PI));
        }
        
        /**
         * 尝试进行反射
         */
        public void updateReflection() {
            Wall wall = detectCollision();
            if (wall != null) { // 到此真正发生反射
                reflect(getIntersectionCenterPoint(wall));
                reflectionTimes.incrementAndGet();
            }
        }
    }
}
