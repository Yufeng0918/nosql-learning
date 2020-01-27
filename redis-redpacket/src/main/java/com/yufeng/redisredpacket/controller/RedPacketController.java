package com.yufeng.redisredpacket.controller;



import com.yufeng.redisredpacket.domain.RedPacketInfo;
import com.yufeng.redisredpacket.domain.RedPacketInfoExample;
import com.yufeng.redisredpacket.domain.RedPacketRecord;
import com.yufeng.redisredpacket.mapper.RedPacketInfoMapper;
import com.yufeng.redisredpacket.mapper.RedPacketRecordMapper;
import com.yufeng.redisredpacket.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.Random;

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

    private static final String TOTAL_NUMBER = "_totalNumber";
    private static final String TOTAL_AMOUNT = "_totalAmount";


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
        redPacketInfo.setUpdateTime(new Date());
        Long redPacketId = System.currentTimeMillis();
        redPacketInfo.setRedPacketId(redPacketId);
        redPacketInfoMapper.insertSelective(redPacketInfo);

        redisService.set(redPacketId + TOTAL_NUMBER, String.valueOf(totalNumber));
        redisService.set(redPacketId + TOTAL_AMOUNT, String.valueOf(totalAmount));
        return "success";
    }


    @ResponseBody
    @RequestMapping("/getPacket")
    public Integer getPacket(long redPacketId) {

        Integer num = Integer.valueOf((String)redisService.get(redPacketId + TOTAL_NUMBER));
        if (num != null && num > 0) {
            return num;
        }
        return 0;
    }


    @ResponseBody
    @RequestMapping("getRedPacketMoney")
    public String getRedPacketMoney(String uid, long redPacketId) {

        String redPacketNum = redPacketId + TOTAL_NUMBER;
        String redPacketAmount = redPacketId + TOTAL_AMOUNT;

        // replace by lua script
        String num = (String)redisService.get(redPacketNum);
        if (!StringUtils.hasText(num) || Integer.parseInt(num) == 0) {
            return "You come late";
        }
        redisService.decr(redPacketNum, 1);

        String totalAmount = (String)redisService.get(redPacketAmount);
        Integer randomAmt = 0;
        if (!StringUtils.hasText(totalAmount)) {
            Integer totalAmountInt = Integer.parseInt(totalAmount);
            Integer totalNumberInt = Integer.parseInt(num);
            Integer maxAmount  = (totalAmountInt / totalNumberInt) * 2;

            Random random = new Random();
            randomAmt = random.nextInt(maxAmount);
        }
        redisService.decr(redPacketAmount, randomAmt);


        RedPacketRecord redPacketRecord = new RedPacketRecord();
        redPacketRecord.setUid(Integer.valueOf(uid));
        redPacketRecord.setAmount(randomAmt);
        redPacketRecord.setCreateTime(new Date());
        redPacketRecordMapper.insertSelective(redPacketRecord);

        redPacketInfoMapper.selectByRedPacketId(redPacketId);
        return String.valueOf(randomAmt);
    }
}
