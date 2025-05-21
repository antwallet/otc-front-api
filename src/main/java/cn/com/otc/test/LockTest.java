package cn.com.otc.test;

import cn.com.otc.common.enums.ResultCodeEnum;
import cn.com.otc.common.exception.RRException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Auther: 2024
 * @Date: 2024/8/16 20:38
 * @Description: 加锁测试
 */
public class LockTest {

    private static Lock lock =new ReentrantLock();

    public static void main(String[] args) {
        String key = "tg_open_group_redpacket_lock_";
        try {
            //并发抢红包加锁 操作很类似Java的ReentrantLock机制
            lock.lock();
            // 让当前线程睡眠 10 秒
             aa();

        } catch (Exception e) {
            if (e instanceof RRException) {
                RRException rRException = (RRException) e;
                throw rRException;
            } else {
                throw new RRException(String.format("RedPacketManageService.openGroupRedPacket"), ResultCodeEnum.SYSTEM_ERROR_500.code);
            }
        } finally {
            System.out.println(111);
            lock.unlock();
        }
    }
     static String aa() throws InterruptedException {
         long l = System.currentTimeMillis();
         System.out.println(l);
         TimeUnit.SECONDS.sleep(5);
         long end = System.currentTimeMillis();
         System.out.println(end);
         System.out.println(l-end);
        return null;
     }

}
