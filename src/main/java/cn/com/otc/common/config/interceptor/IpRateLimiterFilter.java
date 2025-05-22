package cn.com.otc.common.config.interceptor;

import cn.com.otc.common.utils.CheckTokenUtil;
import cn.com.otc.modular.auth.entity.result.UserInfoResult;
import cn.com.otc.modular.sys.bean.pojo.UserIpLimit;
import cn.com.otc.modular.sys.manage.RateLimiterService;
import cn.com.otc.modular.sys.service.UserIpLimitService;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component("customIpRateLimiterFilter")
@Slf4j
public class IpRateLimiterFilter extends OncePerRequestFilter {
    @Resource
    private CheckTokenUtil checkTokenUtil;
    private final Object blacklistLock = new Object();
    private static final int STATUS_TOO_MANY_REQUESTS = 429;
    private final RateLimiterService rateLimiterService;
    private final StringRedisTemplate redisTemplate;
    //private final ConcurrentHashMap<String, CacheEntry> ipCache;
    //private final ScheduledExecutorService scheduler;
    private static final Set<String> ipWhitelist;
    @Resource
    private UserIpLimitService userIpLimitService;
    static {
        Set<String> tempSet = new HashSet<>();
        tempSet.add("127.0.0.1");       // localhost
        tempSet.add("47.237.23.152");   // 测试环境
        tempSet.add("47.236.253.213");  // 线上环境
        tempSet.add("47.237.86.16");    // 监控环境
        tempSet.add("192.168.5.204");    // 大猫本地环境
        tempSet.add("188.253.121.12");   //公司公网ip
        tempSet.add("13.214.89.233");    // 小B端：TONJOY
        ipWhitelist = Collections.unmodifiableSet(tempSet);
        log.info("白名单：{}", JSONUtil.toJsonStr(ipWhitelist));
    }

    public IpRateLimiterFilter(RateLimiterService rateLimiterService, StringRedisTemplate redisTemplate) {
        this.rateLimiterService = rateLimiterService;
        this.redisTemplate = redisTemplate;
        //this.ipCache = new ConcurrentHashMap<>();
        //this.scheduler = Executors.newSingleThreadScheduledExecutor();
        //this.scheduler.scheduleAtFixedRate(this::cleanupCache, 1, 1, TimeUnit.HOURS);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        String ip = getClientIp(request);

        String key = "rate_limit:" + ip + ":" + uri;
        log.info("Checking rate limit for IP: {}, URI: {}", ip, uri);

        if (ipWhitelist.contains(ip)) {
            filterChain.doFilter(request, response);
            return;
        }

        Map<String, Object> responseBody;
        if (isIpBlacklisted(ip)) {
            responseBody = new HashMap<>();
            responseBody.put("code", STATUS_TOO_MANY_REQUESTS);
            responseBody.put("msg", "Network error. Please try again later!");
            response.setStatus(STATUS_TOO_MANY_REQUESTS);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(JSONUtil.toJsonStr(responseBody));
            return;
        }

        if (rateLimiterService.isAllowed(key, 10, 1)) {
            filterChain.doFilter(request, response);
        } else {
            synchronized (blacklistLock) {
                if (!isIpBlacklisted(ip)) {
                    addToBlacklistAsync(ip, request, uri);
                }
            }

            responseBody = new HashMap<>();
            responseBody.put("code", STATUS_TOO_MANY_REQUESTS);
            responseBody.put("msg", "Network error. Please try again later!");
            response.setStatus(STATUS_TOO_MANY_REQUESTS);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(JSONUtil.toJsonStr(responseBody));
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // 如果是多个IP，取第一个非unknown的IP
                String[] ips = ip.split(",");
                log.info("过滤前的IP: {}", ip);
                for (String s : ips) {
                    if (!"unknown".equalsIgnoreCase(s.trim())) {
                        return s.trim();
                    }
                }
            }
        }

        return request.getRemoteAddr();
    }

    private String extractIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip.split(",")[0].trim();
    }

    private boolean isIpBlacklisted(String ip) {
        Boolean isBlacklisted = redisTemplate.hasKey("blacklist:" + ip);
        return Boolean.TRUE.equals(isBlacklisted);
    }

    private void addToBlacklistAsync(String ip, HttpServletRequest httpRequest, String uri) {
        if (!uri.equals("/api/front/auth/doLogin")) {
            String token = checkTokenUtil.getRequestToken(httpRequest);
            UserInfoResult userInfoResult = checkTokenUtil.getUserInfoByToken(token);
            String userTGID = userInfoResult.getUserTGID();
            String key = "blacklist:" + ip;
            Map<String, String> blacklistInfo = new HashMap<>();
            blacklistInfo.put("ip", ip);
            blacklistInfo.put("tgId", userTGID);
            blacklistInfo.put("uri", uri);

            // 格式化当前时间为年月日时分秒
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTimestamp = now.format(formatter);
            blacklistInfo.put("timestamp", formattedTimestamp);

            redisTemplate.opsForHash().putAll(key, blacklistInfo);
            redisTemplate.expire(key, 24, TimeUnit.HOURS);

            UserIpLimit userIpLimit = new UserIpLimit();
            userIpLimit.setIp(ip);
            userIpLimit.setTgId(userTGID);
            userIpLimit.setUrl(uri);
            userIpLimit.setTimestamp(formattedTimestamp);
            userIpLimitService.saveUserIpLimit(userIpLimit);
            log.info("IP {} and tgId {} have been added to the blacklist at {}", ip, userTGID, formattedTimestamp);
        }
    }

}
