package com.yufeng.redisboomfilter.controller;

import com.yufeng.redisboomfilter.config.RedisService;
import com.yufeng.redisboomfilter.service.BloomFilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Auther: daiyu
 * @Date: 27/1/20 15:36
 * @Description:
 */

@RestController
public class BloomFliterController {


    @Autowired
    private BloomFilterService bloomFilterService;

    @Autowired
    private RedisService redisService;


    @RequestMapping("/bloom/idExists")
    public boolean ifExists(int id) {

        return bloomFilterService.userIdExists(id);
    }

    @RequestMapping("/bloom/redisIdAdd")
    public boolean redisAdd(int id) {

        return redisService.bloomFilterAdd(id);
    }

    @RequestMapping("/bloom/redisIdExist")
    public boolean redisExist(int id) {

        return redisService.bloomFilterExist(id);
    }

}
