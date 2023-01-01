package com.azazo1.ui;

import com.azazo1.Config;
import com.azazo1.game.bullet.BulletGroup;
import com.azazo1.game.tank.TankBase;
import com.azazo1.game.tank.TankGroup;
import com.azazo1.online.msg.MsgBase;
import com.azazo1.util.Tools;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

/**
 * 用于侧边栏快速辅助显示信息
 */
public class MyLabel extends JLabel {
    public static final Border TANK_INFO_BORDER = new LineBorder(Config.BORDER_COLOR, 2, true);

    public MyLabel() {
        setText(Config.translation.loading);
    }

    /**
     * 获得一个水平方向的同时具有 {@link MyLabel} 和 {@link JTextField} 的 {@link Box}, 子代顺序如前半句
     *
     * @param text   MyLabel 文字
     * @param column JTextField 列
     */
    public static @NotNull Box createHBoxWithMyLabelAndJTextField(String text, int column) {
        return MyPanel.createHBoxContainingPairComponent(
                new MyLabel() {{
                    setText(text);
                }},
                new JTextField(column)
        );
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        setBorder(null);
    }

    /**
     * 显示帧率
     *
     * @param FPS         当前帧率
     * @param averageFPS  平均帧率
     * @param placeHolder 方法调用标识
     */
    public void setText(int FPS, int averageFPS, @Nullable Tools placeHolder) {
        setText(Config.translation.FPSLabelText + FPS + " / " + averageFPS);
    }

    /**
     * 显示消息接收频率 (Client 接收服务端同步消息)
     *
     * @param rate        当前消息接收速率
     * @param averageRate 平均帧率
     * @param placeHolder 方法调用标识
     */
    public void setText(int rate, int averageRate, @Nullable MsgBase placeHolder) {
        setText(Config.translation.msgSyncRate + rate + " / " + averageRate);
    }

    /**
     * 显示屏幕宽高
     */
    public void setText(int width, int height) {
        setText(String.format(Config.translation.mapSizeLabelFormat, width, height));
    }

    /**
     * 显示坦克数量
     *
     * @param tankNum      现存坦克数量
     * @param totalTankNum 总共坦克数量
     * @param placeHolder  用于标识为调用此方法
     */
    public void setText(int tankNum, int totalTankNum, @Nullable TankGroup placeHolder) {
        setText(Config.translation.tankNumLabelText + tankNum + " / " + totalTankNum);
    }

    /**
     * 显示子弹数量
     *
     * @param placeHolder 用于标识为调用此方法
     */
    public void setText(int bulletNum, @Nullable BulletGroup placeHolder) {
        setText(Config.translation.bulletNumLabelText + bulletNum);
    }

    /**
     * 显示四叉树深度
     */
    public void setText(int qTreeDepth) {
        setText(Config.translation.qTreeDepthLabelText + qTreeDepth);
    }

    /**
     * 显示坦克基本信息
     */
    public void setText(TankBase.@NotNull TankInfo info) {
        setText(String.format(
                Config.translation.basicTankInfoFormat,
                info.getNickname() == null ? "" : info.getNickname(), info.getSeq(),
                info.getRank(),
                info.getNowEndurance(), info.getTotalEndurance(),
                info.getLivingTime() / 1000.0
        ));
        setBorder(TANK_INFO_BORDER);
    }
}
