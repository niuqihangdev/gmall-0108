package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.atguigu.gmall.cart.service.CartService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * 
 *
 * @author niuqihang
 * @email 1208936851@qq.com
 * @date 2021-07-16 19:42:04
 */

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("test")
    @ResponseBody
    public String test(){
        System.out.println(LoginInterceptor.getUserInfo());
        return "hello,Test";
    }

}
