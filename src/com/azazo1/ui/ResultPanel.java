package com.azazo1.ui;

import com.azazo1.game.GameMap;

import javax.swing.*;
import java.awt.*;

public class ResultPanel extends MyPanel {
    protected final GameMap.GameInfo info;
    
    public ResultPanel(GameMap.GameInfo gameInfo) {
        info = gameInfo;
    }
    
    @Override
    public void setupUI() {
        // todo 完善
        var a = new JTextArea(info.toString(), 10, 20);
        a.setLineWrap(true);
        setLayout(new BorderLayout());
        add(a, BorderLayout.CENTER);
    }
}
