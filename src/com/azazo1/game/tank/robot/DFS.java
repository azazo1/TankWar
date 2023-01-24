package com.azazo1.game.tank.robot;

import com.azazo1.util.AtomicDouble;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 深度优先算法 寻找两个可互相到达的路径点之间的最短路径
 */
public class DFS {
    protected final WayPoint startPoint;
    protected final WayPoint endPoint;
    protected AtomicDouble minDistance = new AtomicDouble(Double.MAX_VALUE);
    protected volatile Route shortestRoute = null;

    /**
     * @param startPoint 起始点
     * @param endPoint   目标点
     */
    public DFS(@NotNull WayPoint startPoint, @NotNull WayPoint endPoint) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        HashSet<WayPoint> all = new HashSet<>();
        startPoint.extractReachableWayPoints(all);
        if (!all.contains(endPoint)) {
            throw new IllegalArgumentException("startPoint must be able to reach endPoint, but it isn't");
        }
    }

    /**
     * 由 参数point 开始, 进行目标为 endPoint 的深度优先搜索
     *
     * @param point 要搜索的路径点
     * @param route 本次搜索前的路径
     * @return null: 参数 point 已被搜索过, 且 point 所处路径无法到达 {@link DFS#endPoint}
     * @apiNote 本方法应由 {@link DFS#search()} 调用
     */
    protected @Nullable Route search(@NotNull WayPoint point, @NotNull Route route) {
        if (point.equals(endPoint)) {
            route.setNextWayPoint(point); // 延长路径
            return route;
        }
        // 深度搜索不适用 searched已搜索表, 因为非最短路径搜索过某个路径点会阻止该点下一次搜索
        if (route.contains(point)) { // 走到回头路了
            return null;
        }
        WayPoint lastPoint = route.getLastPoint();
        route.setNextWayPoint(point); // 延长路径
        for (WayPoint nearPoint : point.getNearPoints()) {
            if (nearPoint.equals(lastPoint)) { // 不走回头路
                continue;
            }
            Route newRoute = search(nearPoint, new Route(route)); // 传入备份路径
            if (newRoute != null && (shortestRoute == null || shortestRoute.getTotalDistance() < minDistance.get())) { // 筛选最短路径
                shortestRoute = newRoute;
                minDistance.set(shortestRoute.getTotalDistance());
            }
        }
        return shortestRoute;
    }

    /**
     * {@link DFS#search(WayPoint, Route)} 的 启动方法
     */
    public Route search() {
        minDistance.set(Double.MAX_VALUE);
        shortestRoute = null;
        return search(startPoint, new Route());
    }

    public static class Route implements Iterator<WayPoint> {
        /**
         * 路径, 0 位为起始点, 末位为目标点
         */
        protected final Vector<WayPoint> pointSequence = new Vector<>();
        protected final AtomicDouble totalDistance = new AtomicDouble(0);
        /**
         * 标记了下一个路径点所处的索引
         */
        protected AtomicInteger cursor = new AtomicInteger(0);

        public Route() {
        }

        public double getTotalDistance() {
            return totalDistance.get();
        }

        /**
         * 获得路径的一个备份, 不是 deepCopy
         */
        public Route(@NotNull Route route) {
            this.pointSequence.clear();
            this.pointSequence.addAll(route.pointSequence);
            this.totalDistance.set(route.totalDistance.get());
            this.cursor.set(route.cursor.get());
        }

        /**
         * 设置路径的下一个路径点
         */
        public void setNextWayPoint(WayPoint p) {
            if (pointSequence.isEmpty()) {
                pointSequence.add(p);
            } else {
                WayPoint lastPoint = pointSequence.get(pointSequence.size() - 1);
                totalDistance.set(lastPoint.distanceTo(p) + totalDistance.get());
                pointSequence.add(p);
            }
        }

        public WayPoint getStartPoint() {
            return pointSequence.get(0);
        }

        @Override
        public boolean hasNext() {
            return cursor.get() < pointSequence.size();
        }

        @Override
        public WayPoint next() {
            return pointSequence.get(cursor.getAndIncrement());
        }

        /**
         * 重置迭代器
         */
        public void resetCursor() {
            cursor.set(0);
        }

        /**
         * 获得路径的一个备份, 不是 deepCopy
         */
        public Route copy() {
            return new Route(this);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("distance=" + getTotalDistance() + ", ");
            for (WayPoint point : pointSequence) {
                builder.append("(%d, %d) ".formatted(point.x, point.y));
            }
            builder.deleteCharAt(builder.lastIndexOf(" "));
            return builder.toString();
        }

        /**
         * 获得路径上的最后一点
         *
         * @return null: 路径上没点
         */
        public @Nullable WayPoint getLastPoint() {
            if (!pointSequence.isEmpty()) {
                return pointSequence.get(pointSequence.size() - 1);
            }
            return null;
        }

        public boolean contains(WayPoint point) {
            return pointSequence.contains(point);
        }
    }
}
