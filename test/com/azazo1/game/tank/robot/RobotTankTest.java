package com.azazo1.game.tank.robot;

import com.azazo1.game.session.GameSession;
import com.azazo1.game.tank.TankBase;
import com.azazo1.game.tank.TankGroup;
import com.azazo1.ui.GamePanel;
import com.azazo1.ui.MyFrame;
import com.azazo1.util.Tools;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

class RobotTankTest {
    @Test
    void generateWayPointGraph() throws IOException {
        RobotTank twr = new RobotTank();
        TankBase tank = new TankBase();
        MyFrame frame = new MyFrame();
        GameSession.LocalSession gameSession = GameSession.LocalSession.createLocalSession(
                2,0, new String[]{"a", "b"}, Tools.getFileURL("wallmap/TidyMap.mwal").url().openStream());

        // 设置为只有 TWR 和 另外一个目标坦克
        TankGroup tankGroup = gameSession.getGameMap().getTankGroup();
        tankGroup.getLivingTanks().forEach(tankGroup::removeTank);
        tankGroup.addTank(twr);
        tankGroup.addTank(tank);

        frame.setLayout(new BorderLayout());
        new GamePanel(gameSession);

        twr.randomlyTeleport();
        twr.setName("Robot");
        tank.randomlyTeleport();

        gameSession.start();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                gameSession.stop();
            }
        });
        frame.setBounds(10, 10, 500, 500);
        try {
            while (!gameSession.isOver()) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}