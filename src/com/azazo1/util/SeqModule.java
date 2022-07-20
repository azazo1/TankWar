package com.azazo1.util;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

public final class SeqModule {
    public final AtomicInteger cur = new AtomicInteger(0);
    public final HashSet<Integer> usingSequences = new HashSet<>(); // 正在被使用的序号
    public final HashSet<Integer> spareSequences = new HashSet<>(); // 被以前创建过但是被废弃的序号
    
    public SeqModule() {
        init();
    }
    
    /**
     * 初始化, 清空所有信息, 便于重新开始游戏
     */
    public void init() {
        synchronized (this) {
            cur.set(0);
            usingSequences.clear();
            spareSequences.clear();
        }
    }
    
    /**
     * 产生新序号(未被使用), 但并不会调用 {@link #use(int)} 方法
     */
    public int next() {
        synchronized (this) {
            if (!spareSequences.isEmpty()) { // 先使用 spareSequences 里的序号
                int rst = spareSequences.iterator().next();
                spareSequences.remove(rst);
                return rst;
            }
            int rst;
            while (true) { // 跳过已经在使用的序号
                if (!isUsing((rst = cur.getAndIncrement()))) {
                    break;
                }
            }
            return rst;
        }
    }
    
    /**
     * 判断该序号是否被使用
     */
    public boolean isUsing(int seq) {
        return usingSequences.contains(seq);
    }
    
    /**
     * 将序号设置为正在使用
     */
    public void use(int seq) {
        synchronized (this) {
            if (isUsing(seq)) {
                throw new IllegalArgumentException("This sequence has been used.");
            }
            spareSequences.remove(seq);
            usingSequences.add(seq);
        }
    }
    
    /**
     * 将指定序号设置为未使用(不会判断原来是否在使用)
     */
    public void dispose(int seq) {
        synchronized (this) {
            usingSequences.remove(seq);
            spareSequences.add(seq); // HashSet 不会重复
        }
    }
}
