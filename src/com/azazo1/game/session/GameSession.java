package com.azazo1.game.session;

import com.azazo1.Config;
import com.azazo1.base.SingleInstance;
import com.azazo1.game.GameMap;
import com.azazo1.game.bullet.BulletGroup;
import com.azazo1.game.item.ItemGroup;
import com.azazo1.game.tank.TankBase;
import com.azazo1.game.tank.TankGroup;
import com.azazo1.game.tank.robot.RobotTank;
import com.azazo1.game.wall.WallGroup;
import com.azazo1.util.Timer;
import com.azazo1.util.Tools;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 此类用于管理一局游戏
 */
public abstract class GameSession implements SingleInstance {
    protected volatile static GameSession instance = null;
    protected volatile GameMap gameMap; // 只供子类初始化时更改
    protected final AtomicInteger totalTankNum = new AtomicInteger(0); // 总共的坦克数量
    protected volatile Timer timer;
    /**
     * 游戏进程任务, 调用 {@link Timer.TimerTask#cancel()} 则游戏停止进行
     */
    protected volatile Timer.TimerTask gameProcessTask;
    protected volatile FrameListener listener;

    protected GameSession() {
        checkInstance();
        instance = this;
        TankBase.getSeqModule().init();
        Tools.clearFrameData(); // 清空帧数信息
        gameMap = new GameMap();
        gameMap.setSize(Config.MAP_WIDTH, Config.MAP_HEIGHT);
    }

    public static void clearInstance() {
        instance = null;
    }

    @Override
    public void checkInstance() {
        if (instance != null) {
            throw new IllegalStateException("GameSession can be created only once.");
        }
    }

    public void setFrameListener(FrameListener frameListener) {
        listener = frameListener;
    }

    public GameMap getGameMap() {
        return gameMap;
    }

    /**
     * 开启游戏事件调度
     */
    public void start() {
        timer = new Timer(Config.GAME_THREAD_NAME);
        gameProcessTask = new Timer.TimerTask() {
            @Override
            public void run() {
                gameMap.update(null);
                if (listener != null) {
                    listener.tick(gameMap.getTankGroup().getTanksInfo(), gameMap.getBulletGroup().getBulletNum());
                }
            }
        };
        timer.scheduleAtFixedRate(gameProcessTask, 0, (int) (1000.0 / Config.FPS));
    }

    /**
     * 停止游戏事件调度, 获得游戏信息
     */
    public GameMap.GameInfo stop() {
        gameProcessTask.cancel();
        GameMap.GameInfo rst = gameMap.getInfo();
        gameMap.dispose();
        return rst;
    }

    /**
     * 根据特定条件判断游戏进程是否结束(如:有坦克胜利则则为结束)<br>
     * 一般此方法由 {@link FrameListener} 调用检查游戏进程
     */
    public abstract boolean isOver();

    public int getTotalTankNum() {
        return totalTankNum.get();
    }

    /**
     * 游戏回调函数
     */
    public interface FrameListener {
        /**
         * 每次帧刷新({@link GameMap#update(Graphics)})都要被调用, 用于本地信息显示, 而不用于向客户端/服务器同步数据
         *
         * @param tanks     现存坦克信息
         * @param bulletNum 现在的子弹数量
         * @apiNote 记得调用 {@link Tools#tickFrame()} 来推动游戏进程, 不用手动控制游戏帧率
         */
        void tick(Vector<TankBase.TankInfo> tanks, int bulletNum);
    }

    public static class LocalSession extends GameSession {
        protected LocalSession() {
            super();
        }

        /**
         * 创建一局本地游戏会话
         *
         * @param tankNum   坦克(玩家)数量
         * @param robotNum  机器人(电脑)数量
         * @param tankNames 各个坦克名称, 但如果不为 null, 其长度要符合 tankNum
         * @param wallMap   游戏墙图文件输入流, 用于读取产生 {@link WallGroup}
         */
        public static @NotNull LocalSession createLocalSession(int tankNum, int robotNum, @Nullable String[] tankNames, @NotNull InputStream wallMap) throws IOException {
            if (tankNum + robotNum < 2) {
                throw new IllegalArgumentException("Total tank amount can't be less than 2");
            }
            GameSession.clearInstance();
            LocalSession session = new LocalSession();

            WallGroup wallG = WallGroup.parseFromBitmap(ImageIO.read(wallMap));
            session.gameMap.setWallGroup(wallG);

            session.gameMap.setBulletGroup(new BulletGroup());

            ItemGroup itemGroup = new ItemGroup();
            session.gameMap.setItemGroup(itemGroup);

            TankGroup tankG = new TankGroup();
            session.gameMap.setTankGroup(tankG);
            session.totalTankNum.set(tankNum + robotNum);
            for (int i = 0; i < tankNum; i++) {
                TankBase tank = new TankBase();
                if (tankNames != null) {
                    tank.setName(tankNames[i]);
                }
                tankG.addTank(tank);
                tank.randomlyTeleport(); // 要在其他内容都设置完毕后调用
            }
            for (int i = 0; i < robotNum; i++) {
                TankBase tank = new RobotTank();
                if (tankNames != null) {
                    tank.setName("Robot");
                }
                tankG.addTank(tank);
                tank.randomlyTeleport(); // 要在其他内容都设置完毕后调用
            }
            return session;
        }

        @Override
        public boolean isOver() {
            return gameMap.getTankGroup().getLivingTankNum() <= 1;
        }
    }
}
