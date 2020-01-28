package com.yufeng.redisboomfilter.service;

import com.yufeng.redisboomfilter.config.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * @Auther: daiyu
 * @Date: 28/1/20 00:06
 * @Description:
 */

@Service
public class SecSaleService {


    private static final String SEC_START_FLAG = "skuId_start_";

    private static final String SEC_SALE_ACCESS = "skuId_access_";
    private static final String SEC_SALE_COUNT = "skuId_count_";

    @Autowired
    private RedisService redisService;

    public String secSale(int uid, int skuId) {

        String isStart = (String) redisService.get(SEC_START_FLAG);
        if (!StringUtils.hasText(isStart)) {
            return "Not start yet";
        }

        if (!isStart.contains("_")) {
            return "System error";
        }


        Integer isStartInt = Integer.parseInt(isStart.split("_")[0]);
        Integer isStartTime = Integer.parseInt(isStart.split("_")[1]);
        if (isStartInt == 0) {
            if (isStartTime > (new Date()).getTime() / 1000) {
                return "Not start yet";
            } else {
                redisService.set(SEC_START_FLAG + skuId, 1);
            }
        } else {
            return "System error";
        }

        String skuIdAccessName = SEC_SALE_ACCESS + skuId;
        String skuIdCountName = SEC_SALE_COUNT + skuId;
        Integer accessNum = Integer.parseInt((String)redisService.get(skuIdAccessName));
        Integer countNum = Integer.parseInt((String)redisService.get(skuIdCountName));

        if (countNum * 1.2 < accessNum) {
            return "Sec Sale Finished";
        } else {
            redisService.incr(skuIdAccessName);
        }
        return "";
    }
}
