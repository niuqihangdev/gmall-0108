package com.atguigu.gmall.cart.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig implements AsyncConfigurer {
    /**
     * 配置线程池
     * @return
     */
    @Override
    public Executor getAsyncExecutor() {
        return null;
    }

    /**
     * 配置统一的异常
     * @return
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return null;
    }
}
