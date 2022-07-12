import com.azazo1.wall.WallGroup;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

class WallGroupTest {
    
    public static void main(String[] args) throws IOException {
        WallGroup g = WallGroup.parseFromBinaryBitmap(ImageIO.read(new File("res/WallMap.png")), 500, 500);
        System.out.println(g);
    }
}