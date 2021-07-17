package com.atguigu.gmall.auth;

import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.common.utils.RsaUtils;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;


import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {

    // 别忘了创建D:\\project\rsa目录
    private static final String pubKeyPath = "D:\\project\\rsa\\rsa.pub";
    private static final String priKeyPath = "D:\\project\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234!@@#@@@$@");
    }

    @BeforeEach
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "liuyan");
        // 生成token
        String token = JwtUtils.generateToken(map, RsaUtils.getPrivateKey(priKeyPath), 2);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE2MjYzNjYyODl9.kt1IKFULddxTuvBdaskShqyFWdgX5EsdO6zfRsxMMtZvprlFYzjF1edJmx1P-A8jPb7XTnavg2mweH2DHolylNq1N0uPTOOJfEoXiLdOhzUMByYlD_gM5kYq6cUEo0X3vnvK7Q_CFMtlzXJjWdt_XAecufFSI-zWb1kXDLwduuLaNc4z6oxDaLCxcN36ZTq3_4A6SKfAARtbiQMSPU5v4ABBYR1nE87jwHC5hGyyolsU_B23lgBwcmWCDQwOEdrhPGkGp0ClyU88lHlkjPym_LPVQc1GXzj__lr6as0gMMYe5MYBM1_JE8VZMH7atLJNxPtlgYkHhxBD_6g3c_7VMw";

        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token,  RsaUtils.getPublicKey(pubKeyPath));
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }
}
