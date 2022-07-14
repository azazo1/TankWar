package com.azazo1.ui;

import com.azazo1.Config;
import com.azazo1.base.PlayingMode;
import com.azazo1.game.GameSession;
import com.azazo1.game.wall.WallGroup;
import com.azazo1.util.JRadioButtonGroup;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.awt.GridBagConstraints.REMAINDER;

/**
 * 此类应该只作为 {@link MyFrame#setContentPane(Container)} 的参数
 */
public class MenuPanel extends MyPanel {
    private Box verticalBox;
    private JPanel localPlayingPanel;
    private JPanel onlinePlayingPanel;
    private JTextField serverIPTextField;
    private JTextField serverPortTextField;
    private JTextField tankNameTextField;
    private Box horizontalBox;
    private JRadioButtonGroup buttonGroup;
    private JComboBox<Integer> playerNumComboBox;
    private JComboBox<File> wallMapFilesComboBox;
    private JRadioButton localPlayingRadioButton;
    private JRadioButton onlinePlayingRadioButton;
    private JButton launchButton;
    private AtomicBoolean attached = new AtomicBoolean(false); // 是否曾被设置为 MyFrame 的 contentPanel
    
    public MenuPanel() {
        super();
    }
    
    /**
     * 构建组件内容
     */
    @Override
    public void setupUI() {
        if (attached.get()) { // 不调用第二次
            return;
        }
        horizontalBox = Box.createHorizontalBox();
        verticalBox = Box.createVerticalBox();
        localPlayingPanel = new JPanel();
        localPlayingPanel.setBorder(new LineBorder(Config.BORDER_COLOR));
        playerNumComboBox = new JComboBox<>(new Integer[]{1, 2});
        wallMapFilesComboBox = new JComboBox<>(WallGroup.scanBinaryBitmapFiles("res"));
        localPlayingRadioButton = new JRadioButton(PlayingMode.LOCAL);
        localPlayingRadioButton.setSelected(true);
        onlinePlayingPanel = new JPanel();
        serverIPTextField = new JTextField();
        serverIPTextField.setColumns(10);
        serverPortTextField = new JTextField();
        serverPortTextField.setColumns(4);
        onlinePlayingRadioButton = new JRadioButton(PlayingMode.ONLINE);
        tankNameTextField = new JTextField();
        tankNameTextField.setColumns(serverIPTextField.getColumns() + serverPortTextField.getColumns());
        buttonGroup = new JRadioButtonGroup();
        buttonGroup.add(localPlayingRadioButton);
        buttonGroup.add(onlinePlayingRadioButton);
        launchButton = new JButton(Config.translation.launchButtonText);
        launchButton.addActionListener((e) -> {
            String command = buttonGroup.getSelectedActionCommand();
            if (command == null) {
                JOptionPane.showConfirmDialog(this, Config.translation.playingModeNotChosen,
                        Config.translation.errorTitle, JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            } else if (command.equals(PlayingMode.LOCAL)) {
                try {
                    GameSession.LocalSession session = GameSession.LocalSession.createLocalSession((Integer) playerNumComboBox.getSelectedItem(), (File) wallMapFilesComboBox.getSelectedItem());
                    // 切换到GamePanel
                    GamePanel gamePanel = new GamePanel(session);
                    MyFrame.getInstance().setContentPane(gamePanel);
                    gamePanel.start();
                } catch (IOException | IllegalArgumentException ex) {
                    JOptionPane.showConfirmDialog(this, Config.translation.readingWallMapErrorText,
                            Config.translation.errorTitle, JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                }
            } else if (buttonGroup.getSelectedActionCommand().equals(PlayingMode.ONLINE)) {
                // todo invoke online game
            }
        });
        
        add(horizontalBox);
        horizontalBox.add(Box.createVerticalStrut(Config.WINDOW_HEIGHT - 10));
        horizontalBox.add(verticalBox);
        
        verticalBox.add(new JLabel(Config.translation.menuPanelTitle));
        
        verticalBox.add(localPlayingPanel);
        localPlayingPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = REMAINDER;
        localPlayingPanel.add(localPlayingRadioButton, constraints);
        constraints.gridwidth = 1;
        localPlayingPanel.add(new JLabel(Config.translation.playerNumLabelText), constraints);
        constraints.gridwidth = REMAINDER;
        localPlayingPanel.add(playerNumComboBox, constraints);
        constraints.gridwidth = 1;
        localPlayingPanel.add(new JLabel(Config.translation.wallMapFilesComboLabelText), constraints);
        constraints.gridwidth = REMAINDER;
        localPlayingPanel.add(wallMapFilesComboBox, constraints);
        
        verticalBox.add(onlinePlayingPanel);
        onlinePlayingPanel.setLayout(new GridBagLayout());
        onlinePlayingPanel.setBorder(new LineBorder(Config.BORDER_COLOR));
        constraints = new GridBagConstraints();
        constraints.gridwidth = REMAINDER;
        onlinePlayingPanel.add(onlinePlayingRadioButton, constraints);
        constraints.gridwidth = 1;
        constraints.weightx = 1;
        onlinePlayingPanel.add(new JLabel(Config.translation.typeIPAddressLabelText), constraints);
        constraints.weightx = 4;
        onlinePlayingPanel.add(serverIPTextField, constraints);
        constraints.weightx = 1;
        onlinePlayingPanel.add(new JLabel(Config.translation.ipAddressSeparator), constraints);
        constraints.gridwidth = REMAINDER;
        onlinePlayingPanel.add(serverPortTextField, constraints);
        constraints.gridwidth = 1;
        constraints.weightx = 1;
        onlinePlayingPanel.add(new JLabel(Config.translation.typeNameLabelText), constraints);
        constraints.gridwidth = REMAINDER;
        onlinePlayingPanel.add(tankNameTextField, constraints);
        
        verticalBox.add(launchButton);
        setSize(Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);
        attached.set(true);
    }
}
