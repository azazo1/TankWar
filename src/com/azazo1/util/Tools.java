package com.azazo1.util;

import com.azazo1.Config;
import com.azazo1.game.bullet.BulletBase;
import com.azazo1.game.tank.TankBase;
import jmp123.PlayBack;
import jmp123.output.Audio;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class Tools {
    private static final AtomicInteger framesCounter = new AtomicInteger(0);
    private static final Vector<Long> lastTickTimes = new Vector<>(10); // 最后10次 tickFrame 时间戳, 真实时间
    private static final AtomicLong firstTickTime = new AtomicLong(-1); // 真实时间
    private static final AtomicLong bias = new AtomicLong();

    /**
     * 当以 Jar 包形式运行时, 读取 Jar 包内的文件 URL, 当直接运行时直接获取文件 URL<br>
     * 不用加 "res/"<br>
     * 当在 Jar 包形式运行时, Jar 包内文件 URL 为 *.jar!/*.*
     */
    public static @NotNull MyURL getFileURL(String filePath) {
        ClassLoader cl = Tools.class.getClassLoader();
        URL url = cl.getResource(filePath);
        return new MyURL(Objects.requireNonNull(url));
    }

    /**
     * 获取 dirPath 文件夹下所有文件 URL<br>
     * <p>
     * 当以 Jar 包形式运行时, 读取 Jar 包内的文件 URLs, 当直接运行时直接获取文件 URLs<br>
     * 不用加 "res/"<br>
     * 当在 Jar 包形式运行时, Jar 包内文件 URL 为 *.jar!/*.*
     * </p>
     *
     * @param filter Jar 包模式时 {@link FilenameFilter#accept(File, String)} 第一个参数永远收到 null, 第二个参数为文件名
     * @throws NullPointerException 也许是因为文件夹不存在
     */
    public static @NotNull Vector<MyURL> getFileURLs(String dirPath, @Nullable FilenameFilter filter) throws IOException {
        ClassLoader classLoader = Tools.class.getClassLoader();
        URL url = Objects.requireNonNullElse(classLoader.getResource(dirPath), new URL("file:" + new File(dirPath).getAbsolutePath()));
        String urlStr = url.getProtocol() + ":" + url.getPath();

        Vector<MyURL> rst = new Vector<>();
        if (url.getProtocol().equals("jar")) { // jar 内
            String jarPath = urlStr.substring(0, urlStr.indexOf("!/") + 2); // 用于创建文件 URL
            JarURLConnection jarCon = (JarURLConnection) url.openConnection();
            JarFile jarFile = jarCon.getJarFile();
            Enumeration<JarEntry> jarEntries = jarFile.entries();
            // 迭代目录
            while (jarEntries.hasMoreElements()) {
                JarEntry entry = jarEntries.nextElement();
                String name = entry.getName();
                if (name.startsWith(dirPath) && !name.equals(dirPath)) { // 定位到目标目录
                    if (!entry.isDirectory() && (filter == null || filter.accept(null, name))) {
                        rst.add(new MyURL(new URL(jarPath + name)));
                    }
                }
            }
        } else if (url.getProtocol().equals("file")) {
            File dir = new File(url.getFile());
            File[] files = dir.listFiles(filter);
            for (File file : Objects.requireNonNull(files)) {
                if (file.isFile()) {
                    rst.add(new MyURL(new URL("file:" + file.getAbsolutePath())));
                }
            }
        }
        return rst;
    }

    /**
     * 记录日志, 换行
     */
    public static void logLn(String msg) {
        System.out.println(msg);
    }

    /**
     * 记录日志, 不换行
     */
    public static void log(String msg) {
        System.out.print(msg);
    }

    /**
     * 清除所有帧数信息, 便于重开游戏
     */
    public static void clearFrameData() {
        framesCounter.set(0);
        lastTickTimes.clear();
        firstTickTime.set(-1);
    }

    /**
     * 获得当前帧率<br>
     * 通过 10 除以 倒数10帧的时间 来计算
     */
    public static int getFPS() {
        if (lastTickTimes.size() < 10) {
            return getAverageFPS();
        }
        return (int) (1000.0 * 10 / (lastTickTimes.get(9) - lastTickTimes.get(0)));
    }

    /**
     * 获得平均帧率
     */
    public static int getAverageFPS() {
        return (int) (getFrameCounts() * 1000.0 / (getRealTimeInMillis() - firstTickTime.get()));
    }

    /**
     * 累计帧数, 控制帧率
     */
    public static void tickFrame() {
        tickFrame(true);
    }

    /**
     * 累计帧数, 控制帧率
     *
     * @param controlFrameRate 是否调用{@link Thread#sleep(long)} 以控制帧率
     */
    public static void tickFrame(boolean controlFrameRate) {
        long nowTime = getRealTimeInMillis();
        // 控制帧率
        if (controlFrameRate && !lastTickTimes.isEmpty()) {
            long lastTime = lastTickTimes.get(lastTickTimes.size() - 1);
            double sleepTime = (1000.0 / Config.FPS);
            while (nowTime - lastTime < sleepTime) { // 循环等待
                nowTime = getRealTimeInMillis();
                try {
                    Thread.sleep(1); // 减少 cpu 占用
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

        }
        framesCounter.incrementAndGet();
        if (firstTickTime.get() < 0) {
            firstTickTime.set(nowTime);
        }
        lastTickTimes.add(nowTime);
        while (lastTickTimes.size() > 10) {
            lastTickTimes.remove(0);
        }
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
        return getRealTimeInMillis(bias.get());
    }

    /**
     * 获得真实时间, 可设置时间偏差
     */
    public static long getRealTimeInMillis(long bias) {
        return System.currentTimeMillis() + bias;
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

    /**
     * 尝试重置 {@link JFrame} 大小使内容得到全部显示, 不断尝试直到成功一次
     */
    public static void resizeFrame(JFrame frame, int width, int height) {
        Timer timer = new Timer((int) (1000.0 / Config.FPS), null);
        timer.addActionListener((action) -> {
            if (frame == null) {
                timer.stop();
                return;
            }
            Insets insets = frame.getInsets();
            if (insets.top != 0 || insets.bottom != 0 || insets.left != 0 || insets.right != 0) {
                frame.setSize(width + insets.left + insets.right, height + insets.top + insets.bottom);
                timer.stop();
            }
        });
        timer.setRepeats(true);
        timer.start();
    }

    /**
     * 调整时间偏差, 用于同步客户端和服务端的时间
     */
    public static void setTimeBias(long bias) {
        Tools.bias.set(bias);
    }

    /**
     * 播放音效(新线程中)
     * 服务端 并不会发出声音(通过覆盖为广播事件)
     * 客户端只会在接收到 {@link com.azazo1.online.msg.GlobalEventMsg} 才会发出对应音效
     */
    public static void playSound(@NotNull URL path) {
        if (Config.doPlaySound.get()) {
            new Thread(() -> {
                try {
                    PlayBack playBack = new PlayBack(new Audio());
                    playBack.open(path, null);
                    playBack.start(false);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
    }

    /**
     * 初始化, 放在 main 前部即可
     */
    public static void init() throws IOException {
        TankBase.getSeqModule().init();
        BulletBase.getSeqModule().init();
        Tools.clearFrameData();
        // 初始化(清空) temp 目录
        File tempDir = new File(Config.TEMP_DIR);
        if (!tempDir.exists()) {
            boolean rst = tempDir.mkdirs();
        } else if (tempDir.isDirectory()) {
            deleteFileAndDir(tempDir);
            boolean rst = tempDir.mkdirs();
        } else {
            throw new FileNotFoundException("temp dir is not a DIR.");
        }
    }

    /**
     * 清空该目录下的所有内容, 并删除该目录; 或删除该文件
     */
    public static void deleteFileAndDir(@NotNull File target) {
        if (target.isFile()) {
            boolean rst = target.delete();
            return;
        }
        File[] files = target.listFiles();
        if (files == null) {
            throw new RuntimeException("wrong in listing files");
        }
        for (File f : files) {
            deleteFileAndDir(f);
        }
        target.delete();
    }
}
