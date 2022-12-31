package com.azazo1.ui;

import com.azazo1.Config;
import com.azazo1.game.GameMap;
import com.azazo1.game.tank.TankBase;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ResultPanel extends MyPanel {
    protected final @Nullable GameMap.GameInfo info;
    protected final boolean doShowReturnButton;

    public ResultPanel(GameMap.GameInfo gameInfo) {
        this(gameInfo, true);
    }

    public ResultPanel(@Nullable GameMap.GameInfo gameInfo, boolean doShowReturnButton) {
        info = gameInfo;
        this.doShowReturnButton = doShowReturnButton;
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
        if (info == null || info.getTanksInfo().isEmpty()) {
            listVBox.add(new MyLabel() {{
                setText(Config.translation.emptyResult);
            }});
        } else {
            for (TankBase.TankInfo info1 : info.getTanksInfo()) {
                listVBox.add(new MyLabel() {
                    {
                        setText(info1);
                    }
                });
            }
        }
        vBox.add(new JLabel(Config.translation.resultTitle));
        listPanel.add(listVBox);
        vBox.add(listPanel);
        if (doShowReturnButton) {
            vBox.add(new JButton(Config.translation.backToMenuButtonText) {{
                ResultPanel.this.setVisible(false);
                addActionListener((e) -> MyFrame.getInstance().setContentPane(MenuPanel.getInstance()));
            }});
        }
        setVisible(true);
        add(hBox);
    }
}
