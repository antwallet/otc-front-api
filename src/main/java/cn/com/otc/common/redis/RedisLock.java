package cn.com.otc.common.redis;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2023/3/15
 */
@Slf4j
public class RedisLock {

  /**
   * 默认等待时长 1 毫秒
   */
  private static final long DEF_WAIT_TIME = 1L;

  /**
   * 默认过期时长 10 秒
   */
  private static final long DEF_EXPIRE_TIME = 1000 * 10L;

  /**
   * 默认重试次数
   */
  private static final Integer NO_TRY_COUNT = 0;
  /**
   * 默认重试休眠时长
   */
  private static final Long NO_TRY_SLEEP_TIME = 0L;

  private RedisLock() {
  }

  /**
   * 获取分布式锁
   *
   * @param redissonClient
   * @param lockKey        锁标识
   * @return
   */
  public static RLock getLock(RedissonClient redissonClient, String lockKey) {
    return redissonClient.getLock(lockKey);
  }

  /**
   * 尝试加锁
   *
   * @param lock 锁
   * @return
   */
  public static boolean lock(RLock lock) {
    return lock(lock, null);
  }

  /**
   * 尝试加锁
   *
   * @param lock       锁
   * @param conditions 附加条件
   * @return
   */
  public static boolean lock(RLock lock, Supplier<Boolean> conditions) {
    return lock(lock, NO_TRY_COUNT, NO_TRY_SLEEP_TIME, conditions);
  }

  /**
   * 尝试加锁
   *
   * @param lock       锁
   * @param tryCount   重试次数
   * @param sleepTime  重试休眠时长（毫秒）
   * @param conditions 附加条件
   * @return
   */
  public static boolean lock(RLock lock, int tryCount, long sleepTime, Supplier<Boolean> conditions) {
    return lock(lock, DEF_WAIT_TIME, DEF_EXPIRE_TIME, tryCount, sleepTime, conditions);
  }

  /**
   * 尝试加锁
   *
   * @param lock       锁
   * @param waitTime   获取锁等待时长（毫秒）
   * @param expireTime 获取锁后自动过期时长（毫秒）
   * @param tryCount   重试次数
   * @param sleepTime  重试休眠时长（毫秒）
   * @param conditions 附加条件
   * @return
   */
  public static boolean lock(RLock lock, long waitTime, long expireTime, int tryCount,
      long sleepTime, Supplier<Boolean> conditions) {
    boolean result = false;
    try {
      boolean unlock = lock.tryLock(waitTime, expireTime, TimeUnit.MILLISECONDS);
      log.info("======> " + Thread.currentThread() + "尝试{}获取锁{} -> {}", tryCount, lock.getName(), unlock);
      if (unlock) {
        if (null != conditions) {
          result = conditions.get();
        } else {
          result = true;
        }
      } else {
        while (tryCount > 0) {
          Thread.sleep(sleepTime);
          --tryCount;
          boolean res = lock(lock, waitTime, expireTime, tryCount, sleepTime, conditions);
          if (res) {
            result = true;
            break;
          }
        }
      }
    } catch (InterruptedException e) {
      log.error("RedisLock 获取分布式锁异常 -> {}", e);
      Thread.currentThread().interrupt();
    }
    return result;
  }

  /**
   * 释放锁
   *
   * @param lock
   */
  public static void unlock(RLock lock) {
    if (null != lock) {
      log.info("<====== " + Thread.currentThread() + "释放锁{}", lock.getName());
      lock.unlock();
    }
  }

}