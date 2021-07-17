package com.atguigu.gmall.auth.properties;

import com.atguigu.gmall.common.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String pubKeyPath;
    private String priKeyPath;
    private String secret;
    private Integer expireMinutes;
    private String cookieName;
    private String unick;

    private PublicKey publicKey;
    private PrivateKey privateKey;

    @PostConstruct
    public void init(){
        File pubFile=new File(pubKeyPath);
        File priFile=new File(priKeyPath);
        //判断公钥私钥路径是否为空
        if(!priFile.exists()||!pubFile.exists()){
            try {
                //从新生成公钥私钥
                RsaUtils.generateKey(pubKeyPath,priKeyPath,secret);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            this.publicKey=RsaUtils.getPublicKey(pubKeyPath);
            this.privateKey=RsaUtils.getPrivateKey(priKeyPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
