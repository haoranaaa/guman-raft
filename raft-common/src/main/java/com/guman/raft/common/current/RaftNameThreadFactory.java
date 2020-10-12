package com.guman.raft.common.current;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author duanhaoran
 * @since 2020/4/15 5:49 PM
 */
public class RaftNameThreadFactory implements ThreadFactory{

    private static AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private final boolean daemon;

    public RaftNameThreadFactory(String namePrefix, boolean daemon) {
        this.namePrefix = namePrefix;
        this.daemon = daemon;

    }

    public RaftNameThreadFactory(String namePrefix) {
        this(namePrefix, false);
    }

    @Override
    public Thread newThread(Runnable runnable) {
        final Thread thread = new Thread(runnable, namePrefix + threadNumber.getAndIncrement());
        thread.setDaemon(daemon);
        return thread;
    }


}
