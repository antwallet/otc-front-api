package cn.com.otc.test;

import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CountryResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.InputStream;
import java.net.InetAddress;

/**
 * @Auther: 2024
 * @Date: 2024/9/21 16:07
 * @Description: 测试根据IP获取国家
 */
@Slf4j
@Component
public class GeoIpTest {
    private DatabaseReader databaseReader;
    public static void main(String[] args) {
        String ip = "2400:c600:3360:b864:1:0:de91:c6b5";
        //getCountry(ip);
    }

    @PostConstruct
    public void init() {
        try (InputStream inputStream = GeoIpTest.class.getClassLoader()
                .getResourceAsStream("GeoLite2-Country.mmdb")) {
            if (inputStream == null) {
                throw new IllegalStateException("Cannot find GeoLite2-Country.mmdb");
            }
            databaseReader = new DatabaseReader.Builder(inputStream)
                    .withCache(new CHMCache())
                    .build();
        } catch (Exception e) {
            log.error("Failed to initialize GeoIP database", e);
            throw new RuntimeException("Failed to initialize GeoIP database", e);
        }
    }

    @PreDestroy
    public void destroy() {
        if (databaseReader != null) {
            try {
                databaseReader.close();
            } catch (Exception e) {
                log.error("Error closing DatabaseReader", e);
            }
        }
    }

    public  String getCountry(String ip) {
        if (StringUtils.isBlank(ip) || "127.0.0.1".equals(ip)) {
            return StringUtils.EMPTY;
        }

        try {
            CountryResponse response = databaseReader.country(InetAddress.getByName(ip));
            return response.getCountry().getNames().getOrDefault("zh-CN", StringUtils.EMPTY);
        } catch (Exception e) {
            log.error("Failed to get country for IP: {}", ip);
            return StringUtils.EMPTY;
        }
    }
}
