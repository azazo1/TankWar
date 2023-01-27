package com.azazo1.game.tank.robot;

import com.azazo1.util.AtomicDouble;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 由路径点组成的路径
 */
public class Route implements Iterator<WayPoint> {
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
     */
    public WayPoint getLastPoint() {
        return pointSequence.get(pointSequence.size() - 1);
    }

    public boolean contains(WayPoint point) {
        return pointSequence.contains(point);
    }
}
