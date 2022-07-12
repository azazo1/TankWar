package com.azazo1.wall;

import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class QNodeTest {
    
    @Test
    void fillSonsToDepth() {
        QTreeWallStorage t = new QTreeWallStorage(500, 500, 5);
        assertEquals(t.getFirst().getFirst().copyRect().width, 500 / 2 / 2);
    }
    
    @Test
    void dispatch() {
        QTreeWallStorage t = new QTreeWallStorage(1000, 1000, 4);
        Wall wall = new Wall(250, 375, 50, 50);
        int get = t.dispatch(wall);
        assertEquals(0b1_10_11_00, get);
        assertEquals(3, t.convertToArray(get).length);
        assertSame(t.getFirst().getForth().getThird(), t.getLeave(get));
        Rectangle rect = wall.getRect();
        assertSame(t.getFirst().getForth().getThird(), t.switchLeave(new Point((int) rect.getCenterX(), (int) rect.getCenterY())));
    }
}