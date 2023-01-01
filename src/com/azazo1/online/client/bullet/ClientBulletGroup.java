package com.azazo1.online.client.bullet;

import com.azazo1.game.bullet.BulletBase;
import com.azazo1.game.bullet.BulletGroup;
import org.jetbrains.annotations.NotNull;

import java.util.Vector;

public class ClientBulletGroup extends BulletGroup {
    /**
     * <h3>更新子弹信息</h3>
     * 同步时可能会出现 this 有而 info 没有的子弹
     */
    public void syncBullets(@NotNull Vector<BulletBase.BulletInfo> bulletInfos) {
        bullets.clear();
        for (BulletBase.BulletInfo info : bulletInfos) {
            ClientBullet bullet = new ClientBullet(info.getSeq(), (int) info.getRect().getCenterX(), (int) info.getRect().getCenterY(), info.getOrientation());
            addBullet(bullet);
        }
    }
}
