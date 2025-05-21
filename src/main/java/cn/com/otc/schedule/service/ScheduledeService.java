package cn.com.otc.schedule.service;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author L 用来提交定时任务
 */
public class ScheduledeService {

	private static final ScheduleComponent scheduleComponent = new ScheduleComponent(7, "Schedulede-task-");

	/**
	 * 固定时间运行
	 *
	 * @param scheduleKey 当此值有设置时，则会主动判定是否存在相同任务，存在则会先关闭，没设置时，请注意自己手动关闭，请在类ScheduleTaskKey里定义，这样好管理,如不需要请传null
	 * @param command
	 * @param initialDelay
	 * @param period
	 * @param unit
	 */
	public static ScheduledFuture<?> addFixedRate(String scheduleKey, Runnable command, long initialDelay, long period, TimeUnit unit) {
		return scheduleComponent.addFixedRate(scheduleKey, command, initialDelay, period, unit);
	}

	/**
	 * 固定时间运行(慎用)
	 *
	 * @param command
	 * @param initialDelay
	 * @param period
	 * @param unit
	 * @deprecated 之前调用过的，如果重新被赋值了。可能会出现以前的任务还在跑，需要自己手动去关闭，推荐用带scheduleKey的方法
	 */
	@Deprecated
	public static ScheduledFuture<?> addFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
		return scheduleComponent.addFixedRate(command, initialDelay, period, unit);
	}

	/**
	 * 固定延迟时间运行
	 *
	 * @param scheduleKey 当此值有设置时，则会主动判定是否存在相同任务，存在则会先关闭，没设置时，请注意自己手动关闭，请在类ScheduleTaskKey里定义，这样好管理,如不需要请传null
	 * @param command
	 * @param initialDelay
	 * @param period
	 * @param unit
	 */
	public static ScheduledFuture<?> addFixedDelay(String scheduleKey, Runnable command, long initialDelay, long period, TimeUnit unit) {
		 return scheduleComponent.addFixedDelay(scheduleKey, command, initialDelay, period, unit);
	}

	/**
	 * 固定延迟时间运行(慎用)
	 *
	 * @param command
	 * @param initialDelay
	 * @param period
	 * @param unit
	 * @deprecated 之前调用过的，如果重新被赋值了。可能会出现以前的任务还在跑，需要自己手动去关闭，推荐用带scheduleKey的方法
	 */
	@Deprecated
	public static ScheduledFuture<?> addFixedDelay(Runnable command, long initialDelay, long period, TimeUnit unit) {
		return scheduleComponent.addFixedDelay(command, initialDelay, period, unit);
	}

	/**
	 * 延迟运行,仅运行一次
	 * @param initialDelay
	 * @param unit
	 */
	public static ScheduledFuture<?> execute(Runnable command, long initialDelay, TimeUnit unit) {
		return scheduleComponent.execute(command, initialDelay, unit);
	}
	/**
	 * 运行指定线程
	 */
	public static void execute(Runnable run) {
		scheduleComponent.execute(run);
	}

	public static void schedule(String component, String identity, Runnable task, long delay, TimeUnit unit) {
		scheduleComponent.schedule(component, identity, task, delay, unit);
	}

	public static void scheduleWithoutCancel(String component, String identity, Runnable task, long delay, TimeUnit unit) {
		scheduleComponent.scheduleWithoutCancel(component, identity, task, delay, unit);
	}

	public static void cancelSchedule(String component, String identity) {
		scheduleComponent.cancelSchedule(component, identity);
	}
	/**
	 * 根据 component 关闭定时器 
	 * @date 2019年11月20日
	 * @param component
	 */
	public static void cancelSchedule(String component) {
		scheduleComponent.cancelSchedule(component);
	}

	/**
	 * 获取所有定时任务的组件列表
	 * @return
	 */
	public static List<String> getScheduleComponentKeys() {
		return scheduleComponent.getScheduleComponentKeys();
	}

	/**
	 * 根据定时任务组件名获取任务标识列表
	 * @param component
	 * @return
	 */
	public static List<String> getScheduleIdentitys(String component) {
		return scheduleComponent.getScheduleIdentitys(component);
	}
	
	private static final String END_FIXED = "_END";
	private static final String END_CALLBACK_FIXED = "_END_CALLBACK";
	/**
	 * 指定任务在某段时间内每隔delta时间运行一次
	 * @param component
	 * @param identity
	 * @param task 任务
	 * @param beginTime 开始执行时间 毫秒
	 * @param endTime 结束执行时间 毫秒
	 * @param delta 固定间隔时间 毫秒
	 */
	public static void addPeriodTask(String component,String identity,Runnable task,long beginTime,long endTime,long delta) {
		addPeriodTask(component, identity, task, beginTime, endTime, delta,null);
	}
	/**
	 * 指定任务在某短时间内每隔delta时间运行一次 
	 * @param component
	 * @param identity
	 * @param task
	 * @param beginTime 任务开始时间 毫秒
	 * @param endTime 任务结束时间 毫秒
	 * @param delta 固定间隔时间 毫秒
	 * @param callbackTask 到期后的回调任务
	 */
	public static void addPeriodTask(String component, String identity, Runnable task, long beginTime, long endTime,
			long delta, Runnable callbackTask) {
		// 取消过期任务
		String key = component+"_"+identity;
		scheduleComponent.cancelFixedSchedule(key);
		scheduleComponent.cancelSchedule(component, identity + END_FIXED);
		scheduleComponent.cancelSchedule(component, identity + END_CALLBACK_FIXED);
		
		long now = System.currentTimeMillis();
		if(now < endTime) {
			// 任务尚未结束
			ScheduledFuture<?> future = scheduleComponent.addFixedDelay(key, task, Math.max(0, beginTime - now), delta, TimeUnit.MILLISECONDS);
			scheduleComponent.schedule(component, identity + END_FIXED, ()->{
				future.cancel(true);
				scheduleComponent.cancelFixedSchedule(key);
			}, endTime - now, TimeUnit.MILLISECONDS);
			if(callbackTask != null) {
				scheduleComponent.schedule(component, identity + END_CALLBACK_FIXED
						,callbackTask
						, endTime - now, TimeUnit.MILLISECONDS);
			}
		}
	}
	
	public String getFixedScheduleStatus() {
		return scheduleComponent.getFixedScheduleStatus();
	}
	
	public static void cancelFixedSchedule(String scheduleKey){
		scheduleComponent.cancelFixedSchedule(scheduleKey);
	}
	
}
