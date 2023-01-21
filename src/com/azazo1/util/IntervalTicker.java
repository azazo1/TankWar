package com.azazo1.util;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 让某个方法调用降低频率的判断器, <br>
 * 当分别设定了 {@link IntervalTicker#minIntervalTime} 和 {@link IntervalTicker#maxIntervalTime} 时, 每次的间隔时间将会在该范围内随机生成
 */
public class IntervalTicker {
    public final long minIntervalTime;
    public final long maxIntervalTime;
    public final AtomicLong nextIntervalTime = new AtomicLong(0);
    public final AtomicLong lastExecutionTime = new AtomicLong(0);

    public IntervalTicker(long intervalTime) {
        this.minIntervalTime = intervalTime;
        this.maxIntervalTime = intervalTime;
        resetNextIntervalTime();
    }

    public IntervalTicker(long minIntervalTime, long maxIntervalTime) {
        this.minIntervalTime = minIntervalTime;
        this.maxIntervalTime = maxIntervalTime;
        resetNextIntervalTime();
    }

    /**
     * 判断当前是否可以执行, 若可以时, 会保存当前调用时间戳, 并更新下一次间隔时间
     */
    public boolean judgeCanExecute() {
        long nowTime = Tools.getRealTimeInMillis();
        if (nowTime > nextIntervalTime.get() + lastExecutionTime.get()) {
            lastExecutionTime.set(nowTime);
            resetNextIntervalTime();
            return true;
        }
        return false;
    }

    private void resetNextIntervalTime() {
        nextIntervalTime.set(new Random().nextLong(minIntervalTime, maxIntervalTime + 1));
    }
}
