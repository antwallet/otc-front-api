package cn.com.otc.schedule.service;

import cn.com.otc.schedule.OtcThreadPool;
import cn.hutool.json.JSONUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class ScheduleComponent {

    private final ScheduledExecutorService pool;
    private final ConcurrentMap<String, ConcurrentMap<String, ScheduledFuture<?>>> taskFutureMap;
    private final ConcurrentMap<String, ScheduledFuture<?>> circleFutures;

    public ScheduleComponent(int core, String threadName) {
        if( core < 1 ) {
            core = 1;
        }
        if( threadName == null || threadName.equals("") ) {
            threadName = "schedule_cpt_";
        }
        pool =  new ScheduledThreadPoolExecutor(core, new OtcThreadPool(threadName));
        taskFutureMap = new ConcurrentHashMap<>();
        circleFutures = new ConcurrentHashMap<>();
    }

    /**
     * 固定时间运行
     *
     * @param scheduleKey 当此值有设置时，则会主动判定是否存在相同任务，存在则会先关闭，没设置时，请注意自己手动关闭，请在类ScheduleTaskKey里定义，这样好管理
     * @param command
     * @param initialDelay
     * @param period
     * @param unit
     */
    public ScheduledFuture<?> addFixedRate(String scheduleKey, Runnable command, long initialDelay, long period, TimeUnit unit) {
        if( null == scheduleKey || "".equals(scheduleKey) ) {
            return pool.scheduleAtFixedRate(command, initialDelay, period, unit);
        }
        ScheduledFuture<?> oldFuture = circleFutures.get(scheduleKey);
        if( oldFuture != null ) {
            oldFuture.cancel(true);
        }
        if(command == null) {
        	return null;
        }
        return  circleFutures.put(scheduleKey, pool.scheduleAtFixedRate(command, initialDelay, period, unit));
    }

    /**
     * 固定时间运行, 之前调用过，再次调用请注意自己手动关闭以前的任务
     *
     * @param command
     * @param initialDelay
     * @param period
     * @param unit
     * @deprecated 之前调用过的，如果重新被赋值了。可能会出现以前的任务还在跑，需要自己手动去关闭，推荐用带scheduleKey的方法
     */
    @Deprecated
    public ScheduledFuture<?> addFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return addFixedRate(null, command, initialDelay, period, unit);
    }

    /**
     * 固定延迟时间运行
     *
     * @param scheduleKey 当此值有设置时，则会主动判定是否存在相同任务，存在则会先关闭，没设置时，请注意自己手动关闭，请在类ScheduleTaskKey里定义，这样好管理
     * @param command
     * @param initialDelay
     * @param period
     * @param unit
     */
    public ScheduledFuture<?> addFixedDelay(String scheduleKey, Runnable command, long initialDelay, long period, TimeUnit unit) {
        if( null == scheduleKey || "".equals(scheduleKey) ) {
            return pool.scheduleWithFixedDelay(command, initialDelay, period, unit);
        }
        ScheduledFuture<?> oldFuture = circleFutures.get(scheduleKey);
        if( oldFuture != null ) {
            oldFuture.cancel(true);
        }
        
        ScheduledFuture<?> future = pool.scheduleWithFixedDelay(command, initialDelay, period, unit);
        circleFutures.put(scheduleKey, future);
        return future;
    }

    /**
     * 固定延迟时间运行, 之前调用过，再次调用请注意自己手动关闭以前的任务
     *
     * @param command
     * @param initialDelay
     * @param period
     * @param unit
     * @deprecated 之前调用过的，如果重新被赋值了。可能会出现以前的任务还在跑，需要自己手动去关闭，推荐用带scheduleKey的方法
     */
    @Deprecated
    public ScheduledFuture<?> addFixedDelay(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return addFixedDelay(null, command, initialDelay, period, unit);
    }

    /**
     * 延迟运行,仅运行一次
     * @param initialDelay
     * @param unit
     */
    public ScheduledFuture<?> execute(Runnable command, long initialDelay , TimeUnit unit){
        return pool.schedule(command, initialDelay, unit);
    }
    /**
     * 运行指定线程
     */
    public void execute(Runnable run) {
        pool.execute(run);
    }

    /**
     * 若只调用一次，则无需手动调用cancelSchedule
     * 不适用场景：Runnable里又继续调用schedule，不推荐使用，请使用scheduleWithoutCancel
     * @param component
     * @param identity
     * @param task
     * @param delay
     * @param unit
     */
    public void schedule(String component, String identity, Runnable task, long delay, TimeUnit unit) {
        if (delay < 0) {
            return;
        }
        ConcurrentMap<String, ScheduledFuture<?>> map = taskFutureMap.get(component);
        if (null == map) {
            synchronized (taskFutureMap) {
                map = taskFutureMap.get(component);
                if( null == map ) {
                    map = new ConcurrentHashMap<>();
                    taskFutureMap.put(component, map);
                }
            }
        }
        ScheduledFuture<?> existFuture = map.remove(identity);
        if (existFuture != null) {
            existFuture.cancel(true);
        }
        Runnable afterRunnable = () -> {
            try {
                task.run();
            } finally {
                cancelSchedule(component, identity);
            }
        };
        map.put(identity, pool.schedule(afterRunnable, delay, unit));
    }

    /**
     * 若只调用一次，则需手动调用cancelSchedule，否则可能出现内存泄漏的情况
     * 不适用场景：只调用一次的情况，不推荐使用
     * 适用场景：Runnable里又调用该方法，实现类似定时循环执行的任务
     * @param component
     * @param identity
     * @param task
     * @param delay
     * @param unit
     */
    public void scheduleWithoutCancel(String component, String identity, Runnable task, long delay, TimeUnit unit) {
        if (delay < 0) {
            return;
        }
        ConcurrentMap<String, ScheduledFuture<?>> map = taskFutureMap.get(component);
        if (null == map) {
            synchronized (taskFutureMap) {
                map = taskFutureMap.get(component);
                if( null == map ) {
                    map = new ConcurrentHashMap<>();
                    taskFutureMap.put(component, map);
                }
            }
        }
        ScheduledFuture<?> existFuture = map.remove(identity);
        if (existFuture != null) {
            existFuture.cancel(true);
        }
        map.put(identity, pool.schedule(task, delay, unit));
    }

    public void cancelSchedule(String component, String identity) {
        ConcurrentMap<String, ScheduledFuture<?>> map = taskFutureMap.get(component);
        if (null != map) {
            ScheduledFuture<?> future = map.remove(identity);
            if (null != future) {
                future.cancel(true);
            }
            if (map.size() == 0) {
                taskFutureMap.remove(component);
            }
        }
    }
    /**
     * 根据 component 关闭定时器
     * @date 2019年11月20日
     * @param component
     */
	public void cancelSchedule(String component) {
		List<String> identityList = getScheduleIdentitys(component);
		if (null == identityList) {
			return;
		}
		for (String identity : identityList) {
			cancelSchedule(component, identity);
		}
	}
    /**
     * 取消定时任务
     * @param scheduleKey
     */
    public void cancelFixedSchedule(String scheduleKey) {
    	ScheduledFuture<?> schedule = circleFutures.remove(scheduleKey);
    	if(schedule != null) {
    		schedule.cancel(true);
    	}
    }

    /**
     * 获取所有定时任务的组件列表
     * @return
     */
    public List<String> getScheduleComponentKeys() {
        if( taskFutureMap != null && !taskFutureMap.isEmpty() ) {
            return new ArrayList<>(taskFutureMap.keySet());
        }
        return null;
    }

    /**
     * 根据定时任务组件名获取任务标识列表
     * @param component
     * @return
     */
    public List<String> getScheduleIdentitys(String component) {
        if( taskFutureMap != null) {
            ConcurrentMap<String, ScheduledFuture<?>> map = taskFutureMap.get(component);
            if( map != null ) {
                return new ArrayList<>(map.keySet());
            }
        }
        return null;
    }

    /**
     * identity是否在缓存队列中
     * @param component
     * @param identity
     * @return
     */
    public boolean inSchedule(String component, String identity) {
        if( taskFutureMap == null ) {
            return false;
        }
        ConcurrentMap<String,ScheduledFuture<?>> map = taskFutureMap.get(component);
        if( map == null ) {
            return false;
        }
        return map.containsKey(identity);
    }

    public void shuntdown() {
        pool.shutdown();
    }
    /**
     * 获取循环运行任务
     * @return
     */
    public String getFixedScheduleStatus() {
    	if(this.circleFutures == null || this.circleFutures.size() == 0) {
    		return "当前没有circle任务";
    	}
    	return JSONUtil.toJsonStr(this.circleFutures);
    }
}
