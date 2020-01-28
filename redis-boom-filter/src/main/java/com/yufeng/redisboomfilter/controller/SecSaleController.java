package com.yufeng.redisboomfilter.controller;

import com.yufeng.redisboomfilter.service.SecSaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Auther: daiyu
 * @Date: 28/1/20 00:05
 * @Description:
 */

@RestController
public class SecSaleController {


    @Autowired
    private SecSaleService secSaleService;

    @RequestMapping("/redis/secsale")
    public String secSale(int uid, int skuId) {

       return secSaleService.secSale(uid, skuId);
    }
}
