package com.atguigu.gmall.index.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

@Component
public class DistributeLock {
    @Autowired
    StringRedisTemplate redisTemplate;

    private Timer timer;

    /**
     * 加锁方法
     * @param lockName
     * @param uuid
     * @param expire
     * @return
     */
    public Boolean lock(String lockName,String uuid,Integer expire){
        String script="if(redis.call('exists',KEYS[1])==0 or redis.call('hexists',KEYS[1],ARGV[1])==1) " +
                "then " +
                "redis.call('hincrby',KEYS[1],ARGV[1],1) " +
                "redis.call('expire',KEYS[1],ARGV[2]) " +
                "return 1 " +
                "else return 0 " +
                "end";
        Boolean flag = this.redisTemplate.execute(new DefaultRedisScript<>(script,Boolean.class), Arrays.asList(lockName), uuid, expire.toString());
        //如果加锁失败
        if(!flag){
            try {
                Thread.sleep(1000);
                lock(lockName,uuid,expire);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else{
            this.renewExpire(lockName,uuid,expire);
        }
        return true;
    }

    /**
     * 自动续期
     * @param lockName
     * @param uuid
     * @param expire
     */
    private void renewExpire(String lockName,String uuid,Integer expire){
        String script="if(redis.call('hexists',KEYS[1],ARGV[1])==1) " +
                "then " +
                "redis.call('expire',KEYS[1],ARGV[2]) " +
                "end";
        this.timer=new Timer();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                redisTemplate.execute(new DefaultRedisScript<>(script,Boolean.class),Arrays.asList(lockName),uuid,expire.toString());
            }
        },expire*1000/3,expire*1000/3);
    }

    /**
     * 设置解锁方法
     * @param lockName
     * @param uuid
     */
    public void unlock(String lockName,String uuid){
        String script="if(redis.call('hexists',KEYS[1],ARGV[1])==0) " +
                "then " +
                "return nil elseif(redis.call('hincrby',KEYS[1],ARGV[1],-1)==0) " +
                "then return redis.call('del',KEYS[1]) " +
                "else return 0 " +
                "end";
        //使用Long的原因是，空在boolean中也是false
        Long flag = this.redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(lockName), uuid);
        if(flag==null){
            throw new IllegalMonitorStateException("不是自己的锁");
        }else if(flag==1){
            this.timer.cancel();
        }

    }

    /**
     * 定时器  看门狗
     * @param args
     */
    public static void main(String[] args) {
        System.out.println(System.currentTimeMillis());
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("这是一个定时器任务: "+System.currentTimeMillis());
            }
        },5000,10000);
    }

}
