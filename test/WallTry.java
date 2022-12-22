import com.azazo1.util.Tools;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class WallTry implements Runnable {
    public BufferedImage wallImg = ImageIO.read(Tools.getFileURL("img/SingleWall.png").url());
    public BufferedImage tankImg = ImageIO.read(Tools.getFileURL("img/Tank.png").url());
    
    public WallTry() throws IOException {
    }
    
    public Canvas createCanvas() {
        return new Canvas() {
            @Override
            public void paint(Graphics g) {
                if (g == null) {
                    g = getGraphics();
                }
                g.setColor(new Color(0xC4C4C4));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.drawImage(wallImg, 0, 0, null);
                g.drawImage(tankImg, 0, 0, null);
            }
        };
    }
    
    @Override
    public void run() {
        JFrame frame = new JFrame();
        frame.setLayout(new BorderLayout());
        
        
        frame.add(createCanvas(), BorderLayout.CENTER);
        
        frame.setVisible(true);
        frame.setBounds(10, 10, 500, 500);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
    }
    
    public static void main(String[] args) throws IOException {
        EventQueue.invokeLater(new WallTry());
    }
}
