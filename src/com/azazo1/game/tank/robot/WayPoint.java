package com.azazo1.game.tank.robot;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Random;
import java.util.Vector;

/**
 * 坦克行进的路径点, 路径图(g)上的一个元素
 */
public class WayPoint {
    public final int x;
    public final int y;
    protected final HashSet<WayPoint> nearPoints = new HashSet<>();
    protected static final Vector<WayPoint> allPoints = new Vector<>();

    private WayPoint(int x, int y) {
        synchronized (allPoints) {
            this.x = x;
            this.y = y;
            allPoints.add(this);
        }
    }


    private WayPoint(double x, double y) {
        synchronized (allPoints) {
            this.x = (int) x;
            this.y = (int) y;
            allPoints.add(this);
        }
    }

    public static void clearWayPoint() {
        synchronized (allPoints) {
            allPoints.clear();
        }
    }

    public static @NotNull WayPoint getInstance(int x, int y) {
        synchronized (allPoints) {
            for (WayPoint p : allPoints) { // 若此点已创建过则返回该点
                if (p.x == x && p.y == y) {
                    return p;
                }
            }
        }
        return new WayPoint(x, y);
    }

    /**
     * 生成路径点, 会将 "double" cast 到 "int"
     */
    public static @NotNull WayPoint getInstance(double x, double y) {
        return getInstance((int) x, (int) y);
    }

    public HashSet<WayPoint> getNearPoints() {
        return new HashSet<>(nearPoints);
    }

    /**
     * 添加临路径点
     *
     * @return true: 成功添加 / false: 该路径点已被添加过
     */
    public boolean addNearPoint(WayPoint wayPoint) {
        synchronized (nearPoints) {
            if (nearPoints.contains(wayPoint)) {
                return false;
            } else if (this.equals(wayPoint)) {
                return false;
            }
            nearPoints.add(wayPoint);
            wayPoint.addNearPoint(this);
            return true;
        }
    }

    /**
     * 计算两个路径点间的距离
     */
    public double distanceTo(@NotNull WayPoint p) {
        return Math.sqrt(Math.pow(p.x - x, 2) + Math.pow(p.y - y, 2));
    }

    /**
     * 计算路径点与特定坐标间的距离
     */
    public double distanceTo(int targetX, int targetY) {
        return Math.sqrt(Math.pow(targetX - x, 2) + Math.pow(targetY - y, 2));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WayPoint wayPoint = (WayPoint) o;

        if (x != wayPoint.x) return false;
        return y == wayPoint.y;
    }

    /**
     * 将自己显示在画面上 (仅作测试用)
     */
    public void draw(@NotNull Graphics g) {
        int diameter = 4;
        g.drawOval(x - diameter / 2, y - diameter / 2, diameter, diameter);
    }

    /**
     * 突出将自己显示在画面上 (仅作测试用)
     */
    public void drawHighlight(@NotNull Graphics g) {
        int diameter = 4;
        g.fillOval(x - diameter / 2, y - diameter / 2, diameter, diameter);
    }

    /**
     * 显示本点能到达的所有路径点 (仅作测试用)
     *
     * @param drawnPoints 已经绘制过的点
     * @apiNote 本方法应由 {@link WayPoint#drawAll(Graphics)} 调用
     */
    protected void drawAll(@NotNull Graphics g, @NotNull HashSet<WayPoint> drawnPoints) {
        synchronized (nearPoints) {
            if (!drawnPoints.contains(this)) {
                draw(g);
                drawnPoints.add(this);
                nearPoints.forEach((p) -> p.drawAll(g, drawnPoints));
            }
        }
    }

    /**
     * @see WayPoint#drawAll(Graphics, HashSet)
     */
    public void drawAll(@NotNull Graphics g) {
        drawAll(g, new HashSet<>());
    }

    /**
     * 获得从本路径点出发能到达的, 距离目标坐标最近的路径点
     *
     * @return 没创建过路径点, 则返回null
     */
    public @NotNull WayPoint getNearestWayPoint(int targetX, int targetY) {
        HashSet<WayPoint> get = new HashSet<>();
        extractReachableWayPoints(get);
        Vector<WayPoint> rst = new Vector<>(get);
        rst.sort((o1, o2) -> {
            double d1 = o1.distanceTo(targetX, targetY), d2 = o2.distanceTo(targetX, targetY);
            return Double.compare(d1, d2);
        });
        return rst.get(0);
    }

    /**
     * 提取本路径点能到达的所有路径点
     *
     * @param container 结果输出容器
     */
    public void extractReachableWayPoints(@NotNull HashSet<WayPoint> container) { // 这里用 synchronized 会死锁
        try {
            if (container.contains(this)) {
                return;
            }
            container.add(this);
            nearPoints.forEach((wayPoint -> wayPoint.extractReachableWayPoints(container)));
        } catch (ConcurrentModificationException ignore) { // 似乎只有游戏开始时拓展路径点才会报此错, 过后便不报此错
        }
    }

    @Override
    public String toString() {
        return "WayPoint{" +
                "(" + x +
                ", " + y +
                "), nearPointsNumber=" + nearPoints.size() +
                '}';
    }

    /**
     * 在图中与 (x, y) 距离 大于 minDistance 且 小于 maxDistance 的路径点中选取一个路径点
     *
     * @return null: 该范围内无符合条件的路径点
     */
    public WayPoint getOneNearPointRandomly(int x, int y, double minDistance, double maxDistance) {
        HashSet<WayPoint> container = new HashSet<>();
        extractReachableWayPoints(container);
        Vector<WayPoint> consider = new Vector<>(); // 可考虑的路径点
        for (WayPoint point : container) {
            double distance = point.distanceTo(x, y);
            if (minDistance < distance && distance < maxDistance) {
                consider.add(point);
            }
        }
        if (consider.isEmpty()) {
            return null;
        }
        return consider.get(new Random().nextInt(consider.size())); // 随机选取一点
    }
}
