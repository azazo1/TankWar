package com.azazo1.util;

import com.azazo1.ui.MyFrame;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.WindowListener;
import java.util.Vector;

/**
 * 用于保存 {@link MyFrame} 状态, 以便在不同 ContentPane 切换时恢复<br>
 * 本类在实例化后会暂时清除 {@link MyFrame} 的 {@link java.awt.event.WindowListener} 并保存<br>
 * 调用 {@link #restore()} 后恢复<br>
 * 但本类不会改变其他各种 Listeners<br>
 * 新 ContentPane 应该将 {@link MyFrame#getDefaultCloseOperation()} 设置为 {@link javax.swing.WindowConstants#DISPOSE_ON_CLOSE}
 */
public class MyFrameSetting {
    public final Rectangle windowSize; // 只会用到宽高
    public final int defaultCloseOperation;
    public final boolean resizable;
    public final Container originalContentPane;
    public final MyFrame frame;
    public final String title;
    private final Vector<WindowListener> listeners = new Vector<>();

    public MyFrameSetting(@NotNull MyFrame frame) {
        this.frame = frame;
        title = frame.getTitle();
        windowSize = frame.getBounds();
        defaultCloseOperation = frame.getDefaultCloseOperation();
        resizable = frame.isResizable();
        originalContentPane = frame.getContentPane();
        originalContentPane.setVisible(false);
        storeWindowListeners();
    }

    private void storeWindowListeners() {
        listeners.clear();
        WindowListener[] get = frame.getWindowListeners();
        for (WindowListener w : get) {
            listeners.add(w);
            frame.removeWindowListener(w);
        }
    }

    public void restore() {
        frame.setTitle(title);
        frame.setSize(windowSize.getSize());
        frame.setDefaultCloseOperation(defaultCloseOperation);
        frame.setResizable(resizable);
        frame.setContentPane(originalContentPane);
        listeners.forEach(frame::addWindowListener);
        frame.setVisible(true);
        originalContentPane.setVisible(true);
    }
}
