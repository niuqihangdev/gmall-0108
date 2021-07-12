package com.atguigu.gmall.index.aspect;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.index.config.GmallCache;
import io.lettuce.core.RedisClient;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class GmallCacheAspect {
    /**
     * 获取目标类  joinPoint.getTarget().getClass
     * 目标方法签名  （MethodSignature）joinPoint.getSignature
     * 目标方法   signature.getMethod()
     * 目标方法所有参数  signature.getArgs()
     */
    /*@Before("execution(* com.atguigu.gmall.index.service.*.*(..))")
    public void before(JoinPoint joinPoint){
        //获取目标方法的签名
        MethodSignature signature=(MethodSignature)joinPoint.getSignature();
        System.out.println("这是前置方法"+joinPoint.getTarget().getClass().getName());
        System.out.println("目标方法"+signature.getMethod().getName());
    }*/

    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    RBloomFilter bloomFilter;

    @Around("@annotation(com.atguigu.gmall.index.config.GmallCache)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable{
        //获取目标方法的签名
        MethodSignature signature=(MethodSignature)joinPoint.getSignature();
        //获取目标方法
        Method method = signature.getMethod();
        //获取方法前注解信息
        GmallCache gmallCache = method.getAnnotation(GmallCache.class);
        String prefix = gmallCache.prefix();
        //将目标方法的所有参数放入集合中
        List<Object> key = Arrays.asList(joinPoint.getArgs());
        //获取目标方法的返回值类型
        Class returnType = signature.getReturnType();
        //获取分布式锁的名字
        String lock = gmallCache.lock();

        //添加布隆过滤器，布隆过滤器已将一级分类id放入过滤器中
        if (!this.bloomFilter.contains(prefix+key)) {
            return null;
        }

        //1、查询缓存，判断缓存中是否存在查询数据
        String json = this.redisTemplate.opsForValue().get(prefix + key);
        if(StringUtils.isNotBlank(json)){
            //如果缓存中有数据
            JSON.parseObject(json,returnType);
        }

        //2、添加分布式锁，查询数据库，防止缓存击穿
        RLock fairLock = this.redissonClient.getFairLock(lock + key);
        fairLock.lock();
        try {
            //3、再次查询缓存，当前线程获取锁的同时，其他线程可能已经获取到锁，并将数据放入缓存中
            String json2 = this.redisTemplate.opsForValue().get(prefix + key);
            if(StringUtils.isNotBlank(json2)){
                //如果缓存中有数据
                JSON.parseObject(json2,returnType);
            }
            //4、执行目标方法，查询数据库
            Object result = joinPoint.proceed(joinPoint.getArgs());
            //5、将数据放入缓存，考虑缓存雪崩和缓存穿透
            int timeout=gmallCache.timeout()+new Random().nextInt(gmallCache.random());
            if(result!=null){
                this.redisTemplate.opsForValue().set(prefix+key,JSON.toJSONString(result),timeout, TimeUnit.MINUTES);
            }
            return result;
        } finally {
            fairLock.unlock();
        }
    }
}
