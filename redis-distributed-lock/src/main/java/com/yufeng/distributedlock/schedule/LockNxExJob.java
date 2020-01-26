package com.yufeng.distributedlock.schedule;

import com.yufeng.distributedlock.config.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;


@Slf4j
@Service
public class LockNxExJob {

    @Autowired
    private RedisService redisService;

    @Autowired
    private RedisTemplate redisTemplate;

    private static String LOCK_PREFIX = "prefix";

    private static int LOCK_EXPIRE = 3600;


    @Scheduled(cron = "0/10 * * * * *")
    public void lockJob() {

        String lock = LOCK_PREFIX + this.getClass().getCanonicalName();
        boolean nxRet = false;
        try {

            nxRet = redisTemplate.opsForValue().setIfAbsent(lock, getHostIp());
            Object lockValue = redisService.get(lock);

            if (!nxRet) {
                String value = (String)lockValue;
                log.info("get lock fail,lock belong to:{}", value);
                return;
            }

            redisTemplate.opsForValue().set(lock, getHostIp(), LOCK_EXPIRE);
            log.info("start lock lockNxExJob success");
            Thread.sleep(5000);

        } catch (Exception e) {
            log.error("lock error",e);
        } finally {
            if (nxRet) {
                log.info("release lock success");
                redisService.remove(lock);
            }
        }
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
