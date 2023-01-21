package com.azazo1.ui;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * 对应 {@link MyFrame} 的 JPanel
 */
public abstract class MyPanel extends JPanel {
    public static @NotNull Box createHBoxContainingPairComponent(@NotNull Component left, @NotNull Component right) {
        Box hBox = Box.createHorizontalBox();
        hBox.add(left);
        hBox.add(right);
        return hBox;
    }

    /**
     * 这个方法无需手动调用，在此对象被设置为 MyFrame 的 contentPane 后自动调用
     * 当第二次 {@link MyFrame#setContentPane(MyPanel)} 时, 子类要判断是否进行 UI 的重置
     * {@link MyFrame} 对象可用该类的 {@link MyFrame#getInstance()} 获得
     */
    public abstract void setupUI();
}
