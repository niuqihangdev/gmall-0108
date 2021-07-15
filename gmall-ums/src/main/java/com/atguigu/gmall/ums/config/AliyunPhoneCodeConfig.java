package com.atguigu.gmall.ums.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "aliyun.phone")
public class AliyunPhoneCodeConfig {
    private String host;
    private String path;
    private String method;
    private String appcode;
    private String smsSignId;
    private String templateId;
}
