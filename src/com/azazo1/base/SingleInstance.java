package com.azazo1.base;

/**
 * 类只能有一个实例
 */
public interface SingleInstance {
    /**
     * 检测是否已有实例并处理
     */
    void checkInstance();
    
    boolean hasInstance();
}
