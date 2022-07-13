package com.azazo1.ui;

import javax.swing.*;
import java.awt.*;

public class MyFrame extends JFrame {
    @Override
    public Component add(Component comp) {
        throw new IllegalCallerException("This frame supports content panel only.");
    }
    
    @Override
    public void setContentPane(Container contentPane) {
        super.setContentPane(contentPane);
    }
}
