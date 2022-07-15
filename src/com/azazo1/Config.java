package com.azazo1;

import com.azazo1.base.TankAction;
import com.azazo1.ui.Translation;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Vector;

public final class Config {
    public static final int FPS = 60;
    public static final Font TEXT_FONT = new Font("楷体", Font.PLAIN, 15);
    public static final Font TEXT_FONT_FOCUSED = new Font("楷体", Font.BOLD, 15);
    public static final Font TANK_SEQ_FONT = new Font("楷体", Font.BOLD, 15); // 坦克上的序号字体
    public static final Font TANK_CLIP_FONT = new Font("楷体", Font.BOLD, 7); // 坦克上的序号字体
    public static final Color BACKGROUND_COLOR = new Color(0xffffff);
    public static final Color TEXT_COLOR = new Color(0x100E0E);
    public static final Color BORDER_COLOR = new Color(0x9F9F9F);
    public static final Color TANK_SEQ_COLOR = new Color(0xFF0000); // 坦克上的序号字色
    public static final Color TANK_CLIP_COLOR = TANK_SEQ_COLOR; // 坦克弹夹颜色
    public static final int QUADTREE_DEPTH = 2; // 默认四叉树深度
    public static final int MAP_WIDTH = 500; // 游戏画布尺寸 pixels
    public static final int MAP_HEIGHT = 500;// 游戏画布尺寸 pixels
    public static final int WINDOW_WIDTH = 750; // 界面窗口宽度
    public static final int WINDOW_HEIGHT = 550; // 界面窗口高度
    public static final Vector<HashMap<Integer, TankAction>> TANK_ACTION_KEY_MAPS = new Vector<>() {{
        add(new HashMap<>() {
            {
                put(KeyEvent.VK_A, TankAction.LEFT_TURNING);
                put(KeyEvent.VK_D, TankAction.RIGHT_TURNING);
                put(KeyEvent.VK_S, TankAction.BACKWARD_GOING);
                put(KeyEvent.VK_W, TankAction.FORWARD_GOING);
                put(KeyEvent.VK_SPACE, TankAction.FIRE);
            }
        }); // 一号坦克按键映射
        add(new HashMap<>() {
            {
                put(KeyEvent.VK_LEFT, TankAction.LEFT_TURNING);
                put(KeyEvent.VK_RIGHT, TankAction.RIGHT_TURNING);
                put(KeyEvent.VK_DOWN, TankAction.BACKWARD_GOING);
                put(KeyEvent.VK_UP, TankAction.FORWARD_GOING);
                put(KeyEvent.VK_NUMPAD0, TankAction.FIRE);
            }
        });// 二号坦克按键映射
    }};
    public static final int TANK_MAX_FIRE_CAPACITY = 5; // 坦克弹夹容量
    public static final int TANK_BULLET_INCREMENT_INTERVAL_MILLIS = 1000; // 坦克弹夹子弹数增加间隔
    public static final int TANK_MAX_ENDURANCE = 3; // 默认坦克最大生命值
    public static final int TANK_INJURED_INTERVAL_MILLIS = 200; // 坦克受伤时间间隔 ms
    public static final Translation translation = Translation.Chinese.instance;
    public static final int QUIT_GAME_KEY = KeyEvent.VK_ESCAPE; // 退出该局游戏键
    
    private Config() {
    }
}
