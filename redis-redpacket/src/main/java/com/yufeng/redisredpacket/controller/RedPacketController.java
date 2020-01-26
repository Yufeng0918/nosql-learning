package com.yufeng.redisredpacket.controller;



import com.yufeng.redisredpacket.domain.RedPacketInfo;
import com.yufeng.redisredpacket.domain.RedPacketRecord;
import com.yufeng.redisredpacket.mapper.RedPacketInfoMapper;
import com.yufeng.redisredpacket.mapper.RedPacketRecordMapper;
import com.yufeng.redisredpacket.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

/**
 * @Auther: daiyu
 * @Date: 26/1/20 16:39
 * @Description:
 */
@Controller
public class RedPacketController {

    @Autowired
    private RedisService redisService;

    @Autowired
    private RedPacketInfoMapper redPacketInfoMapper;

    @Autowired
    private RedPacketRecordMapper redPacketRecordMapper;


    @ResponseBody
    @RequestMapping("/addPacket")
    public String saveRedPacket(String uid, Integer totalNumber, Integer totalAmount) {

        RedPacketInfo redPacketInfo = new RedPacketInfo();
        redPacketInfo.setUid(Integer.valueOf(uid));
        redPacketInfo.setTotalPacket(totalNumber);
        redPacketInfo.setTotalAmount(totalAmount);
        redPacketInfo.setRemainingPacket(totalNumber);
        redPacketInfo.setRemainingAmount(totalAmount);
        redPacketInfo.setCreateTime(new Date());
        Long redPacketId = System.currentTimeMillis();
        redPacketInfo.setRedPacketId(redPacketId);
        redPacketInfoMapper.insert(redPacketInfo);

        redisService.set(redPacketId + "_totalNumber", String.valueOf(totalNumber));
        redisService.set(redPacketId + "_totalAmount", String.valueOf(totalAmount));
        return "success";
    }


    @ResponseBody
    @RequestMapping("/getRedPacketMoney")
    public String getRedPacketMoney(String uid, long redPacketId) {

        RedPacketRecord redPacketRecord = new RedPacketRecord();
        redPacketRecord.setUid(Integer.valueOf(uid));
        redPacketRecord.setRedPacketId(redPacketId);
        redPacketRecordMapper.insertSelective(redPacketRecord);

        redisService.decr(redPacketId + "_totalNumber", 1);
        redisService.decr(redPacketId + "_totalAmount", 1);
        return "success";
    }
}
