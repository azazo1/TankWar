package com.azazo1.ui;

import com.azazo1.Config;
import com.azazo1.game.GameMap;
import com.azazo1.game.tank.TankBase;

import javax.swing.*;

public class ResultPanel extends MyPanel {
    protected final GameMap.GameInfo info;
    
    public ResultPanel(GameMap.GameInfo gameInfo) {
        info = gameInfo;
    }
    
    @Override
    public void setupUI() {
        Box hBox = Box.createHorizontalBox();
        Box vBox = Box.createVerticalBox();
        hBox.add(Box.createVerticalStrut(Config.WINDOW_HEIGHT));
        hBox.add(vBox);
        
        vBox.add(Box.createHorizontalStrut(Config.WINDOW_WIDTH));
        Box listVBox = Box.createVerticalBox();
        JPanel listPanel = new JPanel();
        for (TankBase.TankInfo info1 : info.getTanksInfo()) {
            listVBox.add(new MyLabel() {
                {
                    setText(info1);
                }
            });
        }
        vBox.add(new JLabel(Config.translation.resultTitle));
        listPanel.add(listVBox);
        vBox.add(listPanel);
        vBox.add(new JButton(Config.translation.backToMenuButtonText) {{
            ResultPanel.this.setVisible(false);
            addActionListener((e) -> MyFrame.getInstance().setContentPane(MenuPanel.getInstance()));
        }});
        setVisible(true);
        add(hBox);
    }
}
