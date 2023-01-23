package com.azazo1.game.tank.robot;

import com.azazo1.game.tank.TankBase;
import com.azazo1.game.wall.Wall;
import com.azazo1.game.wall.WallGroup;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Vector;


public class RobotTank extends TankBase {
    protected volatile WayPoint startPoint;
    protected volatile TankBase targetTank;

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
        // 搜索到目标坦克最短路径 todo bug:不是最短
        Rectangle targetTankRect = targetTank.getRect();
        WayPoint targetPoint /* 距离目标坦克最近的路径点 */ = startPoint.getNearestWayPoint((int) targetTankRect.getCenterX(), (int) targetTankRect.getCenterY());
        DFS.Route route = searchRouteTo(targetPoint);
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
        startPoint.getNearestWayPoint((int) rect.getCenterX(), (int) rect.getCenterY()).drawHighlight(g1);
        super.update(g);
    }

    /**
     * 用 深度优先搜索 寻找到从 TWR最近的路径点 出发到 目标路径点 的最短路径
     */
    protected DFS.Route searchRouteTo(WayPoint targetPoint) {
        return new DFS(startPoint.getNearestWayPoint((int) rect.getCenterX(), (int) rect.getCenterY()), targetPoint).search();
    }

    /**
     * 初始化 路径点图, 并设定图中一个路径点的引用
     */
    private void initStartPoint() {
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
}
