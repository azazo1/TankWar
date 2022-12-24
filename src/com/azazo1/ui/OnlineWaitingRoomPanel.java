package com.azazo1.ui;

import com.azazo1.Config;
import com.azazo1.game.session.ServerGameSessionIntro;
import com.azazo1.game.wall.WallGroup;
import com.azazo1.online.client.Client;
import com.azazo1.online.msg.*;
import com.azazo1.online.server.GameState;
import com.azazo1.online.server.toclient.ClientHandler;
import com.azazo1.util.MyURL;
import com.azazo1.util.Tools;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.Vector;

import static com.azazo1.online.msg.RegisterMsg.RegisterResponseMsg.*;
import static com.azazo1.online.server.toclient.Server.WAITING;

/**
 * 用户选择了服务器地址和端口但仍未开始连接, 本Panel实现连接服务器, 等待大厅（玩家详情）, 房主选择墙图, 房主开始游戏的功能
 * 本Panel需独立启动一个窗口, 不与MyFrame兼容
 */
public class OnlineWaitingRoomPanel {
    public JPanel panel; // 把此panel作为一个Frame的contentPanel即可显示内容
    public final String addr;
    public final int port;
    public final Client client;
    private JLabel connectionInfo;
    private JList clientList;
    private JLabel yourProfile;
    private JLabel cilentListTitle;
    private JLabel playerListTitle;
    private JList playerList;
    private JComboBox wallMapSelector;
    private JLabel mapSelectorTitle;
    private JLabel modeSelectorTitle;
    private JComboBox modeSelector;
    private JButton wallMapSelectButton;
    private JButton startGameButton;
    private JTextField nicknameInput;
    private JButton registerButton;
    private JLabel inputNameTitle;

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

    public OnlineWaitingRoomPanel(String addr, int port) throws IOException {
        super();
        this.addr = addr;
        this.port = port;
        client = new Client(addr, port);
    }

    private void createUIComponents() {
        panel = new JPanel();

        connectionInfo = new JLabel();
        connectionInfo.setText("Connected to: [" + addr + ":" + port + "]");

        yourProfile = new JLabel();

        cilentListTitle = new JLabel();
        playerListTitle = new JLabel();
        mapSelectorTitle = new JLabel();

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

        wallMapSelectButton = new JButton() {{
            addActionListener(e -> {
                client.selectWallMap((String) wallMapSelector.getSelectedItem());
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

        nicknameInput = new JTextField();
        inputNameTitle = new JLabel(Config.translation.nicknameInputTitle);
        modeSelectorTitle = new JLabel(Config.translation.gameModeSelectorTitle);
        new Timer(10, e -> {
            // 刷新文本内容
            yourProfile.setText(Config.translation.yourProfileFormat.formatted("None", -1, "None", "None"));
            cilentListTitle.setText(Config.translation.onlineClientListTitle);
            playerListTitle.setText(Config.translation.onlinePlayerListTitle);
            mapSelectorTitle.setText(Config.translation.wallMapSelectorTitle);
            inputNameTitle.setText(Config.translation.nicknameInputTitle);
            modeSelectorTitle.setText(Config.translation.gameModeSelectorTitle);
            registerButton.setText(Config.translation.registerButtonText);
            wallMapSelectButton.setText(Config.translation.wallMapSelectButtonText);
            startGameButton.setText(Config.translation.startGameButtonText);
            connectionInfo.setText(Config.translation.connectionInfoFormat.formatted(addr, port));
        }) {{
            setRepeats(false);
        }}.start();
        // 信息收取
        new Timer(1000, e -> {
            if (client == null) {
                return;
            }
            MsgBase obj = client.handle(false);
            if (obj instanceof RegisterMsg.RegisterResponseMsg msg) {
                Tools.logLn("" + msg.code);
                switch (msg.code) {
                    case SUCCEED -> /* 提醒用户*/
                            JOptionPane.showConfirmDialog(panel, Config.translation.registerSucceeded, Config.translation.succeedTitle, JOptionPane.DEFAULT_OPTION);
                    case PLAYER_MAXIMUM -> /* 提醒用户更换游戏模式*/
                            JOptionPane.showConfirmDialog(panel, Config.translation.playerMaximum, Config.translation.errorTitle, JOptionPane.DEFAULT_OPTION);
                    case NAME_OR_SEQ_OCCUPIED -> /* 提醒用户改名*/
                            JOptionPane.showConfirmDialog(panel, Config.translation.nameCollision, Config.translation.errorTitle, JOptionPane.DEFAULT_OPTION);
                }
            } else if (obj instanceof FetchGameIntroMsg.FetchGameIntroResponseMsg msg) {
                // 处理所有玩家信息, 改变墙图设置
                gameIntro.copyFrom(msg.intro);
                wallMapSelector.setSelectedItem(gameIntro.getWallMapFile());
                HashMap<Integer, String> tanks = gameIntro.getTanks();
                players.clear();
                tanks.forEach((seq, name) -> {
                    players.add("%d: %s".formatted(seq, name));
                });
                playerList.setListData(players);
            } else if (obj instanceof QueryClientsMsg.QueryClientsResponseMsg msg) {
                //  处理所有客户端信息, 注意重点标记显示自身
                HashMap<Integer, ClientHandler.ClientHandlerInfo> infos = msg.multiInfo;
                clients.clear();
                infos.forEach((seq, info) -> {
                    clients.add("Seq: %d, IP: [%s:%d], Host: %s".formatted(info.seq, info.address, info.port, info.isHost ? "True" : "False"));
                });
                ClientHandler.ClientHandlerInfo info = infos.get(client.getSeq());
                String isPlayer = Config.translation.notDecided;
                if (info.isPlayer != null) {
                    isPlayer = info.isPlayer.get() ? Config.translation.playerMode : Config.translation.spectatorMode;
                }
                yourProfile.setText(Config.translation.yourProfileFormat.formatted(info.name, info.seq, info.isHost ? "True" : "False", isPlayer));
                clientList.setListData(clients);
            } else if (obj instanceof GameStartMsg) {
                // todo 开始游戏的画面
            } else if (obj instanceof GameStateMsg) {
                // todo 显示游戏画面 (在gamepanel 中)
            } else if (obj instanceof GameOverMsg msg) {
                // todo 游戏结束 显示结束画面
                Tools.logLn("Game over.");
                Tools.logLn("" + msg);
            }
        }) {{
            setRepeats(true);
        }}.start();
        // 定期请求个人信息\客户端信息\玩家信息\墙图信息
        new Timer(3000, e -> {
            if (client != null) {
                client.fetchGameIntro();
                client.queryClients();
            }

        }) {{
            setRepeats(true);
        }}.start();
    }
}
