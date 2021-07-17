package com.atguigu.gmall.gateway.filter;

import com.atguigu.gmall.common.utils.IpUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.gateway.properties.JwtProperties;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@EnableConfigurationProperties(JwtProperties.class)
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.PathConfig> {
    @Autowired
    JwtProperties jwtProperties;

    public AuthGatewayFilterFactory() {
        super(PathConfig.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("paths");
    }

    @Override
    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST;
    }

    @Override
    public GatewayFilter apply(PathConfig config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                System.out.println("局部过滤器  paths: "+config.paths);
                //获取请求
                ServerHttpRequest request = exchange.getRequest();
                ServerHttpResponse response = exchange.getResponse();

                //1、判断当前路径是否被过滤，如果未被过滤，直接放行
                String path = request.getURI().getPath();
                List<String> curPaths = config.getPaths();
                //allMatch：判断条件里的元素，所有的都是，返回true
                if (curPaths.stream().allMatch(curPath->!path.startsWith(curPath))) {
                    return chain.filter(exchange);
                }//path的是否以curPath中的某一个开头

                //2、获取token信息，异步在token头中，同步在cookie中
                String token = request.getHeaders().getFirst(jwtProperties.getToken());
                if(StringUtils.isBlank(token)){
                    MultiValueMap<String, HttpCookie> cookies = request.getCookies();
                    if (!CollectionUtils.isEmpty(cookies) && cookies.containsKey(jwtProperties.getCookieName())){
                        token = cookies.getFirst(jwtProperties.getCookieName()).getValue();
                    }
                }

                //3、判断token是否为空，为空重定向到登录页面
                if(StringUtils.isBlank(token)){
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION,"http://sso.gmall.com/toLogin.html?returnUrl="+request.getURI());
                    return response.setComplete();
                }
                try {
                    //4、解析jwt，如果出现异常，重定向到登录页面
                    Map<String, Object> map = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());

                    //5、获取载荷中ip地址和当前登录的ip地址进行对比，如果不相同，重定向到登录页面
                    String ip = map.get("ip").toString();
                    String usrIp = IpUtils.getIpAddressAtGateway(request);
                    if(!StringUtils.equals(ip,usrIp)){
                        response.setStatusCode(HttpStatus.SEE_OTHER);
                        response.getHeaders().set(HttpHeaders.LOCATION,"http://sso.gmall.com/toLogin.html?returnUrl="+request.getURI());
                        return response.setComplete();
                    }

                    //6、将解析后的用户信息和传递给其他服务
                    request.mutate().header("userId",map.get("userId").toString())
                            .header("userName",map.get("userName").toString()).build();
                    exchange.mutate().request(request).build();

                    //7、放行
                    return chain.filter(exchange);
                } catch (Exception e) {
                    e.printStackTrace();
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION,"http://sso.gmall.com/toLogin.html?returnUrl="+request.getURI());
                    return response.setComplete();
                }

            }
        };
    }


    @Data
    public static class PathConfig{
        public List<String> paths;
    }
}
