package cn.com.otc;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement    //开启事务管理
@EnableAsync(proxyTargetClass = true)    //异步任务管理器
@EnableScheduling
@EnableEncryptableProperties
@MapperScan(value = "cn.com.otc.**.dao")
public class OtcFrontApiApplication {

    public static void main(String[] args) {

//        try {
            SpringApplication.run(OtcFrontApiApplication.class, args);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        System.out.println(
                "(^_−)☆项目启动成功(^_−)☆ \n" +
                        "|----------------------------|\n" +
                        "|      -             -       |\n" +
                        "|   .     .       .     .    |\n" +
                        "| .         .   .          . |\n" +
                        "|                            |\n" +
                        "|                            |\n" +
                        "|   .                    .   |\n" +
                        "|     .                .     |\n" +
                        "|       .            .       |\n" +
                        "|          .       .         |\n" +
                        "|             ---            |\n" +
                        "|----------------------------|\n"
        );
    }

}
