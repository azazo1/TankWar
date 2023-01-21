package com.azazo1.game.item;

import com.azazo1.base.CharWithRectangle;
import com.azazo1.game.tank.TankBase;
import com.azazo1.game.wall.Wall;
import com.azazo1.util.Tools;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.Random;
import java.util.Vector;

/**
 * “物品”: 在场上随机出现, 捡到的坦克可获得一些特定效果
 */
public class ItemBase implements CharWithRectangle {
    public static final String imgFile = "img/RecoverBulletItem.png";
    protected volatile BufferedImage img = Tools.loadImg(imgFile);

    protected final Rectangle rect = new Rectangle(0, 0, 25, 25);
    protected volatile ItemGroup itemGroup;


    public ItemGroup getItemGroup() {
        return itemGroup;
    }

    public ItemBase(int centerX, int centerY) {
        rect.translate((int) (centerX - rect.width * 1.0 / 2), (int) (centerY - (rect.height * 1.0 / 2)));
    }

    @Override
    public Rectangle getRect() {
        return rect;
    }

    /**
     * 随机传送到一个位置, 在这个位置, 该物品不和墙块/坦克重叠
     *
     * @apiNote 要在被添加到 {@link ItemGroup}, 且 {@link com.azazo1.game.GameMap} 配置完毕后使用
     */
    public void randomlyTeleport() {
        Rectangle rectBak = new Rectangle(rect);
        Random random = new Random();
        int mapWidth = itemGroup.getGameMap().getWidth();
        int mapHeight = itemGroup.getGameMap().getHeight();
        int newX = random.nextInt(mapWidth), newY = random.nextInt(mapHeight);
        rect.setLocation(newX, newY);
        if (detectCollision()) {
            rect.setRect(rectBak);
            randomlyTeleport(); // 重试直到没有发生碰撞
        }
    }

    public boolean detectCollision() {
        //检查墙块
        Vector<Wall> walls = itemGroup.getGameMap().getWallGroup().getWalls((int) rect.getCenterX(), (int) rect.getCenterY());
        if (walls != null) {
            for (Wall wall : walls) {
                if (wall.getRect().intersects(rect)) {
                    return true;
                }
            }
        }
        // 检查坦克
        Vector<TankBase> tanks = itemGroup.getGameMap().getTankGroup().getLivingTanks();
        if (tanks != null) {
            for (TankBase tank : tanks) {
                if (tank.getRect().intersects(rect)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 设置本物品所属的 group, 本操作不保证真的从 group 对象中添加本物品
     * 本方法应由 {@link ItemGroup#addItem(ItemBase)} 调用
     */
    public void setItemGroup(ItemGroup itemGroup) {
        this.itemGroup = itemGroup;
    }

    /**
     * 清除本物品所属的 group, 本操作不保证真的从 group 对象中移除本物品
     * 本方法应由 {@link ItemGroup#removeItem(ItemBase)} 调用
     */
    public void clearItemGroup() {
        this.itemGroup = null;

    }

    /**
     * 当坦克检测到拾取到该物品, 会发挥该物品特点, 并删除该物品
     */
    public void update(Graphics g) {
        paint(g);
    }

    /**
     * 是否绘制由 g 否不为 null 控制
     */
    public void paint(Graphics g) {
        if (g != null) {
            g.drawImage(img, rect.x, rect.y, (int) rect.getWidth(), (int) rect.getHeight(), null);
        }
    }

    public ItemInfo getInfo() {
        return new ItemInfo(this);
    }

    public static class ItemInfo implements Serializable {
        protected final Rectangle rect;
        protected final Class<? extends ItemBase> itemType;

        public Rectangle getRect() {
            return rect;
        }

        public Class<? extends ItemBase> getItemType() {
            return itemType;
        }

        public ItemInfo(@NotNull ItemBase item) {
            this.rect = item.getRect();
            this.itemType = item.getClass();
        }
    }

    /**
     * 坦克捡到后发生的效果, 子类继承此方法以发挥不同效果
     */
    public void finishAndEffect(TankBase tank) {
        itemGroup.removeItem(this);
    }
}
