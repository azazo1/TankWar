package com.azazo1.game.bullet;

import com.azazo1.util.AtomicDouble;
import com.azazo1.util.Tools;
import com.azazo1.game.wall.Wall;
import com.azazo1.game.wall.WallGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class BulletBase {
    protected final static int EXISTING_DURATION_IN_MILLIS = 5000; // 子弹飞行时长
    protected final static int MAX_REFLECTION_TIMES = 10; // 子弹最大反弹次数
    protected static BufferedImage img;
    
    static {
        try {
            img = ImageIO.read(new File("res/Bullet.png")); // 为了保证子弹反射正常进行, 建议子弹图像为方形
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
        lifeModule.updateLife();
        if (!finished.get()) {
            reflectionModule.updateReflection();
            rect.translate((int) (speed.get() * Math.cos(orientation.get())), (int) (speed.get() * Math.sin(orientation.get())));
            paint(graphics);
        }
    }
    
    protected void paint(@NotNull Graphics graphics) {
        graphics.translate(rect.x, rect.y);
        ((Graphics2D) graphics).rotate(orientation.get());
        graphics.drawImage(img, -rect.width / 2, -rect.height / 2, rect.width, rect.height, null);
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
            int width = bulletGroup.getGameMap().getWidth();
            int height = bulletGroup.getGameMap().getHeight();
            double cx = rect.getCenterX();
            double cy = rect.getCenterY();
            if (reflectionModule.getReflectionTimes() > MAX_REFLECTION_TIMES // 反射过多
                    || Tools.getFrameTimeInMillis() > EXISTING_DURATION_IN_MILLIS + createdTime // 存在过久
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
         * 查找子弹是否和墙发生碰撞
         *
         * @return 与子弹碰撞的 一 个墙
         */
        @Nullable
        protected Wall detectCollision() {
            WallGroup wg = bulletGroup.getGameMap().getWallGroup();
            Vector<Wall> walls = wg.getWalls((int) rect.getCenterX(), (int) rect.getCenterY());
            if (walls == null) { // 四叉树查找不到则全局检测
                walls = wg.getWalls();
            }
            for (Wall w : walls) {
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
         * 反射, 法线只有水平和垂直两种<br>
         * 通过判断 重叠中心点(intersectionCenterPoint) 处在子弹矩形的四个方位 (U R D L) 中的一个方位来判断法线<br>
         * 四个方位划分如下(1为分界线):<br>
         * \_U_/<br>
         * _\_/_<br>
         * L_·_R<br>
         * _/_\_<br>
         * /_D_\<br>
         * todo 子弹仍有穿墙的bug
         *
         * @param intersectionCenterPoint 参阅 {@link #getIntersectionCenterPoint(Wall)} 返回值
         */
        protected void reflect(@NotNull Point intersectionCenterPoint) {
            double tx = rect.getCenterX();
            double ty = rect.getCenterY();
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
