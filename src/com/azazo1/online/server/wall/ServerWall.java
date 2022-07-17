package com.azazo1.online.server.wall;

import com.azazo1.game.wall.Wall;

public class ServerWall extends Wall {
    {
        doPaint.set(false);
    }
    
    public ServerWall(int startX, int startY) {
        super(startX, startY);
    }
    
    public ServerWall(int startX, int startY, int width, int height) {
        super(startX, startY, width, height);
    }
}
