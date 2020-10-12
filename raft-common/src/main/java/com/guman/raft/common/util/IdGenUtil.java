package com.guman.raft.common.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author duanhaoran
 * @since 2020/4/15 12:23 AM
 */
public class IdGenUtil {
    private static LongAdder id = new LongAdder();

    public static Long gen() {
        id.increment();
        return id.longValue();
    }
}
