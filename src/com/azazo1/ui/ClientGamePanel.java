package com.azazo1.ui;

import com.azazo1.Config;
import com.azazo1.game.bullet.BulletGroup;
import com.azazo1.game.session.ServerGameSessionIntro;
import com.azazo1.game.tank.TankBase;
import com.azazo1.game.tank.TankGroup;
import com.azazo1.online.client.ClientGameMap;
import com.azazo1.online.client.bullet.ClientBulletGroup;
import com.azazo1.online.client.tank.ClientTankGroup;
import com.azazo1.online.msg.MsgBase;
import com.azazo1.util.MyFrameSetting;
import com.azazo1.util.Timer;
import com.azazo1.util.Tools;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 在线模式客户端游戏界面, 显示游戏画面及信息, 在游戏结束时显示 {@link ResultPanel}, 最后返回绑定的 {@link JFrame} 的原 ContentPane
 * 本类不会接管 Socket 连接, 由 {@link OnlineWaitingRoomPanel} 控制本类的操作
 */
public class ClientGamePanel {
    private JPanel gameMapContainer;
    private JLabel msgSyncRateLabel;
    private JLabel tankLabel;
    private JLabel bulletLabel;
    private JLabel mapSizeLabel;
    private JPanel panel;
    private JPanel gameStateSideBar;
    private JPanel tankInfoSidebar;
    /**
     * JFrame 原本的 ContentPane, 用于在游戏结束时返回该 Pane
     */
    private final MyFrameSetting originalFrameSetting;
    public final MyFrame frame;
    public final ClientGameMap gameMap;
    public final ServerGameSessionIntro intro;

    public final HashMap<Integer, MyLabel> tankLabelList = new HashMap<>(); // 顺序: 排名 由高到低
    private final GridBagConstraints constraints = new GridBagConstraints() {{
        gridwidth = REMAINDER;
    }}; // 用于竖直放置 TankInfo 的 label
    protected final AtomicBoolean alive = new AtomicBoolean(true);

    /**
     * @param frame 本 panel 要被放置到的 MyFrame 中,
     *              实例化 {@link ClientGamePanel} 后不用再调用 {@link MyFrame#setContentPane(Container)},
     *              本构造方法会自行调用
     */
    public ClientGamePanel(@NotNull MyFrame frame, @NotNull ClientGameMap gameMap, @NotNull ServerGameSessionIntro intro) {
        this.frame = frame;
        originalFrameSetting = new MyFrameSetting(frame);
        this.gameMap = gameMap;
        this.intro = intro;
        while (this.panel == null) { // 等待 panel 初始化
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        frame.setContentPane(this.panel);
        frame.setSize(850, 600);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                dispose();
                frame.removeWindowListener(this);
            }
        });
        // Tank 的 seq 管理模组在 Client 模式被禁用
        Tools.clearFrameData(); // 清空帧数信息
    }

    private void createUIComponents() {
        panel = new MyPanel() {
            @Override
            public void setupUI() {
            }
        };
        gameMapContainer = new JPanel();

        EventQueue.invokeLater(() -> { // 若再次等待会使整个线程堵塞
            //noinspection LoopConditionNotUpdatedInsideLoop
            while (this.gameMap == null) { // 等待 gameMap 被赋值
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            gameMapContainer.add(gameMap);
            Timer updater = new Timer(Config.GAME_THREAD_NAME);
            updater.scheduleAtFixedRate(new Timer.TimerTask() {
                @Override
                public void run() {
                    if (alive.get()) {
                        gameMap.update(null);
                    } else {
                        this.cancel();
                        updater.cancel();
                    }
                }
            }, 0, (int) (1000.0 / Config.FPS));
        });

        tankInfoSidebar = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        tankInfoSidebar.setLayout(layout);


        msgSyncRateLabel = new MyLabel();
        tankLabel = new MyLabel();
        bulletLabel = new MyLabel();
        mapSizeLabel = new MyLabel();
    }

    /**
     * @see MyLabel#setText(int, int, TankGroup)
     * @see MyLabel#setText(int, BulletGroup)
     * @see MyLabel#setText(int, int)
     * @see MyLabel#setText(int, int, com.azazo1.online.msg.MsgBase)
     * @see MyLabel#setText(TankBase.TankInfo)
     */
    public void updateLabels() {
        ClientTankGroup tankGroup = (ClientTankGroup) gameMap.getTankGroup();
        ClientBulletGroup bulletGroup = (ClientBulletGroup) gameMap.getBulletGroup();
        ((MyLabel) tankLabel).setText(tankGroup.getLivingTankNum(), tankGroup.getTankNum(), (TankGroup) null);
        ((MyLabel) bulletLabel).setText(bulletGroup.getBulletNum(), null);
        ((MyLabel) mapSizeLabel).setText(gameMap.getWidth(), gameMap.getHeight());
        ((MyLabel) msgSyncRateLabel).setText(Tools.getFPS(), Tools.getAverageFPS(), (MsgBase) null);
        // 输出所有坦克基本信息, 按排名顺序
        Vector<TankBase.TankInfo> tanksInfo = tankGroup.getTanksInfo();
        boolean didListChanged = false;
        for (int i = 0, tanksSize = tanksInfo.size(); i < tanksSize; i++) {
            TankBase.TankInfo info = tanksInfo.get(i);
            MyLabel label = null;
            try {
                label = tankLabelList.get(i);
            } catch (ArrayIndexOutOfBoundsException ignore) { // 是空位
            }
            if (label == null) { // 补充空位
                label = new MyLabel();
                tankLabelList.put(i, label);
                didListChanged = true;
            }
            label.setText(info);
        }
        if (didListChanged) {
            // 重新排 Label 的顺序
            tankInfoSidebar.removeAll();
            int max = Collections.max(tankLabelList.keySet()); // 获取最大的 index
            Vector<MyLabel> labels = new Vector<>();
            for (int i = 0; i < max + 1; i++) { // 创建桶位
                labels.add(null);
            }
            tankLabelList.forEach(labels::set); // 按 index 排序(桶排)
            tankInfoSidebar.removeAll();
            labels.forEach(label -> { // 按顺序放置到 sidebar
                if (label != null) {
                    tankInfoSidebar.add(label, constraints);
                }
            });
        }
    }

    /**
     * 游戏结束时, 由 {@link OnlineWaitingRoomPanel} 调用
     */
    public void gameOver() {
        dispose();
    }

    public void dispose() {
        originalFrameSetting.restore();
        alive.set(false);
    }

}
