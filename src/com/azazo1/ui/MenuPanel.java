package com.azazo1.ui;

import com.azazo1.Config;
import com.azazo1.base.PlayingMode;
import com.azazo1.game.session.GameSession;
import com.azazo1.game.wall.WallGroup;
import com.azazo1.util.JRadioButtonGroup;
import com.azazo1.util.MyURL;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.awt.GridBagConstraints.REMAINDER;

/**
 * 此类应该只作为 {@link MyFrame#setContentPane(Container)} 的参数
 */
public class MenuPanel extends MyPanel {
    private static MyPanel instance;
    private final AtomicBoolean attached = new AtomicBoolean(false); // 是否曾被设置为 MyFrame 的 contentPanel
    /**
     * 绑定到的 {@link MyFrame}
     */
    private MyFrame frame;
    private final Vector<Box> localPlayerNames = new Vector<>();
    /**
     * 显示各个墙图文件路径
     */
    private JComboBox<MyURL> wallMapFilesComboBox;
    private JRadioButtonGroup buttonGroup;
    private JComboBox<Integer> playerNumComboBox;
    private JComboBox<Integer> robotNumComboBox;
    private Box localPlayerNamesBox;
    private JTextField serverPortTextField;
    private JTextField serverIPTextField;
    private final ActionListener launchButtonListener = (e) -> {
        String command = buttonGroup.getSelectedActionCommand();
        if (command == null) {
            JOptionPane.showConfirmDialog(this, Config.translation.playingModeNotChosen,
                    Config.translation.errorTitle, JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
        } else if (command.equals(PlayingMode.LOCAL)) {
            // 启动本地游戏
            try {
                @SuppressWarnings("ConstantConditions")
                GameSession.LocalSession session = GameSession.LocalSession.createLocalSession(
                        (Integer) playerNumComboBox.getSelectedItem(),
                        (Integer) robotNumComboBox.getSelectedItem(),
                        getLocalPlayerNames(),
                        ((MyURL) wallMapFilesComboBox.getSelectedItem()).url().openStream());
                // 切换到GamePanel
                GamePanel gamePanel = new GamePanel(session);
                gamePanel.start();
                MenuPanel.this.setVisible(false);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showConfirmDialog(this,
                        String.format(Config.translation.errorTextFormat, ex.getStackTrace()[0], ex.getMessage()),
                        Config.translation.errorTitle, JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            }
        } else if (buttonGroup.getSelectedActionCommand().equals(PlayingMode.ONLINE)) {
            // 启动在线游戏
            try {
                new OnlineWaitingRoomPanel(frame, serverIPTextField.getText(), Integer.parseInt(serverPortTextField.getText()));
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showConfirmDialog(this,
                        String.format(Config.translation.errorTextFormat, ex.getStackTrace()[0], ex.getMessage()),
                        Config.translation.errorTitle, JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            }
        }
    };

    /**
     * 本构造方法会自动调用{@link MyFrame#setContentPane(Container)}
     * 无需再次调用
     */
    public MenuPanel(MyFrame frame) {
        super();
        instance = this;
        this.frame = frame;
        this.frame.setContentPane(this);
    }

    @Nullable
    public static MyPanel getInstance() {
        return instance;
    }

    private String @NotNull [] getLocalPlayerNames() {
        Vector<String> rst = new Vector<>();
        localPlayerNames.forEach((box -> {
            Document doc = ((JTextField) box.getComponents()[1]).getDocument();
            try {
                rst.add(doc.getText(0, doc.getLength()));
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
        }));
        return rst.toArray(new String[]{});
    }

    /**
     * 构建组件内容
     */
    @Override
    public void setupUI() {
        setVisible(true);
        if (attached.get()) { // 不构建第二次
            return;
        }
        Box horizontalBox = Box.createHorizontalBox();
        Box verticalBox = Box.createVerticalBox();
        JPanel localPlayingPanel = new JPanel();
        localPlayerNamesBox = Box.createVerticalBox();
        playerNumComboBox = new JComboBox<>(new Integer[]{0, 1, 2, 3, 4});
        robotNumComboBox = new JComboBox<>(new Integer[]{0, 1, 2, 3, 4});
        wallMapFilesComboBox = new JComboBox<>(WallGroup.scanBinaryBitmapFiles());
        JRadioButton localPlayingRadioButton = new JRadioButton(PlayingMode.LOCAL);
        JPanel onlinePlayingPanel = new JPanel();
        serverIPTextField = new JTextField();
        serverPortTextField = new JTextField();
        JRadioButton onlinePlayingRadioButton = new JRadioButton(PlayingMode.ONLINE);
        buttonGroup = new JRadioButtonGroup();
        JButton launchButton = new JButton(Config.translation.launchButtonText);

        localPlayingPanel.setBorder(new LineBorder(Config.BORDER_COLOR, 2, true));
        onlinePlayingPanel.setBorder(new LineBorder(Config.BORDER_COLOR, 2, true));
        serverIPTextField.setColumns(10);
        serverPortTextField.setColumns(4);
        localPlayingRadioButton.setSelected(true);
        buttonGroup.add(localPlayingRadioButton);
        buttonGroup.add(onlinePlayingRadioButton);
        launchButton.addActionListener(launchButtonListener);

        add(horizontalBox);
        horizontalBox.add(Box.createVerticalStrut(Config.WINDOW_HEIGHT - 10));
        horizontalBox.add(verticalBox);

        verticalBox.add(new JLabel(Config.translation.menuPanelTitle));

        verticalBox.add(localPlayingPanel);
        var layout = new BoxLayout(localPlayingPanel, BoxLayout.Y_AXIS);
        localPlayingPanel.setLayout(layout);
        localPlayingPanel.add(localPlayingRadioButton);
        localPlayingPanel.add(MyPanel.createHBoxContainingPairComponent(
                new JLabel(Config.translation.playerNumLabelText),
                playerNumComboBox
        ));
        localPlayingPanel.add(MyPanel.createHBoxContainingPairComponent(
                new JLabel(Config.translation.robotAmountLabelText),
                robotNumComboBox
        ));
        localPlayingPanel.add(MyPanel.createHBoxContainingPairComponent( // 选择地图
                new JLabel(Config.translation.wallMapFilesComboLabelText),
                wallMapFilesComboBox
        ));
        localPlayingPanel.add(localPlayerNamesBox); // 玩家昵称填写区
        JLabel label = new JLabel(Config.translation.localPlayerNamesBoxHint);
        // 点击刷新功能
        label.setFocusable(true);
        label.setFont(Config.TEXT_FONT);
        label.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                adjustPlayerTextFieldNumber();
                label.setFont(Config.TEXT_FONT_FOCUSED);
            }

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                adjustPlayerTextFieldNumber();
                label.setFont(Config.TEXT_FONT);
            }
        });
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                adjustPlayerTextFieldNumber();
                label.requestFocus();
            }
        });
        localPlayerNamesBox.add(label);
        // 填充玩家昵称输入框列表
        adjustPlayerTextFieldNumber();


        verticalBox.add(onlinePlayingPanel);
        onlinePlayingPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = REMAINDER;
        onlinePlayingPanel.add(onlinePlayingRadioButton, constraints);
        constraints.gridwidth = 1;
        constraints.weightx = 1;
        onlinePlayingPanel.add(new JLabel(Config.translation.typeIPAddressLabelText) {{
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    serverIPTextField.setText("127.0.0.1");
                    serverPortTextField.setText("60000");
                }
            });
        }}, constraints);
        constraints.weightx = 4;
        onlinePlayingPanel.add(serverIPTextField, constraints);
        constraints.weightx = 1;
        onlinePlayingPanel.add(new JLabel(Config.translation.ipAddressSeparator), constraints);
        constraints.gridwidth = REMAINDER;
        onlinePlayingPanel.add(serverPortTextField, constraints);
        constraints.weightx = 1;
        constraints.gridwidth = REMAINDER;

        verticalBox.add(launchButton);
        setSize(Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);
        attached.set(true);
    }

    private void adjustPlayerTextFieldNumber() {
        Integer selectedItem = (Integer) playerNumComboBox.getSelectedItem();
        if (selectedItem == null) {
            localPlayerNames.forEach((e) -> e.getParent().remove(e)); // 清空
            return;
        }
        while (localPlayerNames.size() < selectedItem) {
            Box box = MyLabel.createHBoxWithMyLabelAndJTextField(localPlayerNames.size() + " ", 10);
            localPlayerNames.add(box);
            localPlayerNamesBox.add(box);
        }
        while (localPlayerNames.size() > selectedItem) {
            Box removed = localPlayerNames.remove(localPlayerNames.size() - 1);
            removed.getParent().remove(removed);
        }
    }
}
