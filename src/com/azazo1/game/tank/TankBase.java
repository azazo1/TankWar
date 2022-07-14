package com.azazo1.game.tank;

import com.azazo1.Config;
import com.azazo1.base.TankAction;
import com.azazo1.game.bullet.BulletBase;
import com.azazo1.game.wall.Wall;
import com.azazo1.game.wall.WallGroup;
import com.azazo1.util.AtomicDouble;
import com.azazo1.util.Tools;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

public class TankBase {
    
    protected static final BufferedImage rawImg;
    
    static {
        try {
            rawImg = ImageIO.read(new File("res/Tank.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected final AtomicBoolean leftTurningKeyPressed = new AtomicBoolean(false);
    protected final AtomicBoolean rightTurningKeyPressed = new AtomicBoolean(false);
    protected final AtomicBoolean forwardGoingKeyPressed = new AtomicBoolean(false);
    protected final AtomicBoolean backwardGoingKeyPressed = new AtomicBoolean(false);
    protected final AtomicBoolean firingKeyPressed = new AtomicBoolean(false); // 当对应按键按下后停止开火动作
    protected final OrientationModule orientationModule = new OrientationModule(); // 用于将方向校准于坐标轴
    protected final AtomicDouble goingSpeed = new AtomicDouble(6); // 行动速度 pixels/帧
    protected final AtomicDouble turningSpeed = new AtomicDouble(Math.toRadians(7)); // 转向速度 rad/帧
    protected final EnduranceModule enduranceModule = new EnduranceModule();
    protected final FireModule fireModule = new FireModule();
    private final int seq;
    protected Rectangle rect = new Rectangle(0, 0, rawImg.getWidth(), rawImg.getHeight());
    protected TankGroup tankGroup; // 用于统一处理数据和显示
    private HashMap<Integer, TankAction> actionKeyMap; // 默认按键映射
    
    /**
     * @implNote 当为 Online 模式时, 客户端子类无参构造函数要禁用, 因为 seq 由服务端控制
     */
    public TankBase() {
        this(SeqModule.next());
    }
    
    public TankBase(int seq) {
        super();
        SeqModule.use(seq); // 若序号已经在使用会报错
        this.seq = seq;
        setActionKeyMap(Config.TANK_ACTION_KEY_MAPS.get(seq));
    }
    
    public Rectangle getRect() {
        return new Rectangle(rect);
    }
    
    /**
     * 激发坦克对应运动状态: {@link TankAction}
     * 值得注意的是按键映射的选择应由子代继承后实现
     *
     * @param keyCode 按下的按键对应的 code
     */
    public void pressKey(int keyCode) {
        TankAction motion = actionKeyMap.get(keyCode);
        if (motion == null) {
            return;
        }
        switch (motion) {
            case FORWARD_GOING -> forwardGoingKeyPressed.set(true);
            case LEFT_TURNING -> leftTurningKeyPressed.set(true);
            case RIGHT_TURNING -> rightTurningKeyPressed.set(true);
            case BACKWARD_GOING -> backwardGoingKeyPressed.set(true);
            case FIRE -> toggleTrigger(true); // 开火在每次按下对应键时只会启动一次
        }
    }
    
    /**
     * 解除坦克对应运动状态: {@link TankAction}
     * 值得注意的是按键映射的选择应由子代继承后实现
     *
     * @param keyCode 松开的按键对应的 code
     */
    public void releaseKey(int keyCode) {
        TankAction motion = actionKeyMap.get(keyCode);
        if (motion == null) {
            return;
        }
        switch (motion) {
            case FORWARD_GOING -> forwardGoingKeyPressed.set(false);
            case LEFT_TURNING -> leftTurningKeyPressed.set(false);
            case RIGHT_TURNING -> rightTurningKeyPressed.set(false);
            case BACKWARD_GOING -> backwardGoingKeyPressed.set(false);
            case FIRE -> toggleTrigger(false);
        }
    }
    
    /**
     * 选择按键映射
     */
    public void setActionKeyMap(HashMap<Integer, TankAction> keyMap) {
        this.actionKeyMap = keyMap;
    }
    
    
    /**
     * 设置本坦克所属的 group,本操作不保证真的从 group 对象中添加本坦克
     * <br>本方法应由 {@link com.azazo1.game.tank.TankGroup#addTank(TankBase)} 调用
     */
    public void setTankGroup(TankGroup tankGroup) {
        this.tankGroup = tankGroup;
    }
    
    /**
     * 将本坦克所属的 group 设置为 null,本操作不保证真的从 group 对象中移除本坦克
     * <br>本方法应由 {@link com.azazo1.game.tank.TankGroup#removeTank(TankBase)} 调用
     */
    public void clearTankGroup() {
        this.tankGroup = null;
    }
    
    /**
     * @param state true: 扣下开火扳机, 若原来已经按下则不会开火<br>
     *              false: 松开扳机
     */
    private void toggleTrigger(boolean state) {
        if (state && !firingKeyPressed.get()) {
            fireModule.fire();
        }
        firingKeyPressed.set(state);
    }
    
    /**
     * 向坦克面朝方向运动
     *
     * @param length 运动长度 pixels,正数则前进,负数则后退
     */
    public void go(double length) {
        orientationModule.adjust();
        // 移动
        rect.translate((int) (length * Math.cos(orientationModule.getOrientation())), (int) (length * Math.sin(orientationModule.getOrientation())));
    }
    
    /**
     * 坦克转弯
     *
     * @param theta 转弯角度 rad,正数顺时针,负数逆时针
     */
    public void turn(double theta) {
        orientationModule.setOrientation(orientationModule.getOrientation() + theta);
    }
    
    
    public boolean detectCollision(Rectangle rect) {
        // todo 更合理的碰撞检测
        return this.rect.intersects(rect);
    }
    
    public void update(Graphics g) {
        if (enduranceModule.isDead()) {
            throw new IllegalStateException(this + " has died.");
        }
        // 位置备份
        Rectangle rectBak = getRect();
        double orientationBak = orientationModule.getOrientation();
        
        // 移动
        if (forwardGoingKeyPressed.get()) {
            go(goingSpeed.get());
        } else if (backwardGoingKeyPressed.get()) {
            go(-goingSpeed.get());
        }
        if (leftTurningKeyPressed.get()) {
            turn(-turningSpeed.get());
        }
        if (rightTurningKeyPressed.get()) {
            turn(turningSpeed.get());
        }
        
        // 与墙进行碰撞检测
        WallGroup wg = tankGroup.getGameMap().getWallGroup();
        Vector<Wall> walls = wg.getWalls((int) rect.getCenterX(), (int) rect.getCenterY());
        if (walls == null) {
            walls = wg.getWalls();
        }
        for (Wall w : walls) {
            if (detectCollision(w.getRect())) {
                // 发生碰撞了, 取消变换
                rect = rectBak;
                orientationModule.setOrientation(orientationBak);
            }
        }
        // 与子弹进行碰撞检测
        for (BulletBase b : tankGroup.getGameMap().getBulletGroup().getBullets()) {
            if (detectCollision(b.getRect())) {
                // 碰撞到了, 使受到伤害
                if (enduranceModule.makeAttack(b.getDamage())) {
                    b.finish();
                }
            }
        }
        
        // 更新弹夹状态
        fireModule.updateFireState();
        
        paint(g);
    }
    
    protected void paint(@NotNull Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        double dx = rect.getCenterX();
        double dy = rect.getCenterY();
        g2d.translate(dx, dy); // 移动初始位置
        Graphics2D g2dBak = (Graphics2D) g2d.create();
        g2d.rotate(orientationModule.getOrientation());
        // 注意负值使图像中点落在初始位置上, 使旋转锚点正确在图像中央
        g.drawImage(enduranceModule.adjustEnduranceImage(), -rect.width / 2, -rect.height / 2, rect.width, rect.height, null);
        // 在坦克身上标注序号 todo 有待完善
        g2dBak.setColor(Config.TANK_SEQ_COLOR);
        g2dBak.setFont(Config.TANK_SEQ_FONT);
        g2dBak.drawString(seq + "", -rect.width / 2, -rect.height / 2);
        // 显示坦克子弹数
        g2dBak.setColor(Config.TANK_CLIP_COLOR);
        g2dBak.setFont(Config.TANK_CLIP_FONT); // 降低字体大小
        g2dBak.drawString(Config.translation.hasBulletChar.repeat(fireModule.getSpareBulletNum()) +
                        Config.translation.emptyBulletChar.repeat(fireModule.getUsedBulletNum()),
                -rect.width / 4, -rect.height / 4);
    }
    
    public EnduranceModule getEnduranceManager() {
        return enduranceModule;
    }
    
    private static final class SeqModule {
        private static final AtomicInteger cur = new AtomicInteger(0);
        private static final HashSet<Integer> usingSequences = new HashSet<>(); // 正在被使用的序号
        private static final HashSet<Integer> spareSequences = new HashSet<>(); // 被以前创建过但是被废弃的序号
        
        /**
         * 产生新序列
         */
        private static int next() {
            if (!spareSequences.isEmpty()) { // 先使用 spareSequences 里的序号
                int rst = spareSequences.iterator().next();
                spareSequences.remove(rst);
                return rst;
            }
            int rst;
            while (true) { // 跳过已经在使用的序号
                if (!isUsing((rst = cur.getAndIncrement()))) {
                    break;
                }
            }
            return rst;
        }
        
        /**
         * 判断该序号是否被使用
         */
        private static boolean isUsing(int seq) {
            return usingSequences.contains(seq);
        }
        
        /**
         * 将序号设置为正在使用
         */
        public static void use(int seq) {
            if (isUsing(seq)) {
                throw new IllegalArgumentException("This sequence has been used.");
            }
            spareSequences.remove(seq);
            usingSequences.add(seq);
        }
        
        /**
         * 将指定序号设置为未使用(不会判断原来是否在使用)
         */
        private static void dispose(int seq) {
            usingSequences.remove(seq);
            spareSequences.add(seq);
        }
    }
    
    /**
     * 坦克生命模块
     */
    protected class EnduranceModule {
        protected final AtomicLong lastInjuredTime = new AtomicLong(0);
        protected final AtomicInteger endurance = new AtomicInteger(Config.TANK_MAX_ENDURANCE); // 一般此数值不会小于零
        
        public EnduranceModule() {
        }
        
        /**
         * 尝试使坦克受伤, 血量低于 1 时自动调用 {@link #makeDie()}
         *
         * @return 是否造成了伤害
         */
        public boolean makeAttack(int damage) {
            if (Tools.getFrameTimeInMillis() > Config.TANK_INJURED_INTERVAL_MILLIS + lastInjuredTime.get()) { // 过了受伤间隔
                endurance.getAndAdd(-damage);
                lastInjuredTime.set(Tools.getFrameTimeInMillis());
                return true;
            }
            if (endurance.get() <= 0) {
                makeDie();
            }
            return false;
        }
        
        /**
         * 使坦克死亡(无视其血量), 本操作不保证坦克从 {@link TankGroup} 中移除
         * <br>但死亡的坦克会自动被处在事件调度时的 {@link TankGroup} 清除
         *
         * @see TankGroup#update(Graphics)
         */
        public void makeDie() {
            endurance.set(0);
            SeqModule.dispose(seq);
        }
        
        /**
         * 查看坦克是否死亡
         */
        public boolean isDead() {
            return endurance.get() <= 0;
        }
        
        /**
         * 返回坦克当前生命值对应的图片 (血量越低越透明)
         */
        public Image adjustEnduranceImage() { // 临时生成图片, 测试发现此方法对帧率影响不大
            double bias = endurance.get() * 1.0 / Config.TANK_MAX_ENDURANCE; // 0~1
            BufferedImage img = Tools.deepCopy(rawImg);
            var r = img.getAlphaRaster();
            int[] data = r.getPixels(0, 0, r.getWidth(), r.getHeight(), (int[]) null);
            IntStream newData = Arrays.stream(data).map((i) -> (int) (i * bias));
            r.setPixels(0, 0, r.getWidth(), r.getHeight(), newData.toArray());
            return img;
        }
    }
    
    /**
     * 自动校准坦克朝向，防止 orientation 小角度的偏离坐标轴却平行坐标轴行驶
     */
    protected class OrientationModule {
        protected static double ignoredRad = Math.toRadians(10); // 将被忽略的距离坐标轴的偏差
        protected static int intervalFrames = 10; // 每隔一定帧就尝试校准一次
        protected final AtomicDouble orientation = new AtomicDouble(0); // 0 向右,顺时针为正向
        
        public void adjust() {
            if (Tools.getFrameCounts() % intervalFrames != 0 || leftTurningKeyPressed.get() || rightTurningKeyPressed.get()) {
                return;
            }
            // 尝试去贴近水平轴或竖直轴 todo 等真的碰撞检测做好后可能会有一定副作用
            double _orientation = getOrientation();
            double positiveOrientation = _orientation; // 将 orientation 调整到正数区间内
            while (positiveOrientation < 0) {
                positiveOrientation += Math.PI / 2;
            }
            double delta = positiveOrientation % (Math.PI / 2);
            if (delta < ignoredRad) { // 距离上一个九十度不远
                orientation.set(_orientation - delta);
            } else if (delta > Math.PI / 2 - ignoredRad) { // 要接近下一个九十度
                orientation.set(_orientation + Math.PI / 2 - delta);
            }
        }
        
        public double getOrientation() {
            return orientation.get();
        }
        
        public void setOrientation(double orientation) {
            this.orientation.set(orientation % (Math.PI * 2));
        }
    }
    
    protected class FireModule {
        protected final AtomicLong lastIncrementTime = new AtomicLong(0); // 上次弹夹数量增加时间戳 (FrameTime)
        protected final AtomicInteger spareBulletNum = new AtomicInteger(0); // 弹夹内子弹数量, 有最大值, 见 Config
        
        public FireModule() {
        }
        
        public void fire(@NotNull Class<? extends BulletBase> T) {
            if (spareBulletNum.get() <= 0) {
                return;
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
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new RuntimeException(e); // 一般不会到达此处
            }
        }
        
        /**
         * 管理弹夹
         */
        public void updateFireState() {
            long nowTime = Tools.getFrameTimeInMillis();
            if (nowTime > Config.TANK_BULLET_INCREMENT_INTERVAL_MILLIS + lastIncrementTime.get()
                    && spareBulletNum.get() < Config.TANK_MAX_FIRE_CAPACITY) {
                spareBulletNum.getAndIncrement();
                lastIncrementTime.set(nowTime);
            }
            if (spareBulletNum.get() >= Config.TANK_MAX_FIRE_CAPACITY) { // 防止满弹夹开火瞬间补回一发子弹
                lastIncrementTime.set(nowTime);
            }
        }
        
        /**
         * 坦克开火
         * 子代可以继承来修改子弹的类型
         */
        public void fire() {
            fire(BulletBase.class);
        }
        
        public int getSpareBulletNum() {
            return spareBulletNum.get();
        }
        
        public int getUsedBulletNum() {
            return Config.TANK_MAX_FIRE_CAPACITY - spareBulletNum.get();
        }
    }
}
