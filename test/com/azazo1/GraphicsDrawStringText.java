package com.azazo1;

import com.azazo1.util.Tools;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.Random;
import java.util.Vector;

import static java.awt.Font.BOLD;
import static org.junit.jupiter.api.Assertions.fail;

class GraphicsDrawStringText {
    JFrame frame;
    long t1;
    Vector<Long> times = new Vector<>(60);

    @BeforeEach
    void setUp() {
        frame = new JFrame();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        t1 = Tools.getRealTimeInMillis();
    }

    @Test
    void drawString() {
        Graphics2D g2d = (Graphics2D) frame.getGraphics().create();
        for (int i = 0; i < 60; i++) {
            long start = Tools.getRealTimeInMillis();
            g2d.setFont(new Font(
                    new String[]{"楷体", "宋体"}[new Random().nextInt(0, 2)],
                    BOLD, new Random().nextInt(10, 20)
            ));
            g2d.drawString("", 0, 0);
            times.add(Tools.getRealTimeInMillis() - start);
        }
    }

    @AfterEach
    void tearDown() {
        long deltaTime = Tools.getRealTimeInMillis() - t1;
        Tools.logLn("First: " + times.get(0));
        Tools.logLn("Max: " + Collections.max(times));
        Tools.logLn("Total: " + times.stream().reduce(Long::sum));
        if (deltaTime > 1000) {
            fail(deltaTime + "");
        }
    }
}