package com.azazo1.ui;

import com.azazo1.Config;
import com.azazo1.base.PlayingMode;
import com.azazo1.util.JRadioButtonGroup;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

import static java.awt.GridBagConstraints.REMAINDER;

/**
 * 此类应该只作为 {@link MyFrame#setContentPane(Container)} 的参数
 */
public class MenuPanel extends JPanel {
    private Box verticalBox;
    private JPanel localPlayingPanel;
    private JPanel onlinePlayingPanel;
    private JTextField serverIPTextField;
    private JTextField serverPortTextField;
    private JTextField tankNameTextField;
    private Box horizontalBox;
    private JRadioButtonGroup buttonGroup;
    private JComboBox<Integer> playerNumComboBox;
    private JRadioButton localPlayingRadioButton;
    private JRadioButton onlinePlayingRadioButton;
    private JButton launchButton;
    
    public MenuPanel() {
        super();
    }
    
    /**
     * 构建组件内容
     */
    public void setupUI() {
        horizontalBox = Box.createHorizontalBox();
        verticalBox = Box.createVerticalBox();
        localPlayingPanel = new JPanel();
        localPlayingPanel.setBorder(new LineBorder(Config.BORDER_COLOR));
        playerNumComboBox = new JComboBox<>(new Integer[]{1, 2});
        localPlayingRadioButton = new JRadioButton(PlayingMode.LOCAL);
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
            if (buttonGroup.getSelectedActionCommand().equals(PlayingMode.LOCAL)) {
                // todo invoke local game
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
        localPlayingPanel.add(playerNumComboBox, constraints);
        constraints.gridwidth = REMAINDER;
        
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
    }
}
