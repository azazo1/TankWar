import com.azazo1.Config;
import com.azazo1.util.Tools;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ToolsTest {

    @Test
    void getFileURL() {
        assertNotNull(Tools.getFileURL("img/Bullet.png"));
    }

    @Test
    void getFileURLs() throws IOException {
        var fileURLs = Tools.getFileURLs("wallmap", (dir, name) -> name.endsWith(Config.WALL_MAP_FILE_SUFFIX)); // 获得所有墙图
        Tools.logLn("" + fileURLs);
        assertFalse(fileURLs.isEmpty());
    }

    @Test
    void deepCopy() {
        BufferedImage img;
        try {
            img = ImageIO.read(Tools.getFileURL("img/Tank.png").url());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BufferedImage get = Tools.deepCopy(img);
        assertEquals(get.getHeight(), img.getHeight());
        assertEquals(get.getWidth(), img.getWidth());

        Graphics g = get.getGraphics();
        g.fillRect(0, 0, get.getWidth(), get.getHeight());
        g.dispose();
        boolean equals = Arrays.equals(get.getData().getPixels(0, 0, get.getWidth(), get.getHeight(), (int[]) null),
                img.getData().getPixels(0, 0, img.getWidth(), img.getHeight(), (int[]) null));
        if (equals) {
            fail();
        }
    }

    @Test
    void playSound() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            Tools.playSound(Tools.getFileURL("sound/fire.mp3").url());
            Thread.sleep(50);
        }
    }

    @Test
    void delete() { // 只能在调试模式下运行否则会被乱删
        File target = new File("D:/Temp/新建文件夹/新建文件夹");
        if (target.isDirectory()) {
            Tools.deleteFileAndDir(target);
            if (target.exists()) {
                fail();
            }
        } else {
            fail();
        }

    }
}