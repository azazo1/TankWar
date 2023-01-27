package com.azazo1.game.tank.robot;

import com.azazo1.Config;
import com.azazo1.game.tank.TankBase;
import com.azazo1.game.tank.TankGroup;
import com.azazo1.game.tank.robot.action.Action;
import com.azazo1.game.tank.robot.action.ChangeOrientationAction;
import com.azazo1.game.tank.robot.action.GoingAction;
import com.azazo1.game.tank.robot.action.MultipleFireAction;
import com.azazo1.game.wall.Wall;
import com.azazo1.game.wall.WallGroup;
import com.azazo1.util.IntervalTicker;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;


public class RobotTank extends TankBase {
    /**
     * <h3>"较近" 距离</h3>
     * 此距离内 TWR 由寻路模式变为攻击模式 todo 说明
     */
    protected final double nearDistance = rect.width * 4;
    /**
     * <h3>"很近" 距离<h3/>
     * 此距离下 TWR todo 说明
     */
    protected final double veryCloseDistance = rect.width * 2;
    protected volatile WayPoint startPoint;
    protected volatile TankBase targetTank;
    /**
     * 用于执行 TWR 各种长时间动作
     */
    protected final Vector<Action> actions = new Vector<>();
    /**
     * {@link RobotTank#calmness}∈[0,100], 越高表示 TWR 越冷静, 开火概率越小
     */
    protected final AtomicInteger calmness = new AtomicInteger(100);
    /**
     * TWR 很生气时连发子弹数
     */
    protected final int fireTimesWhileAngry = 3;
    /**
     * 用于判断什么时候进行目标切换
     */
    protected final IntervalTicker targetChangeTicker = new IntervalTicker(3000);
    /**
     * 检测到最近的坦克不是目标坦克的检测次数
     */
    protected final AtomicInteger differentTargetTimes = new AtomicInteger(0);
    /**
     * TWR 行进路线
     */
    protected volatile Route route = null;
    /**
     * 子弹飞行模拟器
     */
    protected final BulletSimulator bulletSimulator = new BulletSimulator();
    /**
     * 全方位子弹模拟的角度间隔 ([0, PI*2]), 越小模拟越精细, 越大模拟越迅速
     */
    protected final double simulateIncrementStep = 5.625 / 180 * Math.PI;

    /**
     * 进行全方向子弹模拟的间隔时间控制
     */
    protected final IntervalTicker allOrientationSimulatorTicker = new IntervalTicker(1000);


    { // 禁用按键
        setActionKeyMap(null);
        enduranceModule = new EnduranceModule() {
            @Override
            public boolean makeAttack(int damage) {
                if (damage > 0) {
                    becomeAngry(true); // 受伤后迅速生气
                }
                return super.makeAttack(damage);
            }
        };
    }

    @Override
    public void update(@NotNull Graphics g) {
        if (startPoint == null) { // 仅在游戏开始时构建一次路径点
            initStartPoint();
        }
        if (targetTank == null || targetTank.enduranceModule.isDead()) {
            // 确定目标坦克
            Vector<TankBase> livingTanks = tankGroup.getLivingTanks();
            double minDistance_pow2 = Double.MAX_VALUE;
            for (TankBase tankBase : livingTanks) {
                if (tankBase.getSeq() != getSeq()) {
                    double distance_pow2 = Math.pow(rect.getCenterX() - tankBase.getRect().getCenterX(), 2)
                            + Math.pow(rect.getCenterY() - tankBase.getRect().getCenterY(), 2);
                    if (distance_pow2 < minDistance_pow2) { // 选择距离最短的坦克
                        targetTank = tankBase;
                        minDistance_pow2 = distance_pow2;
                    }
                }
            }
        }
        behave();

        // 执行 Action
        actions.removeIf(action -> action.take(this)); // 返回值为 true 则删除
        // 显示所有路径点
        Graphics g1 = g.create();
        g1.setColor(new Color(0xff00ff));
        startPoint.drawAll(g1);
        // 显示路径
        g1.setColor(new Color(0x0000ff));
        route.resetCursor();
        while (route.hasNext()) {
            WayPoint point = route.next();
            point.drawHighlight(g1);
        }
        // 突出显示离 TWR 最近的路径点
        g1.setColor(new Color(0xFF0000));
        route.getStartPoint().drawHighlight(g1);
        g1.drawString(Config.translation.calmnessText + getCalmness(), rect.x + rect.width, rect.y + rect.height);
        super.update(g);
    }

    /**
     * 进行 TWR 行为判断
     */
    protected void behave() {
        double cx = rect.getCenterX(), cy = rect.getCenterY();
        // 搜索到目标坦克最短路径 (建议广度优先, 快)
        Rectangle targetTankRect = targetTank.getRect();
        WayPoint targetPoint /* 距离目标坦克最近的路径点, 不一定为 route 终点 */ = startPoint.getNearestWayPoint((int) targetTankRect.getCenterX(), (int) targetTankRect.getCenterY());
        route = searchRouteTo(targetPoint);

        // 转向(长时间)
        route.resetCursor();
        route.next();
        WayPoint secondPoint;
        try {
            secondPoint = route.next(); // 找到第二个路径点
        } catch (ArrayIndexOutOfBoundsException e) { // 起始点和终点在同一路径点
            secondPoint = route.getLastPoint();
        }
        double angleToSecondPoint = Math.atan2(secondPoint.y - cy, secondPoint.x - cx);
        while (angleToSecondPoint < 0) { // 调整到正数区间
            angleToSecondPoint += Math.PI * 2;
        }
        double angleToTarget = Math.atan2(targetPoint.y - cy, targetPoint.x - cx);
        while (angleToTarget < 0) { // 调整到正数区间
            angleToTarget += Math.PI * 2;
        }
        if (targetPoint.distanceTo(route.getStartPoint()) > nearDistance + 5) { // 寻路时才转向至路径点
            putAction(new ChangeOrientationAction(angleToSecondPoint), false, true); // 对准第二个路径点, 用于前进
            calmDown(false); // 变得冷静
        }

        // 前行(长时间) todo TWR 会卡墙角
        putAction(new GoingAction(route.getLastPoint(), nearDistance - 5), false, true);

        // 寻路行进时若发射方向上能打击到坦克则发射
        if (targetPoint.distanceTo(route.getStartPoint()) > nearDistance + 5
                && bulletSimulator.simulate(this)) { // 模拟子弹飞行, 若打到自己则不发射
            becomeAngry(); // 变得冲动
            if (calmnessJudge()) {
                if (fireModule.fire()) /* 开火 (短时间) */ {  // 成功开火则进入冷静期
                    calmDown(false); // 变冷静
                }
            }
        }

        // 较近时
        if (targetPoint.distanceTo(route.getStartPoint()) < nearDistance + 5) {
            becomeAngry(); // 不断变得冲动

            if (allOrientationSimulatorTicker.judgeCanExecute()) {
                EventQueue.invokeLater(() -> { // 在另一个线程进行, 防止其降低帧率
                    // 全方位模拟子弹飞行, 寻找能打到敌人的方向, 转向后开火
                    double hitAngle = bulletSimulator.simulateInAllOrientation(this, simulateIncrementStep);
                    if (hitAngle >= 0) {
                        // 转向 (长时间)
                        putAction(new ChangeOrientationAction(hitAngle), false, true);
                    }
                });
            }
            if (bulletSimulator.simulate(this) && calmnessJudge() // 模拟能否击中 + 冷静判断
                    && fireModule.fire()) /* 开火 (短时间) */ {  // 成功开火则进入冷静期
                calmDown(false); // 变冷静
            }
        }
        // 很近时
        if (targetPoint.distanceTo(route.getStartPoint()) < veryCloseDistance + 5) {
            if (bulletSimulator.simulate(this) /* 判断方向是否可打击到目标坦克, 转向操作在 较近 判断中已经进行 */
                    && calmnessJudge()) {
                // 连发开火 (长时间)
                putAction(new MultipleFireAction(fireTimesWhileAngry), false, false);
                calmDown(true); // 变非常冷静
            }
        }
        // 隔一段时间尝试一次目标切换
        if (targetChangeTicker.judgeCanExecute()) {
            TankBase t = getNearestTank();
            if (t.getSeq() != targetTank.getSeq()) {
                if (differentTargetTimes.getAndIncrement() > 0) { // 连续两次检测时, 最近坦克都不同于目标坦克
                    differentTargetTimes.set(0);
                    targetTank = t;
                }
            } else {
                differentTargetTimes.set(0);
            }
        }
    }

    /**
     * 找到距离本 TWR 最近的异己坦克
     */
    private TankBase getNearestTank() {
        double minDistance2 = Double.MAX_VALUE;
        TankBase rst = null;
        for (TankBase tank : tankGroup.getLivingTanks()) {
            if (tank.getSeq() == getSeq()) {
                continue;
            }
            double distance2 = Math.pow(tank.getRect().getCenterX() - rect.getCenterX(), 2) + Math.pow(tank.getRect().getCenterY() - rect.getCenterY(), 2);
            if (distance2 < minDistance2) {
                rst = tank;
                minDistance2 = distance2;
            }
        }
        return rst;
    }

    public int getCalmness() {
        return calmness.get();
    }

    /**
     * 迅速让 TWR 冷静
     */
    protected void calmDown(boolean intense) {
        if (intense) {
            calmness.set(170);
        } else {
            calmness.set(120);
        }
    }

    /**
     * TWR 通过冷静值判断是否应该开火或做其他<u>冲动</u>行为
     *
     * @apiNote 越冷静越难为true
     */
    protected boolean calmnessJudge() {
        return new Random().nextInt(101) > getCalmness();
    }

    /**
     * 让 TWR 变暴躁
     */
    protected void becomeAngry() {
        becomeAngry(false);
    }

    /**
     * 让 TWR 变暴躁
     *
     * @param intense 是否迅速变得暴躁
     */
    protected void becomeAngry(boolean intense) {
        calmness.getAndAdd(-new Random().nextInt(0, intense ? 2 : 50));
        if (calmness.get() < 0) {
            calmness.set(0);
        }
    }

    /**
     * 用 深度优先搜索 寻找到从 TWR最近的路径点 出发到 目标路径点 的最短路径
     */
    protected Route searchRouteTo(WayPoint targetPoint) {
        return new BFS(startPoint.getNearestWayPoint((int) rect.getCenterX(), (int) rect.getCenterY()), targetPoint).search();
    }

    /**
     * 初始化 路径点图, 并设定图中一个路径点的引用
     */
    protected void initStartPoint() {
        WallGroup wallGroup = tankGroup.getGameMap().getWallGroup();
        var walls = wallGroup.getWalls();
        if (!walls.isEmpty()) {
            Rectangle wallRect = walls.get(0).getRect(); // 选取最左上角的墙块
            // 获取墙的尺寸作为步长
            int wallWidth = wallRect.width;
            int wallHeight = wallRect.height;
            int originalStartX = (int) wallRect.getCenterX(), startX = originalStartX;
            int originalStartY = (int) wallRect.getCenterY(), startY = originalStartY;
            // 选择没被墙挡住的路径点
            while (wallGroup.inWall(startX, startY)) {
                try {
                    do {
                        startX += wallWidth; // 横向扫描
                    } while (wallGroup.inWall(startX, startY));
                    // 找到点了
                    break;
                } catch (IllegalArgumentException e) { // 到达地图横轴边界之外, 扫描下一行
                    startX = originalStartX;
                    startY += originalStartY;
                }
            }
            // 走到此处则为找到点了
            startPoint = generateWayPointGraph(startX, startY, wallWidth, wallHeight);
        }
    }

    /**
     * 生成由 特定起点坐标 按照 特定步长 产生由路径点({@link WayPoint})组成的图(graph)
     *
     * @return 返回坦克所处的起始路径点
     * @apiNote 本方法在游戏开始后调用一次即可
     */
    public WayPoint generateWayPointGraph(int startX, int startY, int X_step, int Y_step) {
        WayPoint startPoint = WayPoint.getInstance(startX, startY);
        expandWayPointRecursively(startPoint, X_step, Y_step);
        return startPoint;
    }

    /**
     * 就地 递归 地拓展目标路径点及其刚被拓展的路径点
     */
    protected void expandWayPointRecursively(WayPoint p, int X_step, int Y_step) {
        Vector<WayPoint> points = expandWayPoint(p, X_step, Y_step);
        if (points.isEmpty()) {
            return;
        }
        for (WayPoint p1 : points) {
            expandWayPointRecursively(p1, X_step, Y_step);
        }
    }

    /**
     * 向八个方向拓展特定路径点. 坦克在这些路径点不会与墙相撞, 不会超出地图范围. 若已拓展过某个方向则不向该方向再次拓展
     *
     * @param X_step 产生路径点的横轴步长
     * @param Y_step 产生路径点的纵轴步长
     * @return 返回成功拓展的出的临路径点
     * @apiNote 变化是 就地 的
     */
    protected @NotNull Vector<WayPoint> expandWayPoint(@NotNull WayPoint p, int X_step, int Y_step) {
        WayPoint[] wayPoints = new WayPoint[]{
                WayPoint.getInstance(p.x - X_step, p.y - Y_step),
                WayPoint.getInstance(p.x, p.y - Y_step),
                WayPoint.getInstance(p.x + X_step, p.y - Y_step),
                WayPoint.getInstance(p.x - X_step, p.y),
                WayPoint.getInstance(p.x + X_step, p.y),
                WayPoint.getInstance(p.x - X_step, p.y + Y_step),
                WayPoint.getInstance(p.x, p.y + Y_step),
                WayPoint.getInstance(p.x + X_step, p.y + Y_step),
        };
        Vector<WayPoint> expanded = new Vector<>();
        outer:
        for (WayPoint p1 : wayPoints) {
            if (p1.x < 0 || p1.y < 0 || p1.y > tankGroup.getGameMap().getHeight() || p1.x > tankGroup.getGameMap().getWidth()) {
                // 路径点超出地图
                continue;
            }
            Vector<Wall> walls = tankGroup.getGameMap().getWallGroup().getWalls(p1.x, p1.y);
            if (walls == null) { // 四叉树中不包含该点
                continue;
            }
            for (Wall w : walls) {
                if (w.getRect().contains(p1.x, p1.y)) {
                    // 点在墙块内
                    continue outer;
                }
                // 模拟坦克走到路径点时会不会和墙块碰撞
                Rectangle simulateRect = new Rectangle(rect);
                simulateRect.setLocation(p1.x - rect.width / 2, p1.y - rect.height / 2);
                if (simulateRect.intersects(w.getRect())) {
                    continue outer;
                }
            }
            if (p.addNearPoint(p1)) {
                expanded.add(p1);
            }
        }
        return expanded;
    }

    /**
     * 设置坦克转向状态
     */
    public void setTurningKeys(boolean left, boolean right) {
        leftTurningKeyPressed.set(left);
        rightTurningKeyPressed.set(right);
    }

    /**
     * 设置坦克前进状态
     */
    public void setGoingKeys(boolean forward, boolean backward) {
        forwardGoingKeyPressed.set(forward);
        backwardGoingKeyPressed.set(backward);
    }

    /**
     * 添加 {@link Action}
     *
     * @param multipleInSameClass 若为 false 则一个 {@link Action} 类型只能存一个
     * @param override            当 multipleInSameClass 为 false 时生效, override 为 true 时, 新 {@link Action} 会覆盖原有 {@link Action},
     *                            override 为 false 时, 旧 {@link Action} 会被保留而新 {@link Action} 被忽视; 当 multipleInSameClass 为 true 时,
     *                            override 的值将被忽视
     */
    public void putAction(@NotNull Action action, boolean multipleInSameClass, boolean override) {
        if (!multipleInSameClass) {
            var clazz = action.getClass();
            if (override) {
                actions.removeIf((action1) -> action1.getClass().equals(clazz)); // 移除同类
                actions.add(action);
            } else {
                for (Action action1 : actions) {
                    if (action1.getClass().equals(clazz)) {
                        return;
                    }
                }
                actions.add(action); // 没有同类, 可以添加
            }
        } else {
            actions.add(action);

        }
    }

    public TankGroup getTankGroup() {
        return tankGroup;
    }
}
