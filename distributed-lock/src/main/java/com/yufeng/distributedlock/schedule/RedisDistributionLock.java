package com.yufeng.distributedlock.schedule;

import com.yufeng.distributedlock.config.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCommands;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@Slf4j
@Service
public class RedisDistributionLock {

    @Autowired
    private RedisService redisService;

    @Autowired
    private RedisTemplate redisTemplate;

    private static String LOCK_PREFIX = "redis_";

    private DefaultRedisScript<Boolean> lockScript;


    @Scheduled(cron = "0/10 * * * * *")
    public void lockJob() {

        String lock = LOCK_PREFIX + "JedisNxExJob";
        boolean lockRet = false;

        try {
            lockRet = this.setLock(lock, 600);

            if (!lockRet) {
                String value = (String) redisService.genValue(lock);
                log.info("jedisLockJob get lock fail,lock belong to:{}", value);
                return;
            } else {
                log.info("jedisLockJob start  lock lockNxExJob success");
                Thread.sleep(5000);
            }
        } catch (Exception e) {
            log.error("jedisLockJob lock error", e);

        } finally {
            if (lockRet) {
                log.info("jedisLockJob release lock success");
                redisService.remove(lock);
            }
        }
    }

    public Boolean setLock(String key, long expire) {

        try {
            Boolean result = (Boolean) redisTemplate.execute(new RedisCallback<Boolean>() {
                @Override
                public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                    return connection.set(key.getBytes(), getHostIp().getBytes(), Expiration.seconds(expire), RedisStringCommands.SetOption.SET_IF_ABSENT);
                }
            });

            return result;
        } catch (Exception e) {
            log.error("error get lock");
        }
        return false;
    }


    private static String getHostIp(){
        try{
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()){
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()){
                    InetAddress ip = (InetAddress) addresses.nextElement();
                    if (ip != null
                        && ip instanceof Inet4Address
                        && !ip.isLoopbackAddress() //loopback地址即本机地址，IPv4的loopback范围是127.0.0.0 ~ 127.255.255.255
                        && ip.getHostAddress().indexOf(":")==-1){
                        return ip.getHostAddress();
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
