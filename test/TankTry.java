import com.azazo1.util.Tools;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class TankTry {
    public BufferedImage rawTankImg = ImageIO.read(Tools.getFileURL("img/Tank.png"));
    public Rectangle tankRect = new Rectangle(rawTankImg.getWidth(), rawTankImg.getHeight());
    public TankDrawer canvas = new TankDrawer();
    public AtomicBoolean doPaint = new AtomicBoolean(true);
    public double orientation = 0; // 炮口方向,零为向右
    
    public TankTry() throws IOException {
    }
    
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            TankTry tank;
            try {
                tank = new TankTry();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            JFrame frame = new JFrame();
            frame.setLayout(new BorderLayout());
            
            frame.add(tank.canvas);
            
            Timer t = new Timer((int) (1.0 / 30 * 1000), (listener) -> tank.canvas.update(null));
            t.setRepeats(true);
            t.start();
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setBounds(10, 10, 500, 500);
        });
    }
    
    public void forward(double lengthInPixels) {
        tankRect.translate((int) (lengthInPixels * Math.cos(orientation)), (int) (lengthInPixels * Math.sin(orientation)));
    }
    
    public void rotate(double angleInRadians) {
        orientation += angleInRadians;
    }
    
    public class TankDrawer extends Canvas {
        public volatile HashMap<Character, Boolean> pressedMap;
        
        public TankDrawer() {
            super();
            pressedMap = new HashMap<>();
            pressedMap.put('a', false);
            pressedMap.put('s', false);
            pressedMap.put('d', false);
            pressedMap.put('w', false);
            addMouseMotionListener(new MouseMotionListener() {
                @Override
                public void mouseDragged(MouseEvent e) {
                
                }
                
                @Override
                public void mouseMoved(MouseEvent e) {
                    // 鼠标
                    double x = e.getPoint().getX();
                    double y = e.getPoint().getY();
                    // 坦克
                    double cx = tankRect.getCenterX();
                    double cy = tankRect.getCenterY();
                    // 由于Y轴反向而取负号
                    double dx = (x - cx), dy = (y - cy);
                    orientation = Math.atan2(dy, dx);
                }
            });
            // todo<notice> 到时按键分别设置是否按下的布尔变量,否则按键效果一卡一卡,也不能实现同时按多键
            addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {
                    if (e.getKeyChar() == 'z') doPaint.set(!doPaint.get());
                }
                
                @Override
                public void keyPressed(KeyEvent e) {
                    switch (e.getKeyChar()) {
                        case 'a' -> pressedMap.put('a', true);
                        case 's' -> pressedMap.put('s', true);
                        case 'd' -> pressedMap.put('d', true);
                        case 'w' -> pressedMap.put('w', true);
                    }
                }
                
                @Override
                public void keyReleased(KeyEvent e) {
                    switch (e.getKeyChar()) {
                        case 'a' -> pressedMap.put('a', false);
                        case 's' -> pressedMap.put('s', false);
                        case 'd' -> pressedMap.put('d', false);
                        case 'w' -> pressedMap.put('w', false);
                    }
                }
            });
        }
        
        @Override
        public void update(Graphics g) {
            if (g == null) {
                g = getGraphics();
            }
            if (pressedMap.get('a')) rotate(Math.toRadians(-15));
            if (pressedMap.get('s')) forward(-10);
            if (pressedMap.get('d')) rotate(Math.toRadians(15));
            if (pressedMap.get('w')) forward(10);
            
            paint(g);
        }
        
        @Override
        public void paint(Graphics g) {
            if (!doPaint.get()) {
                return;
            }
            if (g == null) {
                g = getGraphics();
            }
            Graphics2D g2d = (Graphics2D) g;
            g.setColor(new Color(0xffffff));
            g.fillRect(0, 0, 500, 500);
            double dx = tankRect.getCenterX();
            double dy = tankRect.getCenterY();
            g2d.translate(dx, dy); // 移动初始位置
            g2d.rotate(orientation);
            // 注意负值使图像中点落在初始位置上,使旋转锚点正常
            g.drawImage(rawTankImg, (int) (-tankRect.getWidth() / 2), (int) (-tankRect.getHeight() / 2), (int) tankRect.getWidth(), (int) tankRect.getHeight(), (img, infoflags, x, y, width, height) -> {
                System.out.println("asd");
                return false;
            });
        }
    }
}
