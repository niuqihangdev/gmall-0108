package com.atguigu.gmall.item.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolExecutorConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(
            @Value("${thread.pool.corePoolSize}")Integer corePoolSize,
            @Value("${thread.pool.maxCorePoolSize}")Integer maxCorePoolSize,
            @Value("${thread.pool.keepAliveTime}")Long keepAliveTime,
            @Value("${thread.pool.workQueue}")Integer workQueue)
    {
        return new ThreadPoolExecutor(corePoolSize, maxCorePoolSize, keepAliveTime, TimeUnit.SECONDS, new ArrayBlockingQueue<>(workQueue));
    }
}
