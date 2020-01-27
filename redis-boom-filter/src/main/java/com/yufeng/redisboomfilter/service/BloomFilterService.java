package com.yufeng.redisboomfilter.service;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.yufeng.redisboomfilter.domain.SysUser;
import com.yufeng.redisboomfilter.domain.SysUserExample;
import com.yufeng.redisboomfilter.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @Auther: daiyu
 * @Date: 27/1/20 15:05
 * @Description:
 */
@Service
public class BloomFilterService {


    @Autowired
    private SysUserMapper sysUserMapper;


    private BloomFilter<Integer> bloomFilter;

    @PostConstruct
    public void initBloomFilter() {

        SysUserExample sysUserExample = new SysUserExample();
        List<SysUser> sysUserList = sysUserMapper.selectByExample(sysUserExample);
        if (CollectionUtils.isEmpty(sysUserList)) {
            return;
        }

        bloomFilter = BloomFilter.create(Funnels.integerFunnel(), sysUserList.size());
        sysUserList.forEach(u -> bloomFilter.put(u.getId()));
    }


    public boolean userIdExists(int id) {
        return bloomFilter.mightContain(id);
    }
}
