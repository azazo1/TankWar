import com.azazo1.game.wall.WallGroup;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

class WallGroupTest {
    
    public static void main(String[] args) throws IOException {
        WallGroup g = WallGroup.parseFromBinaryBitmap(ImageIO.read(new File("res/WallMap.png")), 500, 500);
        WallGroup g1 = WallGroup.parseFromWallExpression("110\n011", 500, 500);
        System.out.println(g);
        System.out.println(g1);
    }
}