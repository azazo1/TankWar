package com.azazo1.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 让某个方法调用降低频率的判断器
 */
public class IntervalTicker {
    public final long intervalTime;
    public final AtomicLong lastExecutionTime = new AtomicLong(0);

    public IntervalTicker(long intervalTime) {
        this.intervalTime = intervalTime;
    }

    /**
     * 判断当前是否可以执行, 若可以时, 会保存当前调用时间戳
     */
    public boolean judgeCanExecute() {
        long nowTime = Tools.getRealTimeInMillis();
        if (nowTime > intervalTime + lastExecutionTime.get()) {
            lastExecutionTime.set(nowTime);
            return true;
        }
        return false;
    }
}
