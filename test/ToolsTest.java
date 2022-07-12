import org.junit.jupiter.api.Test;
import com.azazo1.util.Tools;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class ToolsTest {
    
    @Test
    void deepCopy() {
        BufferedImage img;
        try {
            img = ImageIO.read(new File("res/com.azazo1.tank.png"));
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
}