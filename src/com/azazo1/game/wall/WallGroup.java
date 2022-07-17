package com.azazo1.game.wall;

import com.azazo1.Config;
import com.azazo1.base.ConstantVal;
import com.azazo1.game.GameMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

public class WallGroup {
    protected final AtomicInteger qTreeDepth = new AtomicInteger(Config.QUADTREE_DEPTH);
    protected final Vector<Wall> walls = new Vector<>();
    protected final Vector<Wall> wallsToDispatch = new Vector<>();
    protected QTreeWallStorage tree;
    protected GameMap map;
    
    public WallGroup() {
    }
    
    public WallGroup(int qTreeDepth) {
        this.qTreeDepth.set(qTreeDepth);
    }
    
    /**
     * 将 0 1 \n 组成的字符串(墙图表达式)解析为 {@link WallGroup}
     * 如:
     * 010<br>
     * 101<br>
     * 010<br>
     *
     * @param mapWidth   要被映射到的地图像素尺寸
     * @param mapHeight  要被映射到的地图像素尺寸
     * @param qTreeDepth 四叉树深度
     */
    public static @NotNull WallGroup parseFromWallExpression(@NotNull String wallExpression, int mapWidth, int mapHeight, int qTreeDepth) {
        String[] lines = wallExpression.strip().split("\n");
        checkWallExpression(lines, true);
        int h = lines.length;
        int w = lines[0].length();
        return new WallGroup(qTreeDepth) {{
            for (int i = 0; i < lines.length; i++) {
                for (int j = 0; j < lines[i].length(); j++) {
                    if (lines[i].charAt(j) == '1') {
                        addSingleWall(new Wall((int) ((mapWidth * 1.0) / w * j), (int) ((mapHeight * 1.0) / h * i), // i和j不需要减一来使左上角坐标处于正确位置,因为ij从0开始
                                (int) ((mapWidth * 1.0) / w), (int) (mapWidth * 1.0 / h)));
                    }
                }
            }
        }};
    }
    
    /**
     * 将 墙图文件 解析为 {@link WallGroup}
     *
     * @param image     黑白两色图片
     *                  只有像素为 (0,0,0) 时才会被记为墙<br>
     *                  最左上角的一个像素的 alpha 通道值 a 为四叉树深度 d = 255-a <br>
     *                  其他像素 alpha 通道被忽略<br>
     * @param mapWidth  要被映射到的地图像素尺寸
     * @param mapHeight 要被映射到的地图像素尺寸
     */
    public static @NotNull WallGroup parseFromBitmap(@NotNull BufferedImage image, int mapWidth, int mapHeight) {
        Raster data = image.getData();
        int w = data.getWidth(), h = data.getHeight();
        return new WallGroup(255 - data.getPixel(0, 0, (int[]) null)[3]) {{
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    int[] array = new int[4];
                    data.getPixel(x, y, array);
                    if (array[0] == array[1] && array[1] == array[2] && array[2] == 0) {
                        addSingleWall(new Wall((int) ((mapWidth * 1.0) / w * x), (int) ((mapHeight * 1.0) / h * y), // x和y不需要减一来使左上角坐标处于正确位置,因为xy从0开始
                                (int) ((mapWidth * 1.0) / w), (int) (mapWidth * 1.0 / h)));
                    }
                }
            }
        }};
    }
    
    public static @NotNull WallGroup parseFromBitmap(@NotNull BufferedImage image) {
        return parseFromBitmap(image, Config.MAP_WIDTH, Config.MAP_HEIGHT);
    }
    
    /**
     * 将四叉树深度保存到墙图文件中<br>
     * 此方法会将墙图文件 (0,0) 像素 的 rgb 值不变, 修改其 alpha 值
     */
    public static void setBitmapQTreeDepth(File bitmapFile, int depth) throws IOException {
        BufferedImage img = ImageIO.read(bitmapFile);
        WritableRaster copiedData = (WritableRaster) img.getData();
        int[] pixel = copiedData.getPixel(0, 0, (int[]) null);
        copiedData.setPixel(0, 0, new int[]{pixel[0], pixel[1], pixel[2], 255 - depth});
        img.setData(copiedData);
        ImageIO.write(img, "png", bitmapFile);
    }
    
    /**
     * @param _throws             是否报错
     * @param wallExpressionLines 由读取的 wallExpression 将每行分割后形成的字符串组
     */
    @SuppressWarnings({"UnusedReturnValue", "SameParameterValue"})
    protected static boolean checkWallExpression(@NotNull String[] wallExpressionLines, boolean _throws) {
        boolean valid = true;
        int length = -1;
        if (!(wallExpressionLines.length == 0)) {
            for (String line : wallExpressionLines) {
                if (length > 0 && length != line.length()) {
                    valid = false;
                    break;
                }
                length = line.length();
                if (length == 0) {
                    valid = false;
                    break;
                }
            }
        } else {
            valid = false;
        }
        if (!valid) {
            if (_throws) {
                throw new IllegalArgumentException("Invalid com.azazo1.game.wall expression.");
            }
        }
        
        return valid;
    }
    
    /**
     * 扫描当前文件夹下可用的游戏墙图文件
     */
    public static @Nullable Vector<File> scanBinaryBitmapFiles(String scanPath) {
        File f = new File(scanPath);
        String[] sons = f.list((dir, name) -> name.endsWith(ConstantVal.WALL_MAP_FILE_SUFFIX));
        if (sons == null) {
            return null;
        }
        Vector<File> rst = new Vector<>();
        for (String name : sons) {
            rst.add(new File(f.getPath() + File.separator + name));
        }
        return rst;
    }
    
    public static @Nullable Vector<File> scanBinaryBitmapFiles() {
        return scanBinaryBitmapFiles(".");
    }
    
    /**
     * 不保证真的能从 map 中添加,本方法应由 {@link GameMap} 调用
     * <p>
     * 此方法会将之前 ({@link #addSingleWall(Wall)}) 没有分配到四叉树内的墙进行分配
     */
    public void setGameMap(@NotNull GameMap map) {
        this.map = map;
        tree = new QTreeWallStorage(map.getWidth(), map.getHeight(), qTreeDepth.get());
        for (Wall w : wallsToDispatch) {
            tree.dispatch(w);
        }
        wallsToDispatch.clear();
    }
    
    /**
     * 不保证真的能从 map 中去除,本方法应由 {@link GameMap} 调用
     */
    public void clearGameMap() {
        this.map = null;
        tree.dispose();
        tree = null;
    }
    
    /**
     * 将墙添加到列表和四叉树(尝试)中<br>
     * 如果未能成功添加到四叉树上则将墙加入等待列表 {@link #wallsToDispatch}
     */
    public void addSingleWall(Wall wall) {
        walls.add(wall);
        if (tree != null) {
            tree.dispatch(wall);
        } else {
            wallsToDispatch.add(wall);
        }
    }
    
    public void update(Graphics graphics) {
        if (walls.isEmpty()) {
            return;
        }
        for (Wall wall : walls) {
            wall.update(graphics.create());
        }
    }
    
    /**
     * 获得墙列表拷贝
     */
    @NotNull
    public Vector<Wall> getWalls() {
        return new Vector<>(walls);
    }
    
    /**
     * 通过四叉树筛选出一部分墙，用于减少计算量
     */
    @Nullable
    public Vector<Wall> getWalls(int x, int y) {
        try {
            QNode leave = tree.switchLeave(new Point(x, y));
            if (leave == null) {
                return null;
            } else {
                return leave.getWalls();
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    public int getQTreeDepth() {
        return qTreeDepth.get();
    }
}
