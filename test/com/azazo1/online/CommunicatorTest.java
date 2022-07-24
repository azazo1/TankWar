package com.azazo1.online;

import com.azazo1.game.tank.TankBase;
import com.azazo1.util.Tools;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommunicatorTest {
    
    @Test
    void sendObject() throws IOException, ClassNotFoundException {
        TankBase.TankInfo targetObject = new TankBase(500) {{
            go(100);
        }}.getInfo();
        // TankBase.TankInfo targetObject = null; 会报错
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream objStream = new ObjectOutputStream(bytes);
        objStream.writeObject(targetObject);
        objStream.flush();
        
        ByteArrayInputStream input = new ByteArrayInputStream(bytes.toByteArray());
        System.out.println(targetObject.getClass().getTypeName());
        Object o = new ObjectInputStream(input).readObject();
        assertEquals(targetObject.getClass().getTypeName(), o.getClass().getTypeName()); // 测试是否还有原始类信息
        assertTrue(o instanceof TankBase.TankInfo); // 测试是否还有原始类信息
        assertEquals(targetObject.getRect(), ((TankBase.TankInfo) o).getRect());// 测试是否还能复原成原始类
        System.out.println(targetObject.getRect());
    }
}