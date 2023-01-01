package com.azazo1.ui;

import com.azazo1.Config;
import com.azazo1.base.TankAction;
import com.azazo1.game.session.ServerGameSessionIntro;
import com.azazo1.game.tank.TankBase;
import com.azazo1.game.wall.WallGroup;
import com.azazo1.online.client.Client;
import com.azazo1.online.client.ClientGameMap;
import com.azazo1.online.client.bullet.ClientBulletGroup;
import com.azazo1.online.client.tank.ClientTank;
import com.azazo1.online.client.tank.ClientTankGroup;
import com.azazo1.online.client.wall.ClientWallGroup;
import com.azazo1.online.msg.*;
import com.azazo1.online.server.toclient.ClientHandler;
import com.azazo1.util.MyFrameSetting;
import com.azazo1.util.MyURL;
import com.azazo1.util.Tools;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azazo1.online.msg.PostGameIntroMsg.*;
import static com.azazo1.online.msg.RegisterMsg.RegisterResponseMsg.*;
import static com.azazo1.online.msg.ReqGameStartMsg.*;
import static com.azazo1.online.server.toclient.Server.GAMING;
import static com.azazo1.online.server.toclient.Server.WAITING;
import static com.azazo1.util.Tools.resizeFrame;

/**
 * 用户选择了服务器地址和端口但仍未开始连接, 本Panel实现连接服务器, 等待大厅（玩家详情）, 房主选择墙图, 房主开始游戏的功能
 */
public class OnlineWaitingRoomPanel {
    public JPanel panel; // 把此panel作为一个Frame的contentPanel即可显示内容
    protected final AtomicBoolean alive = new AtomicBoolean(true);
    public final String addr;
    public final int port;
    public final Client client;
    private JLabel connectionInfo;
    private JList<String> clientList;
    private JLabel yourProfile;
    private JLabel cilentListTitle;
    private JLabel playerListTitle;
    private JList<String> playerList;
    private JComboBox<String> wallMapSelector;
    private JLabel gameIntroEditTitle;
    private JLabel modeSelectorTitle;
    private JComboBox modeSelector;
    private JButton gameIntroEditButton;
    private JButton startGameButton;
    private JTextField nicknameInput;
    private JButton registerButton;
    private JLabel inputNameTitle;
    private JButton resultButton;
    public ClientGameMap gameMap;

    /**
     * 用于储存查询到的intro信息
     */
    private final ServerGameSessionIntro gameIntro = new ServerGameSessionIntro();
    /**
     * 用于储存查询到的客户端信息
     */
    private Vector<String> clients;
    /**
     * 用于储存查询到的玩家信息
     */
    private Vector<String> players;

    /**
     * 当前状态(根据服务器消息修改)
     */
    public String currentState = WAITING;
    /**
     * 所属 MyFrame
     */
    public MyFrame frame;
    /**
     * @see MyFrameSetting
     */
    private final MyFrameSetting originalFrameSetting;
    /**
     * @see ClientGamePanel
     */
    private ClientGamePanel gamePanel;

    /**
     * @param frame 本 panel 要被放置到的 JFrame 中,
     *              实例化 {@link OnlineWaitingRoomPanel} 后不用再额外调用 {@link MyFrame#setContentPane(Container)},
     *              本构造方法会自行调用
     *              当本 Panel 生命结束时将会自动返回原来的 ContentPane
     */
    public OnlineWaitingRoomPanel(MyFrame frame, String addr, int port) throws IOException {
        super();
        Tools.clearFrameData();
        this.addr = addr;
        this.port = port;
        client = new Client(addr, port);
        while (this.panel == null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        this.frame = frame;
        this.originalFrameSetting = new MyFrameSetting(frame);
        frame.setContentPane(this.panel);
        frame.setResizable(false);
        frame.setTitle(Config.translation.connectedFrameTitle);
        resizeFrame(frame, 850, 500);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        // 点击窗口关闭按钮, 调用 dispose 方法
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                frame.removeWindowListener(this);
                dispose();
            }
        });
    }

    private void createUIComponents() {
        panel = new MyPanel() {
            @Override
            public void setupUI(MyFrame frame) {
            }
        };

        connectionInfo = new JLabel();

        yourProfile = new JLabel();

        cilentListTitle = new JLabel();
        playerListTitle = new JLabel();
        gameIntroEditTitle = new JLabel();

        // Vector初始化放在这确保这俩不是null
        clients = new Vector<>();
        players = new Vector<>();
        clientList = new JList<>(clients);
        playerList = new JList<>(players);

        modeSelector = new JComboBox<String>(new Vector<>() {{
            add(Config.translation.playerMode);
            add(Config.translation.spectatorMode);
        }});

        Vector<String> maps = new Vector<>();
        for (MyURL url : Objects.requireNonNull(WallGroup.scanBinaryBitmapFiles())) {
            maps.add(url.toString());
        }
        wallMapSelector = new JComboBox<>(maps);
        gameIntroEditButton = new JButton() {{
            addActionListener(e -> {
                // 打开编辑游戏配置的窗口
                JDialog selectDialog = new JDialog();
                selectDialog.setModal(true);
                selectDialog.setLayout(new FlowLayout());
                JComboBox<String> selector = new JComboBox<>(maps);
                JTextField textField = new JTextField("%dx%d".formatted(gameIntro.getMapSize().width, gameIntro.getMapSize().height));
                selectDialog.setTitle(Config.translation.gameIntroEditTitle);
                selectDialog.add(new JLabel(Config.translation.mapSizeEditTitle));
                selectDialog.add(textField);
                selectDialog.add(selector);
                selectDialog.add(new JButton(Config.translation.confirmButtonText) {{
                    addActionListener(e1 -> {
                        // 解析输入
                        String input = textField.getText();
                        String[] inputSplit = input.split("x");
                        int width, height;
                        if (!(inputSplit.length == 2)) { // 检查宽高格式是否正确
                            JOptionPane.showMessageDialog(panel, Config.translation.invalidMapSizeInput);
                            return;
                        } else {
                            try {
                                width = Integer.parseInt(inputSplit[0]);
                                height = Integer.parseInt(inputSplit[1]);
                                if (!(0 < width && 0 < height)) { // 检查宽高范围是否正确
                                    throw new NumberFormatException();
                                }
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(panel, Config.translation.invalidMapSizeInput);
                                return;
                            }
                        }
                        try {
                            client.editGameIntro((String) selector.getSelectedItem(), new Rectangle(width, height));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(panel,
                                    String.format(Config.translation.errorTextFormat, ex.getStackTrace()[0], ex.getMessage()),
                                    Config.translation.errorTitle, JOptionPane.ERROR_MESSAGE);
                        }
                        selectDialog.dispose();
                    });
                }});
                selectDialog.setResizable(false);
                selectDialog.setSize(325, 100);
                selectDialog.setVisible(true);
            });
        }};

        startGameButton = new JButton() {{
            addActionListener(e -> {
                client.reqStartGame();
            });
        }};

        registerButton = new JButton() {{
            addActionListener(e -> {
                String mode = (String) modeSelector.getSelectedItem();
                String name = nicknameInput.getText();
                boolean mode_;
                if (mode != null && name != null && !name.isBlank()) {
                    if (mode.equals(Config.translation.playerMode)) {
                        mode_ = true;
                    } else if (mode.equals(Config.translation.spectatorMode)) {
                        mode_ = false;
                    } else {
                        JOptionPane.showConfirmDialog(panel, Config.translation.errorTitle, Config.translation.errorTitle, JOptionPane.DEFAULT_OPTION);
                        return;
                    }
                } else {
                    JOptionPane.showConfirmDialog(panel, Config.translation.errorTitle, Config.translation.errorTitle, JOptionPane.DEFAULT_OPTION);
                    return;
                }
                client.register(mode_, name.strip());
            });
        }};
        resultButton = new JButton() {{
            addActionListener(e -> {
                client.queryGameResult();
            });
        }};

        nicknameInput = new JTextField();
        inputNameTitle = new JLabel(Config.translation.nicknameInputTitle);
        modeSelectorTitle = new JLabel(Config.translation.gameModeSelectorTitle);

        flushComponentContent();

        startMsgHandler();

        startAskor();
    }

    private void startAskor() {
        // 定期请求个人信息\客户端信息\玩家信息\墙图信息
        new Thread(() -> {
            try {
                //noinspection LoopConditionNotUpdatedInsideLoop
                while (client == null) {
                    Thread.sleep(50);
                }
                while (client.getAlive() && currentState.equals(WAITING)) {
                    client.fetchGameIntro();
                    client.queryClients();
                    Thread.sleep(3000);
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                JOptionPane.showConfirmDialog(panel,
                        String.format(Config.translation.errorTextFormat, ex.getStackTrace()[0], ex.getMessage()),
                        Config.translation.errorTitle, JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                dispose(); // 显示错误后关闭界面
            }
        }).start();
    }

    private void startMsgHandler() {
        // 信息收取, 用Timer会卡顿
        new Thread(() -> {
            try {
                //noinspection LoopConditionNotUpdatedInsideLoop
                while (client == null) {
                    Thread.sleep(50);
                }
                while (client.getAlive()) {
                    MsgBase obj = client.handle(false);
                    if (obj == null) {
                        continue;
                    }
                    if (obj instanceof RegisterMsg.RegisterResponseMsg msg) {
                        handleRegisterResponseMsg(msg);
                    } else if (obj instanceof PostGameIntroResponseMsg msg) {
                        handlePostGameIntroResponseMsg(msg);
                    } else if (obj instanceof FetchGameIntroMsg.FetchGameIntroResponseMsg msg) {
                        handleFetchGameIntroResponseMsg(msg);
                    } else if (obj instanceof QueryClientsMsg.QueryClientsResponseMsg msg) {
                        handleQueryClientsResponseMsg(msg);
                    } else if (obj instanceof GameStateMsg msg) {
                        handleGameStateMsg(msg);
                    } else if (obj instanceof GameOverMsg msg) {
                        handleGameOverMsg(msg);
                    } else if (obj instanceof QueryGameResultMsg.QueryGameResultResponseMsg msg) {
                        handleQueryGameResultResponseMsg(msg);
                    } else if (obj instanceof ReqGameStartResponseMsg msg) {
                        handleReqGameStartResponseMsg(msg);
                    } else if (obj instanceof GameStartMsg msg) {
                        handleGameStartMsg(msg);
                    } else if (obj instanceof KickMsg) {
                        handleKickMsg();
                    }
                    Thread.sleep(1);// 需要比服务端处理速度快
                }
                // client 已经关闭
                if (alive.get()) { // 如果是意外性的连接断开
                    throw new IllegalStateException("Client has closed.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel,
                        String.format(Config.translation.errorTextFormat, ex.getStackTrace()[0], ex.getMessage()),
                        Config.translation.errorTitle, JOptionPane.ERROR_MESSAGE);
                dispose(); // 显示错误后关闭界面
            }
        }).start();
    }

    private void handleKickMsg() {
        JOptionPane.showMessageDialog(panel, Config.translation.kicked);
        dispose();
    }

    private void handleGameStartMsg(@NotNull GameStartMsg msg) {
        Tools.logLn("Game start.");
        gameIntro.copyFrom(msg.intro);
        Tools.logLn("" + msg.intro);
        currentState = GAMING;
        startGamePanel();
    }

    private void handleQueryGameResultResponseMsg(@NotNull QueryGameResultMsg.QueryGameResultResponseMsg msg) {
        // 显示游戏结果
        new ResultPanel(frame, msg.info, true, false);
    }

    private void handleGameOverMsg(GameOverMsg msg) {
        // 游戏结束 显示结束画面
        Tools.logLn("Game over.");
        Tools.logLn("" + msg);
        gamePanel.gameOver();
        EventQueue.invokeLater(() -> {
            new ResultPanel(frame, msg.info, true, false);
        });
    }

    private void handleRegisterResponseMsg(RegisterMsg.@NotNull RegisterResponseMsg msg) {
        switch (msg.code) {
            case SUCCEED -> {
                /*提醒用户注册成功*/
                JOptionPane.showMessageDialog(panel, Config.translation.registerSucceeded, Config.translation.succeedTitle, JOptionPane.INFORMATION_MESSAGE);
                // 改变标题
                frame.setTitle(Config.translation.registeredFrameTitleFormat.formatted(msg.origin.name));
            }
            case PLAYER_MAXIMUM -> /*提醒用户更换游戏模式*/
                    JOptionPane.showMessageDialog(panel, Config.translation.playerMaximum, Config.translation.errorTitle, JOptionPane.ERROR_MESSAGE);
            case NAME_OR_SEQ_OCCUPIED -> /*提醒用户改名*/
                    JOptionPane.showMessageDialog(panel, Config.translation.nameCollision, Config.translation.errorTitle, JOptionPane.ERROR_MESSAGE);
            case HAVING_REGISTERED -> /*提醒用户已经注册了*/
                    JOptionPane.showMessageDialog(panel, Config.translation.havingRegistered, Config.translation.errorTitle, JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handlePostGameIntroResponseMsg(@NotNull PostGameIntroResponseMsg msg) {
        switch (msg.rst) {
            case POST_GAME_INTRO_NOT_HOST -> JOptionPane.showMessageDialog(panel, Config.translation.notHost);
            case POST_GAME_INTRO_INCORRECT_STATE ->
                    JOptionPane.showMessageDialog(panel, Config.translation.serverNotInWaitingState);
            case POST_GAME_INTRO_SUCCESSFULLY ->
                    JOptionPane.showMessageDialog(panel, Config.translation.postGameIntroSucceeded);
        }
    }

    private void handleFetchGameIntroResponseMsg(FetchGameIntroMsg.@NotNull FetchGameIntroResponseMsg msg) {
        // 处理所有玩家信息, 改变墙图设置, 改变画布大小
        gameIntro.copyFrom(msg.intro);
        gameIntroEditTitle.setText(Config.translation.gameIntroEditTitleFormat.formatted(
                msg.intro.getMapSize().width, msg.intro.getMapSize().height
        ));
        wallMapSelector.setSelectedItem(gameIntro.getWallMapFile());
        HashMap<Integer, String> tanks = gameIntro.getTanks();
        players.clear();
        tanks.forEach((seq, name) -> {
            players.add("%d: %s".formatted(seq, name));
        });
        playerList.setListData(players);
    }

    private void handleQueryClientsResponseMsg(QueryClientsMsg.@NotNull QueryClientsResponseMsg msg) {
        //  处理所有客户端信息, todo 注意重点标记显示自身
        HashMap<Integer, ClientHandler.ClientHandlerInfo> infos = msg.multiInfo;
        clients.clear();
        infos.forEach((seq, info) -> {
            clients.add(Config.translation.clientInfoFormat.formatted(info.seq, info.address, info.port, info.isHost ? "True" : "False"));
        });
        ClientHandler.ClientHandlerInfo info = infos.get(client.getSeq());
        String isPlayer = Config.translation.notDecided;
        if (info.isPlayer != null) {
            isPlayer = info.isPlayer.get() ? Config.translation.playerMode : Config.translation.spectatorMode;
        }
        yourProfile.setText(Config.translation.yourProfileFormat.formatted(info.name, info.seq, info.isHost ? "True" : "False", isPlayer));
        clientList.setListData(clients);
    }

    private void handleReqGameStartResponseMsg(@NotNull ReqGameStartResponseMsg msg) {
        switch (msg.rst) {
// <此项会导致卡在 Dialog 而没能使房主及时进入游戏> case START_GAME_SUCCESSFULLY -> JOptionPane.showMessageDialog(panel, Config.translation.startGameSucceeded);
            case START_GAME_NOT_ENOUGH_PLAYER ->
                    JOptionPane.showMessageDialog(panel, Config.translation.notEnoughPlayer);
            case START_GAME_NO_WALL_MAP_FILE ->
                    JOptionPane.showMessageDialog(panel, Config.translation.serverHasNoWallMapFile);
            case START_GAME_INCORRECT_STATE ->
                    JOptionPane.showMessageDialog(panel, Config.translation.serverNotInWaitingState);
            case START_GAME_NOT_HOST -> JOptionPane.showMessageDialog(panel, Config.translation.notHost);
        }
    }

    /**
     * 服务端在游戏阶段向客户端同步信息
     * 服务端每次同步就会调用 {@link Tools#tickFrame()}
     * 因此客户端每次收到此消息就调用 {@link Tools#tickFrame(boolean)} 以同步帧时间
     */
    private void handleGameStateMsg(@NotNull GameStateMsg msg) {
        if (gameMap == null) {
            return; // 若 gameMap 为 null 则暂时不处理
        }
        Tools.tickFrame(false);
        ClientTankGroup tankGroup = (ClientTankGroup) gameMap.getTankGroup();
        ClientBulletGroup bulletGroup = (ClientBulletGroup) gameMap.getBulletGroup();
        tankGroup.syncTanks(msg.state.tankInfos(), client);
        bulletGroup.syncBullets(msg.state.bulletInfos());
        gamePanel.updateLabels();
    }

    /**
     * 刷新组件内容
     */
    private void flushComponentContent() {
        new Timer(10, e -> {
            // 刷新文本内容
            yourProfile.setText(Config.translation.yourProfileFormat.formatted("None", -1, "None", "None"));
            cilentListTitle.setText(Config.translation.onlineClientListTitle);
            playerListTitle.setText(Config.translation.onlinePlayerListTitle);
            gameIntroEditTitle.setText(Config.translation.gameIntroEditTitleFormat.formatted(gameIntro.getMapSize().width, gameIntro.getMapSize().height));
            inputNameTitle.setText(Config.translation.nicknameInputTitle);
            modeSelectorTitle.setText(Config.translation.gameModeSelectorTitle);
            registerButton.setText(Config.translation.registerButtonText);
            gameIntroEditButton.setText(Config.translation.gameIntroEditButtonText);
            startGameButton.setText(Config.translation.startGameButtonText);
            connectionInfo.setText(Config.translation.connectionInfoFormat.formatted(addr, port));
            resultButton.setText(Config.translation.queryResultButtonText);
        }) {{
            setRepeats(false);
        }}.start();
    }

    public void startGamePanel() {
        // 创建 ClientGameMap
        TankBase.getSeqModule().init();
        gameMap = new ClientGameMap();
        gameMap.setSize(gameIntro.getMapSize().width, gameIntro.getMapSize().height);
        gameMap.setBulletGroup(new ClientBulletGroup());
        ClientTankGroup tankGroup = new ClientTankGroup();
        gameMap.setTankGroup(tankGroup);
        gameIntro.getTanks().forEach((seq, name) -> {
            ClientTank tank = new ClientTank(seq, seq == client.getSeq());
            tank.setName(name);
            tankGroup.addTank(tank);
        });
        BufferedImage bImage;
        try {
            bImage = ImageIO.read(Tools.getFileURL(gameIntro.getWallMapFile()).url());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        gameMap.setWallGroup(ClientWallGroup.parseFromBitmap(bImage, gameIntro.getMapSize().width, gameIntro.getMapSize().height));
        initGameMapKeyListener();
        // 构建 ClientGamePanel (ContentPane 自动切换)
        gamePanel = new ClientGamePanel(frame, gameMap, gameIntro);
    }

    /**
     * 绑定按键控制坦克行为
     */
    private void initGameMapKeyListener() {
        HashMap<Integer, TankAction> keyActionMap = Config.TANK_ACTION_KEY_MAPS.get(0);
        gameMap.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                TankAction action = keyActionMap.get(e.getKeyCode());
                if (action == TankAction.FIRE) {
                    client.fire();
                } else {
                    switch (action) {
                        case LEFT_TURNING -> gameMap.left.set(true);
                        case RIGHT_TURNING -> gameMap.right.set(true);
                        case FORWARD_GOING -> gameMap.forward.set(true);
                        case BACKWARD_GOING -> gameMap.backward.set(true);
                    }
                    client.changePressedKey(
                            gameMap.left.get(),
                            gameMap.right.get(),
                            gameMap.forward.get(),
                            gameMap.backward.get()
                    );
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                TankAction action = keyActionMap.get(e.getKeyCode());
                switch (action) {
                    case LEFT_TURNING -> gameMap.left.set(false);
                    case RIGHT_TURNING -> gameMap.right.set(false);
                    case FORWARD_GOING -> gameMap.forward.set(false);
                    case BACKWARD_GOING -> gameMap.backward.set(false);
                }
                client.changePressedKey(
                        gameMap.left.get(),
                        gameMap.right.get(),
                        gameMap.forward.get(),
                        gameMap.backward.get()
                );

            }
        });
    }


    /**
     * 返回上一个 ContentPane
     */
    public void dispose() {
        alive.set(false);
        client.close();
        originalFrameSetting.restore();
    }
}
