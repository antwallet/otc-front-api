package cn.com.otc.test;

import cn.com.otc.common.utils.TimeUtil;
import cn.com.otc.modular.sys.service.TLoginRecordService;
import cn.com.otc.modular.sys.service.TUserService;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2022/6/22
 */
@Slf4j
@RestController
@RequestMapping("/api/front/aa")
public class Test111 {
    private static final RateLimiter rateLimiter = RateLimiter.create(5); // 每秒不超过5个请求

    public static String doSomething() {
        //尝试获取令牌
        if(rateLimiter.tryAcquire()){
            //模拟业务执行500毫秒
          /*  try {
                Thread.sleep(500);
            }catch (InterruptedException e){
                e.printStackTrace();
            }*/
            return "aceess success [" + TimeUtil.sdf3.get().format(new Date()) + "]";
        }else{
            return "aceess limit [" + TimeUtil.sdf3.get().format(new Date()) + "]";
        }
    }

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 2; i++) {
            System.out.println(doSomething());
        }
//        SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        System.out.println(sdf.format(new Date(1655893445277l)));
          //List<Double> moneys = doubleMeanMethod(100.00, 5);
//        for (int i=0;i<2;i++) {
//            String accountId = "account_".concat(UidGeneratorUtil.genId());//根据雪花算法获取账户id
//            System.out.println(accountId);
//        }
        /*String password = "920605";
        String encryptPassward = EncryptUtil.encryptByBCrypt(password);
        System.out.println(encryptPassward);
        System.out.println(EncryptUtil.checkPswByBCrypt(password,encryptPassward));*/
        /*Map<String,String> map = new HashMap<>();
        map.put("111###0","aaa");
        map.put("222###1","aaa");
        map.put("333###1","aaa");
        map.put("444###1","aaa");
        // 分组相同的 value
        Map<String, List<String>> groupedByValue = map.entrySet().stream()
            .collect(Collectors.groupingBy( Map.Entry::getValue,
                Collectors.mapping(Map.Entry::getKey, Collectors.toList())
        ));
        System.out.println(groupedByValue);*/
    }

    public static List<Double> getRandomMoney(double remainMoney, int remainSize) {
        List<Double> moneys = new ArrayList<>();
        if (remainSize == 1) {
            double money = (double) Math.round(remainMoney * 100) / 100;
            moneys.add(money);
            return moneys;
        }
        Random random = new Random();
        double luck = random.nextGaussian();
        double mean = remainMoney / remainSize;
        double money = mean + luck * mean * 0.3;
        money = Math.floor(money * 100) / 100;
        money = money <= 0.01 ? 0.01 : money;
        return moneys;
    }

    public void getRandomRedPacket(){
        Scanner sc = new Scanner(System.in);
        System.out.println("请输入要发送的红包金额：");
        double money = sc.nextDouble();
        System.out.println("请输入抢红包的人数：");
        int nums = sc.nextInt();
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.CHINA);
        if (money <= 0 || nums <= 0) {
            System.out.println("输入 有误！！！");
        } else {
            if (nums == 1) {
                System.out.println("抢到的红包为 ：" + nf.format(money));
            } else {
                if ((money / nums) > 0.01) {
                    double temp=0;
                    for (int i = 0; i < nums-1; i++) {
                        temp = Math.random()*(money-(0.01*(nums-i+2))) + 0.01;
                        money=money-temp;
                        System.out.println("第" + (i+1) + "个人抢到："+nf.format(temp));
                    }
                    System.out.println("第"+nums+"个人抢到："+nf.format(money));
                } else if ((money / nums) == 0.01) {
                    for (int i = 0; i < nums; i++) {
                        System.out.println("第" + (i + 1) + "个人抢到：￥" + 0.01);
                    }
                } else {
                    System.out.println("金额输入有误，请重新输入！");
                }
            }
        }
    }


    public static List<Double> doubleMeanMethod(double money,int number){
        List<Double> result = new ArrayList<Double>();
        if(money<0&&number<1)
            return null;
        double amount,sum=0;
        int remainingNumber=number;
        int i=1;
        while(remainingNumber>1){
            amount= nextDouble(0.01,2*(money/remainingNumber));
            sum+=amount;
            System.out.println("第"+i+"个人领取的红包金额为："+format(amount));
            money -= amount;
            remainingNumber--;
            result.add(amount);
            i++;
        }
        result.add(money);
        System.out.println("第"+i+"个人领取的红包金额为："+format(money));
        sum+=money;
        System.out.println("验证发出的红包总金额为："+format(sum));
        return result;
    }

    /**
     * 生成min到max范围的浮点数
     **/
    public static double nextDouble(final double min, final double max) {
        return min + ((max - min) * new Random().nextDouble());
    }

    public static String format(double value) {

        return new java.text.DecimalFormat("0.00").format(value); // 保留两位小数
    }

    @Resource
    private DataSource dataSource;

    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private PlatformTransactionManager txManager;

    @Resource
    private TUserService userMapper;

    @Resource
    private PlatformTransactionManager transactionManager;
    public void doUpdate() {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            // 先用 SELECT FOR UPDATE 加锁
            jdbcTemplate.execute("SELECT * FROM t_user WHERE TG_id = '7044326915' FOR UPDATE");

            // 再执行更新
            jdbcTemplate.execute("UPDATE t_user SET update_time = now() WHERE TG_id = '7044326915'");

            log.info("线程[{}]获得锁，开始睡眠", Thread.currentThread().getName());
            Thread.sleep(30000);
            log.info("线程[{}]完成", Thread.currentThread().getName());

            transactionManager.commit(status);
        } catch (Exception e) {
            transactionManager.rollback(status);
            log.error("线程[{}]发生异常: {}", Thread.currentThread().getName(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void startTest() {
        // 第一个线程
        new Thread(() -> {
            doUpdate();
        }, "Thread-1").start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 第二个线程
        new Thread(() -> {
            doUpdate();
        }, "Thread-2").start();
    }




    @RequestMapping("/qq")
    public void qq() throws SQLException, InterruptedException {
        //startTest();

        generateTestUsers(20000);


    }

    @Resource
    private TUserService tUserService;

    @Resource
    private TLoginRecordService tLoginRecordService;

    public void generateTestUsers(int count) {
        Random random = new Random();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String[] languages = {"en", "zh", "ja", "ru", "es"};
        String[] resolutions = {"1920x1080", "2560x1440", "1366x768", "3840x2160"};
        String[] networkTypes = {"4G", "5G", "WiFi"};
        String[] networkStatus = {"excellent", "good", "fair", "poor"};
        String[] deviceModels = {"iPhone 13", "Samsung S21", "Xiaomi 12", "Huawei P40", "OnePlus 9"};

        for (int i = 1; i <= count; i++) {
            try {
                // 构造用户基本信息
                String userId = String.format("USER%06d", i);
                String tgId = String.format("TG%08d", i);
                String name = "User_" + RandomStringUtils.randomAlphanumeric(5);
                String nick = "Nick_" + RandomStringUtils.randomAlphanumeric(3);
                String avatar = "https://example.com/avatar/" + userId + ".jpg";
                String password = DigestUtils.md5Hex(userId + "pwd");  // 示例密码生成

                // 构造设备和网络信息
                String remoteIP = generateRandomIP();
                Boolean isPremium = random.nextBoolean();
                String deviceModel = deviceModels[random.nextInt(deviceModels.length)];

                // 生成随机日期（过去一年内）
                LocalDate registrationLocalDate = LocalDate.now().minusDays(random.nextInt(365));
                String registrationDate = registrationLocalDate.format(dateFormatter);

                // 生成随机评分 (1-5)
                BigDecimal userScore = BigDecimal.valueOf(1 + random.nextDouble() * 4).setScale(1, RoundingMode.HALF_UP);

                // 其他随机信息
                String lang = languages[random.nextInt(languages.length)];
                String resolution = resolutions[random.nextInt(resolutions.length)];
                String netStatus = networkStatus[random.nextInt(networkStatus.length)];
                String netType = networkTypes[random.nextInt(networkTypes.length)];

                // 调用注册方法
                tUserService.registerTUser(
                        userId, tgId, name, nick, avatar, password,
                        remoteIP, isPremium, deviceModel, registrationDate,
                        userScore, lang, resolution, netStatus, netType
                );

                // 生成登录记录，使用空字符串作为loginChannel
                tLoginRecordService.saveLoginRecord(userId, "");

                if (i % 100 == 0) {
                    log.info("Generated {} users", i);
                }

                // 适当的延迟，避免系统压力过大
                if (i % 1000 == 0) {
                    Thread.sleep(1000);
                }

            } catch (Exception e) {
                log.error("Error generating user {}: {}", i, e.getMessage());
            }
        }
    }
    private String generateRandomIP() {
        Random r = new Random();
        return r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256);
    }



}
