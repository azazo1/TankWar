package com.azazo1.ui;

import javax.swing.*;
import java.awt.*;

public class MyFrame extends JFrame {
    protected static MyFrame instance;
    
    public MyFrame() {
        super();
        if (instance != null) {
            throw new IllegalStateException("MyFrame can be created only once.");
        }
        instance = this;
    }
    
    public static MyFrame getInstance() {
        return instance;
    }
    
    @Override
    public Component add(Component comp) {
        throw new IllegalCallerException("This frame supports content panel only.");
    }
    
    @Override
    public void setContentPane(Container contentPane) {
        throw new IllegalArgumentException("MyFrame supports MyPanel only");
    }
    
    public void setContentPane(MyPanel panel) {
        super.setContentPane(panel);
        panel.setupUI();
    }
}
