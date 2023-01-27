package com.azazo1;

import com.azazo1.base.TankAction;
import com.azazo1.online.server.toclient.Server;
import com.azazo1.ui.Translation;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

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
    public static final Color TANK_NAME_YOU_COLOR = new Color(0x00FF00); // Client: 坦克 "你" 的名称颜色
    public static final Color TANK_NAME_COLOR = new Color(0xFF0000); // Client: 坦克的名称颜色
    public static final Color TANK_CLIP_YOU_COLOR = new Color(0x00FF00); // Client: 坦克 "你" 的弹夹颜色
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
                put(KeyEvent.VK_SHIFT, TankAction.FIRE); // 横排数字 1
            }
        }); // 一号坦克按键映射
        add(new HashMap<>() {
            {
                put(KeyEvent.VK_G, TankAction.LEFT_TURNING);
                put(KeyEvent.VK_J, TankAction.RIGHT_TURNING);
                put(KeyEvent.VK_H, TankAction.BACKWARD_GOING);
                put(KeyEvent.VK_Y, TankAction.FORWARD_GOING);
                put(KeyEvent.VK_SPACE, TankAction.FIRE);
            }
        });// 二号坦克按键映射
        add(new HashMap<>() {
            {
                put(KeyEvent.VK_LEFT, TankAction.LEFT_TURNING);
                put(KeyEvent.VK_RIGHT, TankAction.RIGHT_TURNING);
                put(KeyEvent.VK_DOWN, TankAction.BACKWARD_GOING);
                put(KeyEvent.VK_UP, TankAction.FORWARD_GOING);
                put(KeyEvent.VK_PERIOD, TankAction.FIRE); // 句号
            }
        });// 三号坦克按键映射
        add(new HashMap<>() {
            {
                put(KeyEvent.VK_NUMPAD4, TankAction.LEFT_TURNING);
                put(KeyEvent.VK_NUMPAD6, TankAction.RIGHT_TURNING);
                put(KeyEvent.VK_NUMPAD5, TankAction.BACKWARD_GOING);
                put(KeyEvent.VK_NUMPAD8, TankAction.FORWARD_GOING);
                put(KeyEvent.VK_NUMPAD0, TankAction.FIRE);
            }
        });// 四号坦克按键映射
    }};
    public static final int TANK_MAX_FIRE_CAPACITY = 5; // 坦克弹夹容量
    public static final int TANK_BULLET_INCREMENT_INTERVAL_MILLIS = 1000; // 坦克弹夹子弹数增加间隔
    public static final int TANK_MAX_ENDURANCE = 3; // 默认坦克最大生命值
    public static final int TANK_INJURED_INTERVAL_MILLIS = 200; // 坦克受伤时间间隔 ms
    public static final Translation translation = Translation.Chinese.instance;
    /**
     * 退出该局游戏按键
     */
    public static final int QUIT_GAME_KEY = KeyEvent.VK_ESCAPE;
    /**
     * 服务端套接字超时 ms
     */
    public static final int SERVER_SOCKET_TIMEOUT = 500;
    /**
     * 聊天字数限制
     */
    public static final int TALKING_WORDS_LIMIT = 50;
    /**
     * 客户端处理器对应套接字超时时间 ms<br>
     * 客户端较多, 超时设置短些
     *
     * @apiNote 在服务端使用, 不知道为什么当服务端进入 {@link Server#GAMING} 状态时,
     * 此设置会极大阻碍服务端消息读取速度, 因此将其设置为 1
     */
    public static final int CLIENT_HANDLER_SOCKET_TIMEOUT = 1;
    /**
     * 客户端套接字超时时间 ms
     *
     * @apiNote 在客户端使用
     */
    public static final int CLIENT_SOCKET_TIMEOUT = 500;
    /**
     * Msg对象有效时间, 超过该时间将被丢弃
     */
    public static final long MSG_OUTDATED_TIME = 5000;
    /**
     * 服务端最大允许注册玩家数量
     */
    public static final int MAX_SERVER_SESSION_PLAYER_NUM = 5;
    /**
     * 服务器广播游戏同步信息的时间间隔 (与游戏进程的处理速度无关)
     */
    public static final long SERVER_GAME_STATE_SENDING_INTERVAL_TIME_MS = 100;

    /**
     * 是否播放音效
     */
    public static final AtomicBoolean doPlaySound = new AtomicBoolean(true);
    /**
     * 保存墙图像素图片的文件的文件后缀
     */
    public static final String WALL_MAP_FILE_SUFFIX = ".mwal";
    /**
     * 受伤音效
     */
    public static final String ATTACKED_SOUND = "sound/attacked.mp3";
    /**
     * 开火音效
     */
    public static final String FIRE_SOUND = "sound/fire.mp3";
    /**
     * 随机传送音效
     */
    public static final String RANDOMLY_TELEPORT_SOUND = "sound/teleport.mp3";
    /**
     * 临时文件夹, 应用运行时会放一些临时文件, 每次启动应用都会被清空
     */
    public static final String TEMP_DIR = "temp";
    /**
     * 游戏线程名字
     */
    public static final String GAME_THREAD_NAME = "GameProcess";

    private Config() {
    }
}
