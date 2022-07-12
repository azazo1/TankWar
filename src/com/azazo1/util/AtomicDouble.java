package com.azazo1.util;

import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Double.doubleToLongBits;
import static java.lang.Double.longBitsToDouble;

public class AtomicDouble {
    private AtomicLong bits;
    
    public AtomicDouble(double initialValue) {
        bits = new AtomicLong(doubleToLongBits(initialValue));
    }
    
    public final double getAndSet(double newValue) {
        return longBitsToDouble(bits.getAndSet(doubleToLongBits(newValue)));
    }
    
    public final boolean compareAndSet(double expect, double update) {
        return bits.compareAndSet(doubleToLongBits(expect), doubleToLongBits(update));
    }
    
    public final void set(double newValue) {
        bits.set(doubleToLongBits(newValue));
    }
    
    public final double get() {
        return Double.longBitsToDouble(bits.get());
    }
}
