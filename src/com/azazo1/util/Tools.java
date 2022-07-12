package com.azazo1.util;

import com.azazo1.base.Config;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

public final class Tools {
    private static final AtomicInteger framesCounter = new AtomicInteger(0);
    
    /**
     * 累计帧数
     */
    public static void tickFrame() {
        framesCounter.incrementAndGet();
    }
    
    /**
     * 获得累积的总帧数
     */
    public static int getFrameCounts() {
        return framesCounter.get();
    }
    
    /**
     * 获得真实的时间
     */
    public static long getRealTimeInMillis() {
        return Calendar.getInstance().getTimeInMillis(); // 必须要每次都创建 instance 才能获得最新时间
    }
    
    /**
     * 获得游戏经过累计帧数计算出的经过时间
     * 相比于 {@link #getRealTimeInMillis()} 更推荐这个
     */
    public static long getFrameTimeInMillis() {
        return (long) (framesCounter.get() * (1000.0 / Config.FPS));
    }
    
    /**
     * 复制 BufferedImage
     */
    @NotNull
    public static BufferedImage deepCopy(@NotNull BufferedImage src) {
        ColorModel cm = src.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        return new BufferedImage(cm, src.copyData(null), isAlphaPremultiplied, null);
    }
}
