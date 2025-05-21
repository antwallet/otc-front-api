package cn.com.otc.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalUnit;
import java.util.Calendar;
import java.util.Date;

/**
 * @description:时间工具类
 * @author: zhangliyan
 * @time: 2024/2/3
 */
public class TimeUtil {

  public static final ThreadLocal<SimpleDateFormat> sdf3 = ThreadLocal.withInitial(()->new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

  public static long getFristMonthDay(long dateMillis){
    Calendar calendar=Calendar.getInstance();
    calendar.setTime(new Date(dateMillis));
    calendar.set(Calendar.DAY_OF_MONTH,calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
    return calendar.getTimeInMillis();
  }

  public static long getLastMonthDay(){
    Calendar c2 = Calendar.getInstance();
    c2.set(Calendar.DAY_OF_MONTH, c2.getActualMaximum(Calendar.DAY_OF_MONTH)); //获取当前月最后一天
    c2.set(Calendar.HOUR_OF_DAY, 23); //将小时至23
    c2.set(Calendar.MINUTE, 59); //将分钟至59
    c2.set(Calendar.SECOND,59); //将秒至59
    c2.set(Calendar.MILLISECOND, 999); //将毫秒至999
    return c2.getTimeInMillis();
  }

  /**
   * 当天凌晨
   * @param dateMillis
   * @return
   */
  public static long getMorning(long dateMillis) {
    Calendar c = Calendar.getInstance();
    c.setTimeInMillis(dateMillis);
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);
    return c.getTimeInMillis();
  }

  /**@param dateMillis
   * @param diffDay
   * 得到当天偏移diffDay天的00:00:00的时间戳
   * @return
   */
  public static long getDayZeroTime(long dateMillis,int diffDay) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(dateMillis);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    calendar.add(Calendar.DAY_OF_MONTH, diffDay);
    return calendar.getTimeInMillis();
  }

  /**
   * 获取当前日期
   */
  public static String getNowDate() {
    return new SimpleDateFormat("yyyyMMdd").format(System.currentTimeMillis());
  }

  public static long getAfterDate(long timestamp,int time,TemporalUnit unit){
    Instant instant = Instant.ofEpochMilli(timestamp);
    Instant newInstant = instant.plus(time, unit);
    return newInstant.toEpochMilli();
  }

  public static long getNextMonthFirstDay(long dateMillis){
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(dateMillis);
    calendar.add(Calendar.MONTH, 1); // 将月份加1，即下个月
    calendar.set(Calendar.DAY_OF_MONTH, 1); // 将日期设置为1，即下个月的第一天
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTimeInMillis();
  }

  /**
   * 获取日期分钟偏移
   */
  public static String getDateAfterMin(String date, int minute){
    try {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(sdf3.get().parse(date));
      calendar.add(Calendar.MINUTE, minute);
      return sdf3.get().format(calendar.getTime());
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static LocalDateTime getDateTimeOfTimestamp(long timestamp) {
    Instant instant = Instant.ofEpochMilli(timestamp);
    ZoneId zone = ZoneId.systemDefault();
    return LocalDateTime.ofInstant(instant, zone);
  }

  public static void main(String[] args) throws ParseException {
     /*long lastMonthDay = getLastMonthDay();
     System.out.println(lastMonthDay);
     SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     System.out.println(sdf.format(new Date(lastMonthDay)));

     long dayZeroTime = getDayZeroTime(System.currentTimeMillis(),1);
     System.out.println(dayZeroTime);
     System.out.println(sdf.format(new Date(dayZeroTime)));

     long fristMonthDay =getFristMonthDay(getMorning(System.currentTimeMillis()));
     System.out.println(sdf.format(new Date(fristMonthDay)));
     long nextMonthFirstDay = getNextMonthFirstDay(1679646271857l);
     System.out.println(sdf3.get().format(new Date(nextMonthFirstDay)));*/

    /*System.out.println(TimeUtil.getMorning(System.currentTimeMillis()));//1680192000000
    System.out.println(TimeUtil.getDayZeroTime(System.currentTimeMillis(),1));
    System.out.println(TimeUtil.sdf3.get().format(new Date(TimeUtil.getFristMonthDay(System.currentTimeMillis()))));
    System.out.println(TimeUtil.sdf3.get().format(new Date(TimeUtil.getNextMonthFirstDay(System.currentTimeMillis()))));*/
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    long dayZeroTime = getDayZeroTime(System.currentTimeMillis(),1);
    System.out.println(dayZeroTime);
    System.out.println(sdf.format(new Date(dayZeroTime)));
  }

}
