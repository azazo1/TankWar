package com.azazo1.base;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;

public class Config {
    public static final int FPS = 60;
    public static final Color BACKGROUND_COLOR = new Color(0xffffff);
    public static final Color TEXT_COLOR = new Color(0x100E0E);
    public static final int MAP_WIDTH = 500; // 游戏画布尺寸 pixels
    public static final int MAP_HEIGHT = 500;// 游戏画布尺寸 pixels
    
    public static final HashMap<Integer, TankAction> TANK_MOTION_KEY_MAP_1_ST = new HashMap<>() {
        {
            put(KeyEvent.VK_A, TankAction.LEFT_TURNING);
            put(KeyEvent.VK_D, TankAction.RIGHT_TURNING);
            put(KeyEvent.VK_S, TankAction.BACKWARD_GOING);
            put(KeyEvent.VK_W, TankAction.FORWARD_GOING);
            put(KeyEvent.VK_SPACE, TankAction.FIRE);
        }
    };
    public static final int TANK_MAX_ENDURANCE = 3; // 默认坦克最大生命值
    public static final int TANK_INJURED_INTERVAL_MILLIS = 200; // 坦克受伤时间间隔 ms
}
