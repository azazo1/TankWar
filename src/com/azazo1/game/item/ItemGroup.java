package com.azazo1.game.item;

import com.azazo1.game.GameMap;
import com.azazo1.util.IntervalTicker;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;
import java.util.Vector;

/**
 * {@link ItemGroup} 在服务端不需要特别设置
 * 在客户端要接收服务器 items 并同步
 */
public class ItemGroup {
    protected final Vector<ItemBase> items = new Vector<>();
    protected GameMap map;
    protected static final Vector<Class<? extends ItemBase>> itemTypes = new Vector<>() {{
        // 注册物品类型
        add(RecoverBulletItem.class);
        add(FastBulletItem.class);
        add(BunshinBulletItem.class);
    }};

    /**
     * 用于随机生成物品
     */
    protected volatile IntervalTicker ticker = new IntervalTicker(3000);

    public void addItem(ItemBase itemBase) {
        items.add(itemBase);
        itemBase.setItemGroup(this);
    }

    public Vector<ItemBase> getItems() {
        return new Vector<>(items);
    }

    public void update(Graphics graphics) {
        synchronized (items) { // 防止 Client 模式多线程时闪烁
            // 尝试产生物品
            if (!itemTypes.isEmpty() && ticker.judgeCanExecute()) {
                Class<? extends ItemBase> T = randomlyChooseItemType();
                Constructor<? extends ItemBase> constructor = null;
                if (T != null) {
                    try {
                        constructor = T.getConstructor(int.class, int.class);
                        ItemBase item = constructor.newInstance(0, 0);
                        addItem(item);
                        item.randomlyTeleport();
                    } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                             IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            if (graphics == null) {
                for (ItemBase item : items) {
                    item.update(null);
                }
            } else {
                for (ItemBase item : items) {
                    item.update(graphics.create());
                }
            }
        }
    }

    private @Nullable Class<? extends ItemBase> randomlyChooseItemType() {
        if (itemTypes.isEmpty()) {
            return null;
        }
        int index = new Random().nextInt(0, itemTypes.size());
        return itemTypes.get(index);
    }

    public void removeItem(ItemBase item) {
        items.remove(item);
        item.clearItemGroup();
    }

    public GameMap getGameMap() {
        return map;
    }

    /**
     * 不保证真的能从 map 中添加,本方法应由 {@link GameMap#setItemGroup(ItemGroup)} 调用
     */
    public void setGameMap(GameMap map) {
        this.map = map;
    }

    /**
     * 不保证真的能从 map 中去除,本方法应由 {@link GameMap#setItemGroup(ItemGroup)} 调用
     */
    public void clearGameMap() {
        this.map = null;
    }

    /**
     * 获得物品数
     */
    public int getItemCount() {
        return items.size();
    }

    public Vector<ItemBase.ItemInfo> getItemInfos() {
        Vector<ItemBase.ItemInfo> v = new Vector<>();
        for (ItemBase item : items) {
            v.add(item.getInfo());
        }
        return v;
    }
}
