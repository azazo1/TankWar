package com.azazo1.online.client.tank;

import com.azazo1.game.tank.TankBase;
import com.azazo1.game.tank.TankGroup;
import com.azazo1.online.client.Client;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Vector;

public class ClientTankGroup extends TankGroup {

    /**
     * <h3>同步坦克状态</h3>
     * 一般不会出现 {@link ClientTankGroup} 有而 {@link com.azazo1.game.tank.TankBase.TankInfo} 中没有的坦克
     */
    public void syncTanks(@NotNull Vector<TankBase.TankInfo> tankInfos, @NotNull Client client) {
        for (TankBase.TankInfo info : tankInfos) {
            ClientTank get = (ClientTank) getTank(info.getSeq());
            if (get == null) {
                get = new ClientTank(info.getSeq(), info.getSeq() == client.getSeq());
                addTank(get); // info 中有而本 group 没有, 补充坦克信息
            }
            get.setName(info.getNickname());
            get.turnTo(info.getOrientation());
            get.goTo(info.getRect());
            get.setEndurance(info.getNowEndurance());
            get.setLivingTime(info.getLivingTime());
            get.setRank(info.getRank());
            get.setSpareBullet(info.getSpareBulletNum());
            get.setActions(info.getPressedActions());
        }
    }

    public HashMap<Integer, TankBase> getTanks() {
        return tanks;
    }
}
