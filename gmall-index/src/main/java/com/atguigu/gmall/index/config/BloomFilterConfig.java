package com.atguigu.gmall.index.config;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Configuration
public class BloomFilterConfig {

    @Autowired
    RedissonClient redissonClient;
    @Autowired
    GmallPmsClient pmsClient;
    private static final String KEY_PREFIX="index:category:";
    @Bean
    public RBloomFilter bloomFilter(){
        RBloomFilter<Object> bloomFilter = this.redissonClient.getBloomFilter("index:bloom:filter");
        bloomFilter.tryInit(1000,0.03);
        //设置bloomFilter初始数据  查询一级分类的id来判断是否储存在二级三级分类
        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryCategoryEntityByPid(0L);
        List<CategoryEntity> categoryEntities = listResponseVo.getData();
        if(!CollectionUtils.isEmpty(categoryEntities)){
            categoryEntities.forEach(categoryEntity -> {
                bloomFilter.add(KEY_PREFIX+"["+categoryEntity.getId()+"]");
            });
        }
        return bloomFilter;
    }
}
