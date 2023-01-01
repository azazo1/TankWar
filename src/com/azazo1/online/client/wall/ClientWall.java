package com.azazo1.online.client.wall;

import com.azazo1.game.wall.Wall;

public class ClientWall extends Wall {
    public ClientWall(int startX, int startY) {
        super(startX, startY);
    }

    public ClientWall(int startX, int startY, int width, int height) {
        super(startX, startY, width, height);
    }
}
