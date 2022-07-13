package com.azazo1.game.wall;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Vector;

/**
 * 对应屏幕四个区域
 * 12
 * 34
 */
class QNode {
    private final int curDepth; // 当前节点深度 0 始
    private final Rectangle rect = new Rectangle(); // 当前节点表示的区域
    private Vector<Wall> walls; // 只有叶节点才能拥有
    private QNode first;
    private QNode second;
    private QNode third;
    private QNode fourth;
    
    public QNode(@NotNull Rectangle rect, int curDepth) {
        if (rect.width == 0 || rect.height == 0) {
            throw new IllegalArgumentException("Illegal rect size 0");
        }
        this.curDepth = curDepth;
        this.rect.setRect(rect);
    }
    
    /**
     * 新建对象填充四个子节点
     */
    public void fillSons() {
        Rectangle subRect = new Rectangle(rect);
        int halfW = rect.width / 2, halfH = rect.height / 2;
        subRect.setSize(halfW, halfH);
        setFirst(new QNode(subRect, curDepth + 1));
        subRect.translate(halfW, 0);
        setSecond(new QNode(subRect, curDepth + 1));
        subRect.translate(0, halfH);
        setForth(new QNode(subRect, curDepth + 1));
        subRect.translate(-halfW, 0);
        setThird(new QNode(subRect, curDepth + 1));
    }
    
    /**
     * 填充子节点，直到指定深度, 此方法会标记节点为叶节点
     */
    public void fillSonsToDepth(int depth) {
        if (depth < 1) {
            throw new IllegalArgumentException("depth must >= 1"); // == 1 时可以将此节点标记为叶节点
        }
        if (curDepth >= depth - 1) {
            walls = new Vector<>();
            return;
        }
        fillSons();
        for (int i = 0; i < 4; i++) {
            get(i).fillSonsToDepth(depth);
        }
    }
    
    /**
     * 将 {@link Wall} 对象分配到 {@link QNode} 叶节点
     *
     * @param seq 本节点在兄弟中的序号 0 始, -1 表示本节点为根节点
     * @return 添加路线，若未被添加到为 -1<br>
     * 如 int(2进制): 1 01 10 11 01<br>
     * 则代表路线为 root->second->forth->third->second (从低位向高位，不包括根节点, 最高位 1 是标志符)
     */
    public int dispatch(Wall wall, int seq) {
        if (!inThisNode(wall)) {
            return -1;
        }
        int dispatched = -1;
        for (int i = 0; i < 4; i++) {
            QNode son = get(i);
            if (son == null) {
                continue;
            }
            int ret = son.dispatch(wall, i);
            if (ret >= 0) {
                dispatched = seq >= 0 ? (ret << 2) | seq : ret;
                break;
            }
        }
        if (dispatched < 0) {
            if (walls != null) { // 是叶节点
                walls.add(wall);
                return 0b100 | seq; // 0b100为标志位
            }
            return -1;
        }
        return dispatched;
    }
    
    /**
     * 选择包含指定坐标的叶节点
     */
    @Nullable
    public QNode switchLeave(Point pos) {
        if (!inThisNode(pos)) {
            return null;
        }
        if (isLeave()) {
            return this;
        } else {
            for (int i = 0; i < 4; i++) {
                QNode son = get(i);
                QNode rst = son.switchLeave(pos);
                if (rst != null) {
                    return rst;
                }
            }
            return null;
        }
    }
    
    
    /**
     * 判断该 {@link Wall} 对象是否可以分配到此节点
     */
    public boolean inThisNode(@NotNull Wall wall) {
        Rectangle wRect = wall.getRect();
        return rect.contains(wRect.getCenterX(), wRect.getCenterY());
    }
    
    /**
     * 判断该 {@link Point} 对象是否可以分配到此节点
     */
    public boolean inThisNode(@NotNull Point pos) {
        return rect.contains(pos.getX(), pos.getY());
    }
    
    public boolean isLeave() {
        return walls != null;
    }
    
    /**
     * 取消对子节点的引用
     */
    public void dispose() {
        first = second = third = fourth = null;
    }
    
    public QNode getFirst() {
        return first;
    }
    
    public void setFirst(QNode first) {
        this.first = first;
    }
    
    public QNode getSecond() {
        return second;
    }
    
    public void setSecond(QNode second) {
        this.second = second;
    }
    
    public QNode getThird() {
        return third;
    }
    
    public void setThird(QNode third) {
        this.third = third;
    }
    
    public QNode getForth() {
        return fourth;
    }
    
    public void setForth(QNode fourth) {
        this.fourth = fourth;
    }
    
    public int getCurDepth() {
        return curDepth;
    }
    
    public Rectangle copyRect() {
        return new Rectangle(rect);
    }
    
    public QNode get(int i) {
        return switch (i) {
            case 0 -> first;
            case 1 -> second;
            case 2 -> third;
            case 3 -> fourth;
            default -> null;
        };
    }
    
    public QNode[] getSons() {
        return new QNode[]{first, second, third, fourth};
    }
    
    public Vector<Wall> getWalls() throws IllegalAccessException {
        if (!isLeave()) {
            throw new IllegalAccessException("Trying to get walls on a not-leave QNode.");
        }
        return new Vector<>(walls);
    }
}

public class QTreeWallStorage extends QNode {
    public final int width;
    public final int height;
    
    
    public QTreeWallStorage(int width, int height, int treeDepth) {
        super(new Rectangle(width, height), 0);
        this.height = height;
        this.width = width;
        fillSonsToDepth(treeDepth);
    }
    
    public QTreeWallStorage(int width, int height) {
        this(width, height, 2); // 只创建一层子节点
    }
    
    public int dispatch(Wall wall) {
        return dispatch(wall, -1);
    }
    
    /**
     * 将 int(2进制) 路线转化成对应 QNode 数组, 从前向后深度增加
     *
     * @param route 参见{{@link #dispatch(Wall, int)}} 的返回值
     */
    public QNode[] convertToArray(int route) {
        int length = (Integer.toBinaryString(route).length() - 1) / 2; // 得到的 binaryString.length 正常来说为奇数
        QNode[] rst = new QNode[length];
        for (int i = 0; i < length; i++) {
            int seq = route & 0b11;
            route >>= 2;
            rst[i] = i - 1 < 0 ? get(seq) : rst[i - 1].get(seq); // 从上一层 QNode 中取
        }
        return rst;
    }
    
    /**
     * 将 int(2进制) 路线转化成对应 QNode 叶节点
     *
     * @param route 参见{{@link #dispatch(Wall, int)}} 的返回值
     */
    public QNode getLeave(int route) {
        int length = (Integer.toBinaryString(route).length() - 1) / 2; // 得到的 binaryString.length 正常来说为奇数
        QNode rst = null;
        for (int i = 0; i < length; i++) {
            int seq = route & 0b11;
            route >>= 2;
            rst = i - 1 < 0 ? get(seq) : rst.get(seq); // 从上一层 QNode 中取
        }
        return rst;
    }
}
