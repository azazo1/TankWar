package com.azazo1.online.server;

import com.azazo1.game.bullet.BulletBase;
import com.azazo1.game.item.ItemBase;
import com.azazo1.game.tank.TankBase;

import java.io.Serializable;
import java.util.Vector;

/**
 * 储存游戏帧，用于服务器广播游戏状态
 * 信息: 时间戳(FrameTime)，坦克位置，坦克朝向，坦克血量、坦克弹药数，子弹位置
 */
public record GameState(long timeStamp, Vector<TankBase.TankInfo> tankInfos,
                        Vector<BulletBase.BulletInfo> bulletInfos, Vector<ItemBase.ItemInfo> itemInfos) implements Serializable {
}
