package com.azazo1.ui;

import com.azazo1.Config;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

import static com.azazo1.util.Tools.resizeFrame;
import static java.awt.GridBagConstraints.*;

public class MenuPanel extends JPanel {
    private Box verticalBox;
    private JPanel localPlayingPanel;
    private JPanel onlinePlayingPanel;
    private JTextField serverIPTextField;
    private JTextField serverPortTextField;
    private JTextField tankNameTextField;
    private Box horizontalBox;
    private ButtonGroup buttonGroup;
    private JComboBox<Integer> playerNumComboBox;
    private JRadioButton localPlayingRadioButton;
    private JRadioButton onlinePlayingRadioButton;
    private JButton launchButton;
    
    public MenuPanel() {
        super();
    }
    
    /**
     * 构建组件内容
     *
     * @apiNote 应该在被放到父组件后才调用
     */
    public void setupUI(JFrame frame) {
        if (getParent() == null) {
            throw new IllegalStateException("setupUI should be called after MenuPanel was added to the parent.");
        }
        horizontalBox = Box.createHorizontalBox();
        verticalBox = Box.createVerticalBox();
        localPlayingPanel = new JPanel();
        localPlayingPanel.setBorder(new LineBorder(Config.BORDER_COLOR));
        playerNumComboBox = new JComboBox<>(new Integer[]{1, 2});
        localPlayingRadioButton = new JRadioButton(Config.translation.localPlayingPanelTitle);
        launchButton = new JButton(Config.translation.launchButtonText);
        onlinePlayingPanel = new JPanel();
        serverIPTextField = new JTextField();
        serverIPTextField.setColumns(10);
        serverPortTextField = new JTextField();
        serverPortTextField.setColumns(4);
        onlinePlayingRadioButton = new JRadioButton(Config.translation.onlinePlayingPanelTitle);
        tankNameTextField = new JTextField();
        tankNameTextField.setColumns(5);
        buttonGroup = new ButtonGroup();
        
        add(horizontalBox);
        horizontalBox.add(Box.createVerticalStrut(Config.WINDOW_HEIGHT));
        horizontalBox.add(verticalBox);
        
        verticalBox.add(localPlayingPanel);
        localPlayingPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = REMAINDER;
        constraints.anchor = FIRST_LINE_START;
        localPlayingPanel.add(localPlayingRadioButton, constraints);
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridwidth = 1;
        localPlayingPanel.add(new JLabel(Config.translation.playerNumLabelText), constraints);
        localPlayingPanel.add(playerNumComboBox, constraints);
        constraints.gridwidth = REMAINDER;
        
        verticalBox.add(onlinePlayingPanel);
        onlinePlayingPanel.setLayout(new GridBagLayout());
        onlinePlayingPanel.setBorder(new LineBorder(Config.BORDER_COLOR));
        constraints = new GridBagConstraints();
        constraints.gridwidth = REMAINDER;
        constraints.anchor = FIRST_LINE_START;
        onlinePlayingPanel.add(onlinePlayingRadioButton, constraints);
        constraints.anchor = CENTER;
        constraints.gridwidth = 1;
        constraints.weightx = 4;
        onlinePlayingPanel.add(serverIPTextField, constraints);
        constraints.weightx = 1;
        constraints.gridwidth = REMAINDER;
        onlinePlayingPanel.add(serverPortTextField, constraints);
        constraints.weightx = 1;
        constraints.gridwidth = REMAINDER;
        onlinePlayingPanel.add(tankNameTextField, constraints);
        
        verticalBox.add(launchButton);
        setSize(Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);
        resizeFrame(frame, Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);
    }
}
