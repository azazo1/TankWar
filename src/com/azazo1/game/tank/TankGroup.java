package com.azazo1.game.tank;

import com.azazo1.game.GameMap;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

public class TankGroup {
    protected final HashMap<Integer, TankBase> tanks = new HashMap<>(); // 所有坦克 <序号, tank>
    protected final Vector<Integer> tankDeathSequence = new Vector<>(); // 用于储存坦克死亡顺序(用坦克 Seq 表示坦克), 索引 0 为最先死亡
    protected final HashSet<Integer> livingTanks = new HashSet<>(); // 存活的坦克
    protected final KeyListener keyListener = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            livingTanks.forEach((seq) -> tanks.get(seq).pressKey(e.getKeyCode()));
        }

        @Override
        public void keyReleased(KeyEvent e) {
            livingTanks.forEach((seq) -> tanks.get(seq).releaseKey(e.getKeyCode()));
        }
    };
    protected GameMap map;

    public TankGroup() {
    }

    public GameMap getGameMap() {
        return map;
    }

    /**
     * 不保证真的能从 map 中添加,本方法应由 {@link GameMap#setTankGroup(TankGroup)} 调用
     */
    public void setGameMap(GameMap map) {
        this.map = map;
        this.getGameMap().addKeyListener(keyListener);
    }

    /**
     * 不保证真的能从 map 中去除,本方法应由 {@link GameMap#setTankGroup(TankGroup)} 调用
     */
    public void clearGameMap() {
        this.getGameMap().removeKeyListener(keyListener);
        this.map = null;
    }

    public void addTank(@NotNull TankBase tank) {
        tanks.put(tank.getSeq(), tank);
        livingTanks.add(tank.getSeq());
        tank.setTankGroup(this);
    }

    /**
     * 移除坦克
     * 此方法不会在游戏进行时被调用
     */
    public void removeTank(@NotNull TankBase tank) {
        tanks.remove(tank.getSeq());
        tankDeathSequence.remove(Integer.valueOf(tank.getSeq()));
        livingTanks.remove(tank.getSeq());
        tank.clearTankGroup();
        tank.getEnduranceManager().makeDie(); // 强行销毁坦克
    }

    public void update(Graphics g) {
        if (tanks.isEmpty()) {
            return;
        }
        for (TankBase tank : tanks.values()) {
            if (tank.getEnduranceManager().isDead()) {
                if (livingTanks.contains(tank.getSeq())) {
                    tankDeathSequence.add(tank.getSeq());
                    livingTanks.remove(tank.getSeq());
                }
                continue;
            }
            tank.update(g.create()); // 更新坦克, 传入一个副本
        }
    }

    /**
     * 获得现在存活的坦克数量
     */
    public int getLivingTankNum() {
        return tanks.size() - tankDeathSequence.size();
    }

    /**
     * 获得所有坦克信息<br>
     * 同时将排名分配到每个 {@link TankBase.TankInfo} 上
     *
     * @return 坦克信息列表, 结果按照排名高到低排序
     */
    public Vector<TankBase.TankInfo> getTanksInfo() {
        Vector<TankBase.TankInfo> infoSequence = new Vector<>();
        for (int tankSeq : livingTanks) {
            TankBase tank = tanks.get(tankSeq);
            TankBase.TankInfo info = tank.getInfo();
            info.rank = 0;
            infoSequence.add(info);
        }
        for (int i = tankDeathSequence.size() - 1; i >= 0; i--) {
            TankBase tank = tanks.get(tankDeathSequence.get(i));
            TankBase.TankInfo info = tank.getInfo();
            info.rank = tankDeathSequence.size() - i; // 从一开始
            infoSequence.add(info);
        }
        return infoSequence;
    }

    public Vector<TankBase> getLivingTanks() {
        Vector<TankBase> _livingTanks = new Vector<>();
        livingTanks.forEach((integer -> _livingTanks.add(tanks.get(integer))));
        return _livingTanks;
    }

    public TankBase getTank(int seq) {
        return tanks.get(seq);
    }
}
