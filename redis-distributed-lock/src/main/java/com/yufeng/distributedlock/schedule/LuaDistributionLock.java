package com.yufeng.distributedlock.schedule;

import com.yufeng.distributedlock.config.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@Slf4j
@Service
public class LuaDistributionLock {

    @Autowired
    private RedisService redisService;

    @Autowired
    private RedisTemplate redisTemplate;

    private static String LOCK_PREFIX = "lua_";

    private DefaultRedisScript<Boolean> lockScript;


    @Scheduled(cron = "0/10 * * * * *")
    public void lockJob() {

        String lock = LOCK_PREFIX + this.getClass().getCanonicalName();
        boolean luaRet = false;
        try {

            luaRet = luaExpress(lock, getHostIp());


            if (!luaRet) {
                String value = (String)redisService.genValue(lock);
                log.info("get lock fail,lock belong to:{}", value);
                return;
            }

            log.info("start lua lock lockNxExJob success");
            Thread.sleep(5000);

        } catch (Exception e) {
            log.error("lock lua error",e);
        } finally {
            if (luaRet) {
                log.info("release lua lock success");
                unlock(lock, getHostIp());
            }
        }
    }

    public Boolean luaExpress(String key, String value) {

        lockScript = new DefaultRedisScript<>();
        lockScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("add.lua")));
        lockScript.setResultType(Boolean.class);

        List<Object> keyList = new ArrayList<>();
        keyList.add(key);
        keyList.add(value);
        Boolean result = (Boolean)redisTemplate.execute(lockScript, keyList);
        return result;
    }

    public Boolean unlock(String key, String value) {

        lockScript = new DefaultRedisScript<>();
        lockScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("unlock.lua")));
        lockScript.setResultType(Boolean.class);

        List<Object> keyList = new ArrayList<>();
        keyList.add(key);
        keyList.add(value);
        Boolean result = (Boolean)redisTemplate.execute(lockScript, keyList);
        return result;
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
