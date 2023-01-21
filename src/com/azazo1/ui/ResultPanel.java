package com.azazo1.ui;

import com.azazo1.Config;
import com.azazo1.game.GameMap;
import com.azazo1.game.tank.TankBase;
import com.azazo1.util.MyFrameSetting;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 显示一局游戏的结果
 */
public class ResultPanel extends MyPanel {
    protected final @Nullable GameMap.GameInfo info;
    protected final boolean doShowReturnButton;
    protected final boolean doReturnToMenuPanelOnDispose;
    private final MyFrame frame;
    private final MyFrameSetting originalFrameSetting;

    /**
     * 本构造方法会自动调用 {@link MyFrame#setContentPane(Container)}, 结束后会自动恢复 {@link MyFrame} 状态
     */
    public ResultPanel(MyFrame frame, @Nullable GameMap.GameInfo gameInfo, boolean doShowReturnButton, boolean doReturnToMenuPanelOnDispose) {
        info = gameInfo;
        this.doReturnToMenuPanelOnDispose = doReturnToMenuPanelOnDispose;
        this.doShowReturnButton = doShowReturnButton;
        this.frame = frame;

        if (doReturnToMenuPanelOnDispose) {
            // 先让上一个 ContentPanel 切换为 MenuPanel
            this.frame.setContentPane(MenuPanel.getInstance());
        }
        originalFrameSetting = new MyFrameSetting(frame);

        this.frame.setContentPane(this);
        this.frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                frame.removeWindowListener(this);
                dispose();
            }
        });
    }

    public void dispose() {
        originalFrameSetting.restore();
    }

    @Override
    public void setupUI() {
        setVisible(true);
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
                addActionListener((e) -> dispose());
            }});
        }
        add(hBox);
    }
}
