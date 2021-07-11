package com.atguigu.gmall.index.config;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
//自定义注解+分布式锁+aop封装缓存
public @interface GmallCache {
    /**
     * 自定义缓存的前缀
     */
    String prefix() default "gmall";

    /**
     * 缓存过期的时间
     */
    int timeout() default 30;

    /**
     * 为了防止缓存雪崩，自定义随机缓存过期的时间
     */
    int random() default 30;

    /**
     * 为了防止缓存击穿，自定义缓存的前缀
     */
    String lock() default "lock";
}
