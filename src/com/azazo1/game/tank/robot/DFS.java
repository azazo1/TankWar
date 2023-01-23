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
    protected final HashSet<WayPoint> searched = new HashSet<>();

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
     * @return null: 参数 point 已被搜索过, 且 point 所处路径无法到达 {@link DFS#endPoint}
     * @apiNote 本方法应由 {@link DFS#search()} 调用
     */
    protected @Nullable Route search(@NotNull WayPoint point, @NotNull Route route) {
        if (point.equals(endPoint)) {
            return route;
        }
        if (searched.contains(point)) {
            return null;
        }
        searched.add(point);
        double minDistance = Double.MAX_VALUE;
        Route shortestRoute = null;
        for (WayPoint nearPoint : point.getNearPoints()) {
            Route newRoute = search(nearPoint, new Route(route) {{ // 传入备份路径
                setNextWayPoint(nearPoint); // 延长路径
            }});
            if (newRoute != null && (shortestRoute == null || shortestRoute.getTotalDistance() < minDistance)) { // 筛选最短路径
                shortestRoute = newRoute;
                minDistance = shortestRoute.getTotalDistance();
            }
        }
        return shortestRoute;
    }

    /**
     * {@link DFS#search(WayPoint, Route)} 的 启动方法
     */
    public Route search() {
        return search(startPoint, new Route(startPoint));
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

        public Route(WayPoint startPoint) {
            pointSequence.add(startPoint);
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
            WayPoint lastPoint = pointSequence.get(pointSequence.size() - 1);
            totalDistance.set(lastPoint.distanceTo(p) + totalDistance.get());
            pointSequence.add(p);
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
    }
}
