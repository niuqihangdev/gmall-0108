package com.atguigu.gmall.index.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.config.GmallCache;
import com.atguigu.gmall.index.config.RedissonConfig;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.index.util.DistributeLock;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class IndexServiceImpl implements IndexService {
    @Autowired
    GmallPmsClient pmsClient;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    DistributeLock distributeLock;
    @Autowired
    RedissonClient redissonClient;


    private static final String KEY_PREFIX="index:category:";
    private static final String LOCK_PREFIX="index:category:lock:";

    /**
     * 同步查询一级标题
     * @return
     */
    @Override
    public List<CategoryEntity> queryLvl1Categories() {
        //查询缓存
        String categories = this.redisTemplate.opsForValue().get(KEY_PREFIX + 0);
        if(StringUtils.isNotBlank(categories)){
            //缓存不为空 直接返回
            List<CategoryEntity> categoryEntities = JSON.parseArray(categories, CategoryEntity.class);
            return categoryEntities;
        }

        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryCategoryEntityByPid(0L);
        //放入缓存
        this.redisTemplate.opsForValue().set(KEY_PREFIX+0,JSON.toJSONString(listResponseVo.getData()),30,TimeUnit.DAYS);
        return listResponseVo.getData();
    }


    /**
     * 异步查询二级、三级标题 （注解）
     * @param pid
     * @return
     */

    @Override
    @GmallCache(prefix = KEY_PREFIX,timeout = 259200,random = 14400,lock = LOCK_PREFIX)
    public List<CategoryEntity> queryLvl2Categories(Long pid) {

        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryLvl2ByPid(pid);
        return listResponseVo.getData();

    }


    /**
     * 异步查询二级、三级标题
     * @param pid
     * @return
     */

    public List<CategoryEntity> queryLvl2Categories2(Long pid) {
        //从缓存中获取分类信息
        String categoryCache = this.redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if(StringUtils.isNotBlank(categoryCache)){
            //如果缓存数据不为空   将json字符串转换为对象类型
            List<CategoryEntity> categoryEntities = JSON.parseArray(categoryCache, CategoryEntity.class);
            return categoryEntities;
        }

        //为了防止缓存击穿，添加分布式锁
        RLock fairLock = this.redissonClient.getFairLock(LOCK_PREFIX + pid);
        fairLock.lock();


        try {
            //当前线程获取锁的同时，可能已经有其他线程获取锁，并将数据放入缓存中，应查询此时缓存中是否存在
            String categoryCache2 = this.redisTemplate.opsForValue().get(KEY_PREFIX + pid);
            if(StringUtils.isNotBlank(categoryCache2)){
                //如果缓存数据不为空   将json字符串转换为对象类型
                List<CategoryEntity> categoryEntities = JSON.parseArray(categoryCache2, CategoryEntity.class);
                return categoryEntities;
            }

            //如果缓存为空，直接查询数据库
            ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryLvl2ByPid(pid);

            //防止缓存穿透，导致mysql宕机
            if(CollectionUtils.isEmpty(listResponseVo.getData())){
                this.redisTemplate.opsForValue().set(KEY_PREFIX+pid,JSON.toJSONString(listResponseVo.getData()),5,TimeUnit.MINUTES);
            }else{

                //将分类信息存储到缓存中  将对象序列化成一个字符串 增加redis可读性
                //设置缓存随机过期时间，防止缓存同时过期，造成缓存雪崩
                this.redisTemplate.opsForValue().set(KEY_PREFIX+pid,JSON.toJSONString(listResponseVo.getData()),30+new Random().nextInt(10), TimeUnit.DAYS);

            }
            return listResponseVo.getData();
        } finally {
            fairLock.unlock();
        }

    }

    /**
     * 测试本地锁
     */
    @Override
    public  void testLock() {
        //生成一个uuid，用来防误删
        String uuid = UUID.randomUUID().toString();
        //获取锁sentx     setIfAbsent  如果lock不存在，设置锁
        Boolean lock = this.redisTemplate.opsForValue().setIfAbsent("lock", uuid,3,TimeUnit.SECONDS);
        //如果分布式锁获取失败 sleep 进行递归
        if(!lock){
            try {
                Thread.sleep(100);
                testLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else{
            //如果获取锁成功，执行业务流程
            String numString = this.redisTemplate.opsForValue().get("num");
            if(StringUtils.isBlank(numString)){
                this.redisTemplate.opsForValue().set("num","1");
                return;
            }
            int num = Integer.parseInt(numString);
            this.redisTemplate.opsForValue().set("num",String.valueOf(++num));

            //判断是否为本线程的锁
            String script="if(redis.call('get',KEYS[1])==ARGV[1]) then return redis.call('del','KEYS[1]') else return 0 end";
            this.redisTemplate.execute(new DefaultRedisScript<>(script,Boolean.class), Arrays.asList("lock"),uuid);
           /* if(StringUtils.equals(uuid,this.redisTemplate.opsForValue().get("lock"))){
                //执行完业务流程 释放锁
                this.redisTemplate.delete("lock");
            }*/

        }

    }

    /**
     * 测试可重入锁
     */
    @Override
    public  void testLock2() {

        String uuid = UUID.randomUUID().toString();
        Boolean flag = this.distributeLock.lock("lock", uuid, 30);
        //如果加锁成功
        if(flag){
            //如果获取锁成功，执行业务流程
            String numString = this.redisTemplate.opsForValue().get("num");
            if(StringUtils.isBlank(numString)){
                this.redisTemplate.opsForValue().set("num","1");
                return;
            }
            int num = Integer.parseInt(numString);
            this.redisTemplate.opsForValue().set("num",String.valueOf(++num));

            //解锁
            this.distributeLock.unlock("lock",uuid);
        }

        }

    /**
     * 测试Redisson锁
     */
    @Override
    public  void testLock3() {
        RLock lock = this.redissonClient.getLock("lock");
        //加锁
        lock.lock();
        try {
            //如果获取锁成功，执行业务流程
            String numString = this.redisTemplate.opsForValue().get("num");
            if(StringUtils.isBlank(numString)){
                this.redisTemplate.opsForValue().set("num","1");
                return;
            }
            int num = Integer.parseInt(numString);
            this.redisTemplate.opsForValue().set("num",String.valueOf(++num));
        } finally {
            lock.unlock();
        }

    }

    /**
     * 测试读写锁
     */
    @Override
    public void testRead() {
        RReadWriteLock rLock = this.redissonClient.getReadWriteLock("rLock");
        rLock.readLock().lock(10,TimeUnit.SECONDS);
        System.out.println("-------------------------测试读锁");
    }

    @Override
    public void testWrite() {
        RReadWriteLock rLock = this.redissonClient.getReadWriteLock("rLock");
        rLock.writeLock().lock(10,TimeUnit.SECONDS);
        System.out.println("-------------------------测试写锁");
    }

    /**
     * 测试闭锁
     */
    @Override
    public void testLatch() {
        try {
            RCountDownLatch cdl = this.redissonClient.getCountDownLatch("cdl");
            cdl.trySetCount(6);
            cdl.await();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void testCountDown() {
        RCountDownLatch cdl = this.redissonClient.getCountDownLatch("cdl");
        cdl.countDown();

    }


}
