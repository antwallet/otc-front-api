package cn.com.otc.schedule;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class OtcThreadPool implements ThreadFactory{
	    static final AtomicInteger poolNumber = new AtomicInteger(1);
	    final ThreadGroup group;
	    final AtomicInteger threadNumber = new AtomicInteger(1);
	    final String namePrefix;

	    public OtcThreadPool(String threadPoolName) {
	        SecurityManager s = System.getSecurityManager();
	        group = (s != null)? s.getThreadGroup() :
	                             Thread.currentThread().getThreadGroup();
	        namePrefix = threadPoolName +
	                      poolNumber.getAndIncrement() +
	                     "-thread-";
	    }

	    public Thread newThread(Runnable r) {
	        Thread t = new Thread(group, r,
	                              namePrefix + threadNumber.getAndIncrement(),
	                              0);
	        if (t.isDaemon())
	            t.setDaemon(false);
	        if (t.getPriority() != Thread.NORM_PRIORITY)
	            t.setPriority(Thread.NORM_PRIORITY);
	        return t;
	    }
	}