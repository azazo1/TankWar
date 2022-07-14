package com.azazo1.ui;

import com.azazo1.Config;
import com.azazo1.game.bullet.BulletGroup;
import com.azazo1.game.tank.TankGroup;
import com.azazo1.util.Tools;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * 用于侧边栏快速辅助显示信息
 */
public class MyLabel extends JLabel {
    public MyLabel() {
        setText(Config.translation.loading);
    }
    /**
     * 显示帧率
     *
     * @param FPS         当前帧率
     * @param averageFPS  平均帧率
     * @param placeHolder 方法调用标识
     */
    public void setText(int FPS, int averageFPS, @Nullable Tools placeHolder) {
        super.setText(Config.translation.FPSLabelText + FPS + " / " + averageFPS);
    }
    
    /**
     * 显示屏幕宽高
     */
    public void setText(int width, int height) {
        super.setText(String.format(Config.translation.mapSizeLabelFormat, width, height));
    }
    
    /**
     * 显示坦克数量
     *
     * @param tankNum      现存坦克数量
     * @param totalTankNum 总共坦克数量
     * @param placeHolder  用于标识为调用此方法
     */
    public void setText(int tankNum, int totalTankNum, @Nullable TankGroup placeHolder) {
        super.setText(Config.translation.tankNumLabelText + tankNum + " / " + totalTankNum);
    }
    
    /**
     * 显示子弹数量
     *
     * @param placeHolder 用于标识为调用此方法
     */
    public void setText(int tankNum, @Nullable BulletGroup placeHolder) {
        super.setText(Config.translation.bulletNumLabelText + tankNum);
    }
}
