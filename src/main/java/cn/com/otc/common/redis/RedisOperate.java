package cn.com.otc.common.redis;

import cn.com.otc.common.constants.CommonConstant;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @description:redis操作类
 */
@Slf4j
@Component
public class RedisOperate {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 创建个人红包key
     *
     * @param redpacketId 红包id
     * @return
     */
    public String buildSingleRedpacketRedisKey(String redpacketId) {
        StringBuilder builder = new StringBuilder();
        builder.append(CommonConstant.SINGLE_REDPACKET_REDIS_KEY)
                .append(":").append(redpacketId);
        return builder.toString();
    }

    /**
     * 根据key获取个人红包redis
     *
     * @param redpacketId
     * @return
     */
    public String getSingleRedpacketToRedis(String redpacketId) {
        //Redis通用的操作组件
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object obj = valueOperations.get(buildSingleRedpacketRedisKey(redpacketId));
        if (obj == null) {
            return "";
        }
        return obj.toString();
    }

    /**
     * 添加个人红包到redis
     *
     * @param redpacketId
     * @param sendTGId
     */
    @Async
    public void addSingleRedpacketToRedis(String redpacketId, String sendTGId) {
        log.info("开始保存个人红包信息到redis,redpacketId={},sendTGId={}", redpacketId, sendTGId);
        //Redis通用的操作组件
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set(buildSingleRedpacketRedisKey(redpacketId), sendTGId, 24, TimeUnit.HOURS);
        log.info("结束保存个人红包信息到redis,redpacketId={},sendTGId={}", redpacketId, sendTGId);
    }

    /**
     * 从redis删除个人红包
     *
     * @param redpacketId
     */
    @Async
    public void removeSingleRedpacketToRedis(String redpacketId) {
        log.info("开始删除个人红包,redpacketId={}", redpacketId);
        redisTemplate.delete(buildSingleRedpacketRedisKey(redpacketId));
        log.info("结束删除个人红包,redpacketId={}", redpacketId);
    }


    /**
     * 创建群发红包key
     *
     * @param redpacketId 红包id
     * @return
     */
    public String buildGroupRedpacketRedisKey(String redpacketId) {
        StringBuilder builder = new StringBuilder();
        builder.append(CommonConstant.GROUP_REDPACKET_REDIS_KEY)
                .append(":").append(redpacketId);
        return builder.toString();
    }

    /**
     * 根据key获取个人红包redis
     *
     * @param redpacketId
     * @return
     */
    public String getGroupRedpacketToRedis(String redpacketId) {
        //Redis通用的操作组件
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object obj = valueOperations.get(buildGroupRedpacketRedisKey(redpacketId));
        if (obj == null) {
            return "";
        }
        return obj.toString();
    }

    /**
     * 添加群发红包到redis
     *
     * @param redpacketId param redpacketNum
     */
    @Async
    public void addGroupRedpacketToRedis(String redpacketId, Integer redpacketNum) {
        log.info("开始保存群发红包信息到redis,redpacketId={},redpacketNum={}", redpacketId, redpacketNum);
        //Redis通用的操作组件
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set(buildGroupRedpacketRedisKey(redpacketId), redpacketNum, 24, TimeUnit.HOURS);
        log.info("结束保存群发红包信息到redis,redpacketId={},redpacketNum={}", redpacketId, redpacketNum);
    }

    /**
     * 添加分享红包到redis
     *
     * @param redpacketId param redpacketNum
     */
    @Async
    public void addShareRedpacketToRedis(String redpacketId, Integer redpacketNum,long expirationTimeInMillis) {
        log.info("开始保存分享红包信息到redis,redpacketId={},redpacketNum={},exp={}", redpacketId, redpacketNum,expirationTimeInMillis);
        //Redis通用的操作组件
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set(buildGroupRedpacketRedisKey(redpacketId), redpacketNum, expirationTimeInMillis, TimeUnit.MILLISECONDS);
        log.info("结束保存分享红包信息到redis,redpacketId={},redpacketNum={},exp={}", redpacketId, redpacketNum,expirationTimeInMillis);
    }

    @Async
    public void deductGroupRedpacketToRedis(String redpacketId) {
        log.info("开始减少抢的群发红包库存到redis,redpacketId={}", redpacketId);
        //Redis通用的操作组件
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.increment(buildGroupRedpacketRedisKey(redpacketId), -1);
        log.info("结束减少抢的群发红包库存到redis,redpacketId={}", redpacketId);
    }

    /**
     * 从redis删除群发红包
     *
     * @param redpacketId
     */
    @Async
    public void removeGroupRedpacketToRedis(String redpacketId) {
        redisTemplate.delete(buildGroupRedpacketRedisKey(redpacketId));
    }

    /**
     * 创建群发红包key
     *
     * @param redpacketId 红包id
     * @return
     */
    public String buildLuckyGroupRedpacketRedisKey(String redpacketId) {
        StringBuilder builder = new StringBuilder();
        builder.append(CommonConstant.LUCKY_GROUP_REDPACKET_REDIS_KEY)
                .append(":").append(redpacketId);
        return builder.toString();
    }

    /**
     * 获取拼手气群发红包信息到redis
     *
     * @param redpacketId
     * @return 对应的多个键值
     */
    public String getLuckyGroupRedpacketToRedis(String redpacketId, Integer sort) {
        Map<Object, Object> map = redisTemplate.opsForHash().entries(buildLuckyGroupRedpacketRedisKey(redpacketId));
        if (map == null || map.size() == 0) {
            return "";
        }
        if (map.get(sort) == null) {
            return "";
        }
        return map.get(sort).toString();
    }

    /**
     * 保存拼手气群发红包信息到redis
     *
     * @param redpacketId
     * @param map
     */
    public void addLuckyGroupRedpacketToRedis(String redpacketId, Map<Integer, String> map) {
        log.info("开始保存拼手气群发红包信息到redis,redpacketId={},map={}", redpacketId, JSONUtil.toJsonStr(map));
        //Redis通用的操作组件
        String key = buildLuckyGroupRedpacketRedisKey(redpacketId);
        HashOperations hashOperations = redisTemplate.opsForHash();
        hashOperations.putAll(key, map);
        redisTemplate.expire(key, 24, TimeUnit.HOURS); // 设置整个Hash的过期时间
        log.info("结束保存拼手气群发红包信息到redis,redpacketId={},map={}", redpacketId, JSONUtil.toJsonStr(map));
    }

    /**
     * 保存拼手气群发红包信息到redis
     *
     * @param redpacketId
     * @param map
     */
    public void addLuckyShareRedpacketToRedis(String redpacketId, Map<Integer, String> map,long expirationTimeInMillis) {
        log.info("开始保存分享红包信息到redis,redpacketId={},map={}", redpacketId, JSONUtil.toJsonStr(map));
        //Redis通用的操作组件
        String key = buildLuckyGroupRedpacketRedisKey(redpacketId);
        HashOperations hashOperations = redisTemplate.opsForHash();
        hashOperations.putAll(key, map);
        redisTemplate.expire(key, expirationTimeInMillis, TimeUnit.MILLISECONDS); // 设置整个Hash的过期时间
        log.info("结束保存分享红包信息到redis,redpacketId={},map={}", redpacketId, JSONUtil.toJsonStr(map));
    }


    /**
     * 创建充值订单key
     *
     * @param base58CheckAddress 钱包地址
     * @param money
     * @return
     */
    public String buildChargeRedisKey(String base58CheckAddress, Integer accountType, String money) {
        StringBuilder builder = new StringBuilder();
        builder.append(CommonConstant.CHARGE_REDIS_KEY)
                .append(":").append(base58CheckAddress)
                .append(":").append(accountType)
                .append(":").append(money);
        return builder.toString();
    }

    /**
     * 根据key获取充值订单redis
     *
     * @param base58CheckAddress 钱包地址
     * @param accountType
     * @param money
     * @return
     */
    public String getChargeToRedis(String base58CheckAddress, Integer accountType, String money) {
        //Redis通用的操作组件
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object obj = valueOperations.get(buildChargeRedisKey(base58CheckAddress, accountType, money));
        if (obj == null) {
            return "";
        }
        return obj.toString();
    }

    /**
     * 添加充值订单到redis
     *
     * @param base58CheckAddress 钱包地址
     * @param money
     */
    @Async
    public void addChargeToRedis(String base58CheckAddress, Integer accountType, String money) {
        log.info("开始保存充值订单到redis,base58CheckAddress={},accountType={},money={}", base58CheckAddress, accountType, money);
        //Redis通用的操作组件
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set(buildChargeRedisKey(base58CheckAddress, accountType, money), 1, 30, TimeUnit.MINUTES);
        log.info("结束保存充值订单到redis,base58CheckAddress={},accountType={},money={}", base58CheckAddress, accountType, money);
    }

    /**
     * 从redis删除充值订单key
     *
     * @param base58CheckAddress
     * @param money
     */
    @Async
    public void removeChargeToRedis(String base58CheckAddress, Integer accountType, String money) {
        redisTemplate.delete(buildChargeRedisKey(base58CheckAddress, accountType, money));
    }

    /**
     * 创建取消充值订单key
     *
     * @param sendTGId 取消用户
     * @return
     */
    public String buildCancelChargeRedisKey(String sendTGId) {
        StringBuilder builder = new StringBuilder();
        builder.append(CommonConstant.CHARGE_CANCEL_REDIS_KEY)
                .append(":").append(sendTGId);
        return builder.toString();
    }

    /**
     * 根据key获取取消充值订单redis
     *
     * @param sendTGId
     * @return
     */
    public String getCancelChargeToRedis(String sendTGId) {
        //Redis通用的操作组件
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object obj = valueOperations.get(buildCancelChargeRedisKey(sendTGId));
        if (obj == null) {
            return "";
        }
        return obj.toString();
    }

    /**
     * 取消充值订单到redis
     *
     * @param sendTGId
     */
    @Async
    public void addCancelChargeToRedis(String sendTGId, long time) {
        log.info("开始添加取消充值订单记录到redis,sendTGId={}", sendTGId);
        //Redis通用的操作组件
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set(buildCancelChargeRedisKey(sendTGId), 1, time, TimeUnit.MILLISECONDS);
        log.info("结束添加取消充值订单记录到redis,sendTGId={}", sendTGId);
    }

    /**
     * 从redis删除取消充值订单key
     *
     * @param sendTGId
     */
    @Async
    public void removeCancelChargeToRedis(String sendTGId) {
        redisTemplate.delete(buildCancelChargeRedisKey(sendTGId));
    }


    public void setRedis(String key, Object value) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value));
    }

    public void setRedisString(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    public void setRedisWithExpire(String key, Object value, long expirationTimeInMillis) {
        redisTemplate.opsForValue().set(key, value, expirationTimeInMillis, TimeUnit.SECONDS);
    }

     /**
     * 设置有效时间
     *
     * @param key Redis键
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout, final TimeUnit unit)
    {
        return redisTemplate.expire(key, timeout, unit);
    }

    public String getRedis(String key) {
        //Object redisValue = redisTemplate.opsForValue().get(key);
        Object redisValue =stringRedisTemplate.opsForValue().get(key);
        return null != redisValue ? redisValue.toString() : StringUtils.EMPTY;
    }

    public void incrementCount(String key ,long value) {
        redisTemplate.opsForValue().increment(key,value);
    }



    /**
     * 功能描述: list 类型的取值
     */
    public List<Object> getRedisList(String key) {
        //Redis通用的操作组件
        ListOperations<String, Object> valueOperations = redisTemplate.opsForList();
        List<Object>  obj = valueOperations.range(key,0,-1);
        if (obj == null) {
            return new ArrayList<>();
        }
        return obj;
    }

    /**
     *
     * 功能描述: list 类型的存值
     */
    public void setRedisList(String key, List<Object> value, int timeout, TimeUnit unit) {
        // 存储到 Redis 中
        redisTemplate.opsForList().rightPushAll(key, value);

        // 设置过期时间为 24 小时
        redisTemplate.expire("overallRankingResult", timeout, unit);
    }



    /**
     * 添加收款到redis
     *
     * @param tronTransId param redpacketNum
     */
    @Async
    public void addTronTransToRedis(String tronTransId, Integer tronTransNum, int timeout, TimeUnit unit) {
        log.info("开始保存收款信息到redis,tronTransId={},tronTransNum={},timeout={},unit={}", tronTransId, tronTransNum,timeout,unit);
        if (timeout==0){ // 当他不设置时间时，则不设置过期时间
            //Redis通用的操作组件
            ValueOperations valueOperations = redisTemplate.opsForValue();
            valueOperations.set(buildTronTransRedisKey(tronTransId), tronTransNum);
            log.info("结束保存收款信息到redis,tronTransId={},tronTransNum={}", tronTransId, tronTransNum);
            return;
        }
        //Redis通用的操作组件
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set(buildTronTransRedisKey(tronTransId), tronTransNum, timeout, unit);
        log.info("结束保存收款信息到redis,tronTransId={},tronTransNum={}", tronTransId, tronTransNum);
    }


    /**
     * 创建群发红包key
     *
     * @param redpacketId 红包id
     * @return
     */
    public String buildTronTransRedisKey(String redpacketId) {
        StringBuilder builder = new StringBuilder();
        builder.append(CommonConstant.TRON_TRANS_REDIS_KEY)
                .append(":").append(redpacketId);
        return builder.toString();
    }


    /**
     * 根据key获取收款的redis
     *
     * @param tronTransId
     * @return
     */
    public String getronTransToRedis(String tronTransId) {
        //Redis通用的操作组件
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object obj = valueOperations.get(buildTronTransRedisKey(tronTransId));
        if (obj == null) {
            return "";
        }
        return obj.toString();
    }


    @Async
    public void deductTronTransToRedis(String tronTransId) {
        log.info("开始减少需付款库存到redis,redpacketId={}", tronTransId);
        //Redis通用的操作组件
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.increment(buildTronTransRedisKey(tronTransId), -1);
        log.info("结束减少需付款库存到redis,redpacketId={}", tronTransId);
    }



    public void compensateTronTransToRedis(String tronTransId) {

        try {
            // 获取事务开始前的红包数量
            ValueOperations valueOperations = redisTemplate.opsForValue();
            String redpacketLeaveRedis = valueOperations.get(buildTronTransRedisKey(tronTransId)).toString();
            if (redpacketLeaveRedis == null) {
                return ;
            }
            if (StringUtils.isNotBlank(redpacketLeaveRedis)) {
                int redpacketLeave = Integer.parseInt(redpacketLeaveRedis);
                // 恢复红包数量
                redisTemplate.opsForValue().increment(buildTronTransRedisKey(tronTransId), 1);
                log.info("compensateTronTransToRedis 恢复红包数量, tronTransId={}, 恢复后的数量={}", tronTransId, redpacketLeave + 1);
            }
        } catch (Exception e) {
            log.error("compensateTronTransToRedis 恢复红包数量失败, tronTransId={}", tronTransId, e);
        }
    }

    public void addSet(String key, String[] ticketArray) {
        //redisTemplate.opsForSet().add(key, value);
        stringRedisTemplate.opsForSet().add(key, ticketArray);
    }

    public Long getSetSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    public String pop(String key) {
        return String.valueOf(redisTemplate.opsForSet().pop(key));
    }
}
