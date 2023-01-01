package com.azazo1.online.client.wall;

import com.azazo1.game.wall.WallGroup;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;

public class ClientWallGroup extends WallGroup {
    // 墙位置信息只在游戏开局共享 (共享墙图文件)

    public ClientWallGroup() {
        super();
    }

    public ClientWallGroup(int qTreeDepth) {
        super(qTreeDepth);
    }

    /**
     * 将 墙图文件 解析为 {@link ClientWallGroup}
     *
     * @param image     黑白两色图片
     *                  只有像素为 (0,0,0) 时才会被记为墙<br>
     *                  最左上角的一个像素的 alpha 通道值 a 为四叉树深度 d = 255-a <br>
     *                  其他像素 alpha 通道被忽略<br>
     * @param mapWidth  要被映射到的地图像素尺寸
     * @param mapHeight 要被映射到的地图像素尺寸
     */
    public static @NotNull ClientWallGroup parseFromBitmap(@NotNull BufferedImage image, int mapWidth, int mapHeight) {
        Raster data = image.getData();
        int w = data.getWidth(), h = data.getHeight();
        return new ClientWallGroup(255 - data.getPixel(0, 0, (int[]) null)[3]) {{
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    int[] array = new int[4];
                    data.getPixel(x, y, array);
                    if (array[0] == array[1] && array[1] == array[2] && array[2] == 0) {
                        addSingleWall(new ClientWall((int) ((mapWidth * 1.0) / w * x), (int) ((mapHeight * 1.0) / h * y), // x和y不需要减一来使左上角坐标处于正确位置,因为xy从0开始
                                (int) ((mapWidth * 1.0) / w), (int) (mapWidth * 1.0 / h)));
                    }
                }
            }
        }};
    }
}
