package com.azazo1.wall;

import com.azazo1.GameMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.Vector;

public class WallGroup {
    protected Vector<Wall> walls = new Vector<>();
    protected GameMap map;
    
    public WallGroup() {
    }
    
    /**
     * 将 0 1 \n 组成的字符串(墙图表达式)解析为 {@link WallGroup}
     * 如:
     * 010<br>
     * 101<br>
     * 010<br>
     */
    @Contract("_, _, _ -> new")
    public static @NotNull WallGroup parseFromWallExpression(@NotNull String wallExpression, int screenWidth, int screenHeight) {
        String[] lines = wallExpression.strip().split("\n");
        checkWallExpression(lines, true);
        int h = lines.length;
        int w = lines[0].length();
        return new WallGroup() {{
            for (int i = 0; i < lines.length; i++) {
                for (int j = 0; j < lines[i].length(); j++) {
                    if (lines[i].charAt(j) == '1') {
                        addSingleWall(new Wall(
                                (int) ((screenWidth * 1.0) / w * j), (int) ((screenHeight * 1.0) / h * i), // i和j不需要减一来使左上角坐标处于正确位置,因为ij从0开始
                                (int) ((screenWidth * 1.0) / w), (int) (screenWidth * 1.0 / h)
                        ));
                    }
                }
            }
        }};
    }
    
    /**
     * 原理同 parseFromWallExpression,
     * 但储存介质变为黑白两色图片
     * 只有像素为 (0,0,0) 时才会被记为墙,alpha 通道被忽略
     */
    public static @NotNull WallGroup parseFromBinaryBitmap(@NotNull BufferedImage image, int screenWidth, int screenHeight) {
        Raster data = image.getData();
        int w = data.getWidth(), h = data.getHeight();
        return new WallGroup() {{
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    int[] array = new int[4];
                    data.getPixel(x, y, array);
                    if (array[0] == array[1] && array[1] == array[2] && array[2] == 0) {
                        addSingleWall(new Wall(
                                (int) ((screenWidth * 1.0) / w * x), (int) ((screenHeight * 1.0) / h * y), // x和y不需要减一来使左上角坐标处于正确位置,因为xy从0开始
                                (int) ((screenWidth * 1.0) / w), (int) (screenWidth * 1.0 / h)
                        ));
                    }
                }
            }
        }};
    }
    
    /**
     * @param _throws             是否报错
     * @param wallExpressionLines 由读取的 wallExpression 将每行分割后形成的字符串组
     */
    protected static boolean checkWallExpression(String[] wallExpressionLines, boolean _throws) {
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
                throw new IllegalArgumentException("Invalid com.azazo1.wall expression.");
            }
        }
        
        return valid;
    }
    
    /**
     * 不保证真的能从 map 中添加,本方法应由 com.azazo1.GameMap 调用
     */
    public void setGameMap(GameMap map) {
        this.map = map;
    }
    
    /**
     * 不保证真的能从 map 中去除,本方法应由 com.azazo1.GameMap 调用
     */
    public void clearGameMap() {
        this.map = null;
    }
    
    public void addSingleWall(Wall wall) {
        getWalls().add(wall);
    }
    
    public void update(Graphics graphics) {
        if (walls.isEmpty()) {
            return;
        }
        for (Wall wall : getWalls()) {
            wall.update(graphics.create());
        }
    }
    
    public Vector<Wall> getWalls() {
        return walls;
    }
    
    /**
     * 通过四叉树筛选出一部分墙，用于减少计算量
     */
    public Vector<Wall> getWalls(int x, int y) {
        return walls; // todo complete it.
    }
}
