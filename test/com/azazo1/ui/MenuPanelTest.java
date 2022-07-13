package com.azazo1.ui;

import javax.swing.*;
import java.awt.*;

class MenuPanelTest {
    
    public static void main(String[] args) {
        JFrame f = new JFrame();
        MenuPanel m = new MenuPanel();
        f.setContentPane(m);
        EventQueue.invokeLater(
                () -> m.setupUI(f)
        );
        f.setVisible(true);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}