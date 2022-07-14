package com.azazo1.ui;

import javax.swing.*;

/**
 * 对应 {@link MyFrame} 的 JPanel
 */
public abstract class MyPanel extends JPanel {
    /**
     * 这个方法无需手动调用，在此对象被设置为 MyFrame 的 contentPane 后自动调用
     * 当第二次 {@link MyFrame#setContentPane(MyPanel)} 时, 子类要判断是否进行 UI 的重置
     */
    public abstract void setupUI();
}
