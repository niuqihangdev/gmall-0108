package com.atguigu.gmall.auth.service.impl;

import com.atguigu.gmall.auth.feign.GmallUmsClient;
import com.atguigu.gmall.auth.properties.JwtProperties;
import com.atguigu.gmall.auth.service.AuthService;
import com.atguigu.gmall.common.exception.AuthException;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.IpUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.ums.api.GmallUmsApi;
import com.atguigu.gmall.ums.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Service
@EnableConfigurationProperties(JwtProperties.class)
public class AuthServiceImpl implements AuthService {
    @Autowired
    GmallUmsClient umsClient;
    @Autowired
    private JwtProperties jwtProperties;
    @Override
    public void login(String loginName, String password, HttpServletRequest request, HttpServletResponse response) throws Exception {
        //通过远程ums接口查询，查询用户是否存在
        UserEntity userEntity = this.umsClient.queryUser(loginName, password).getData();
        //如果不存在，抛出异常
        if(userEntity==null){
            throw new AuthException("用户信息不存在");
        }
        //存在，设置载荷
        Map<String, Object> map = new HashMap<>();
        map.put("userId",userEntity.getId());
        map.put("userName",userEntity.getUsername());
        //获取用户ip地址，放入载荷，防止盗用
        String ip = IpUtils.getIpAddressAtService(request);
        map.put("ip",ip);
        //生成jwt,生成token
        String token = JwtUtils.generateToken(map, jwtProperties.getPrivateKey(), jwtProperties.getExpireMinutes());
        //将jwt放入cookie中
        CookieUtils.setCookie(request,response,jwtProperties.getCookieName(),token,jwtProperties.getExpireMinutes()*60);
        //在页面显示用户登录信息，放入cookie中
        CookieUtils.setCookie(request,response,jwtProperties.getUnick(),userEntity.getNickname(),jwtProperties.getExpireMinutes()*60);
    }
}
