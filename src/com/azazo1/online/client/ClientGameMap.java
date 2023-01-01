package com.azazo1.online.client;

import com.azazo1.game.GameMap;

import java.util.concurrent.atomic.AtomicBoolean;

public class ClientGameMap extends GameMap {
    /**
     * 用于标记这些操作是否启用了, 只做储存, 由 {@link com.azazo1.ui.OnlineWaitingRoomPanel} 控制与读取
     */
    public final AtomicBoolean
            left = new AtomicBoolean(false),
            right = new AtomicBoolean(false),
            forward = new AtomicBoolean(false),
            backward = new AtomicBoolean(false);
}
