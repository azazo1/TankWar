package com.azazo1.game.tank.robot;

import com.azazo1.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Vector;

/**
 * 广度优先算法 寻找两个可互相到达的路径点之间的最短路径 (很快！)
 */
public class BFS {
    protected final WayPoint startPoint;
    protected final WayPoint endPoint;
    /**
     * 已经探索过的路径点
     */
    protected final HashSet<WayPoint> explored = new HashSet<>();
    /**
     * 等待被探索的路径点
     */
    protected final Vector<Pair<WayPoint, Route>> toBeExplored = new Vector<>() {
        @Override
        public void add(int index, Pair<WayPoint, Route> element) {
            throw new IllegalAccessError("Can not add it to middle");
        }

        @Override
        public synchronized Pair<WayPoint, Route> get(int index) {
            if (index != 0) {
                throw new IllegalAccessError("Can not get it from middle");
            }
            return super.get(index);
        }

        @Override
        public boolean remove(Object o) {
            throw new IllegalAccessError("Can not remove it");
        }

        @Override
        public Pair<WayPoint, Route> remove(int index) {
            if (index != 0) {
                throw new IllegalAccessError("Can not get it from middle");
            }
            return super.remove(index);
        }
    };
    protected volatile Route shortestRoute = null;

    /**
     * @param startPoint 起始点
     * @param endPoint   目标点
     */
    public BFS(@NotNull WayPoint startPoint, @NotNull WayPoint endPoint) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        HashSet<WayPoint> all = new HashSet<>();
        startPoint.extractReachableWayPoints(all);
        if (!all.contains(endPoint)) {
            throw new IllegalArgumentException("startPoint must be able to reach endPoint, but it isn't");
        }
    }

    /**
     * 由 参数point 开始, 进行目标为 endPoint 的广度优先搜索
     *
     * @param point 要探索的路径点
     * @param route 本次探索前的路径
     * @return null: 参数 point 已被探索过
     * @apiNote 本方法应由 {@link DFS#search()} 启动
     */
    protected @Nullable Route explore(@NotNull WayPoint point, @NotNull Route route) {
        if (point.equals(endPoint)) {
            route.setNextWayPoint(point); // 延长路径
            return route;
        }
        if (explored.contains(point)) { // 探索过了
            return null;
        }
        WayPoint lastPoint = null;
        try {
            lastPoint = route.getLastPoint();
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
        explored.add(point);
        route.setNextWayPoint(point); // 延长路径
        for (WayPoint nearPoint : point.getNearPoints()) {
            if (nearPoint.equals(lastPoint)) { // 不走回头路
                continue;
            }
            toBeExplored.add(new Pair<>(nearPoint, new Route(route))); // 传入备份路径
        }
        return null; // 没到终点
    }

    /**
     * {@link BFS#explore(WayPoint, Route)} 的 启动方法
     *
     * @return null: 找不到路径
     */
    public Route search() {
        toBeExplored.add(new Pair<>(startPoint, new Route()));
        Route route = null;
        while (!toBeExplored.isEmpty()) {
            Pair<WayPoint, Route> pair = toBeExplored.remove(0); // 移除的就是这个
            if ((route = explore(pair.first, pair.second)) != null) {
                break;
            }
        }
        toBeExplored.clear();
        return route;
    }
}
