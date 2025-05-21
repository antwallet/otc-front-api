package cn.com.otc.common.utils;

import cn.com.otc.modular.sys.bean.pojo.TUser;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SharedCache {
    private static SharedCache instance;
    private final Map<String, TUser> cache = new ConcurrentHashMap<>();

    private SharedCache() {}

    public static synchronized SharedCache getInstance() {
        if (instance == null) {
            instance = new SharedCache();
        }
        return instance;
    }

    public void put(String key, TUser value) {
        cache.put(key, value);
    }

    public TUser get(String key) {
        return cache.get(key);
    }
}
