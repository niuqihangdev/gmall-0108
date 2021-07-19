package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.entity.CartEntity;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.common.bean.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.atguigu.gmall.cart.service.CartService;
import org.springframework.ui.Model;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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

    /**
     * 添加购物车
     * @return
     */
    @GetMapping()
    public String addCart(CartEntity cart){
        this.cartService.addCart(cart);
        return "redirect:http://cart.gmall.com/addCart.html?skuId="+cart.getSkuId()+"&count="+cart.getCount();
    }

    /**
     * 购物车回显
     * @return
     */
    @GetMapping("addCart.html")
    public String queryCartBySkuId(CartEntity cart,Model model){
        BigDecimal count = cart.getCount();
        cart=this.cartService.queryCartBySkuId(cart);
        cart.setCount(count);
        model.addAttribute("cart",cart);
        return "addCart";
    }

    /**
     * 查询购物车
     * @return
     */
    @GetMapping("cart.html")
    public String queryCart(Model model){
        List<CartEntity> carts=this.cartService.queryCart();
        model.addAttribute("carts",carts);
        return "cart";
    }

    @PostMapping("updateNum")
    @ResponseBody
    public ResponseVo updateCart(@RequestBody CartEntity cart){
        this.cartService.updateCart(cart);
        return ResponseVo.ok();
    }

    @PostMapping("deleteCart")
    @ResponseBody
    public ResponseVo deleteCart(@RequestParam("skuId")Long skuId){
        this.cartService.deleteCart(skuId);
        return ResponseVo.ok();
    }


















    @GetMapping("test")
    @ResponseBody
    public String test() throws ExecutionException, InterruptedException {
        //System.out.println(LoginInterceptor.getUserInfo());
        long now = System.currentTimeMillis();
        System.out.println("controller方法开始执行--------------------");
        this.cartService.executor1();
        this.cartService.executor2();
        /*future1.addCallback(result -> {
            System.out.println("executor1成功的回调:"+result);
        },ex -> {
            System.out.println("executor1的回调："+ex);
        });
        future2.addCallback(result -> {
            System.out.println("executor2成功的回调:"+result);
        },ex -> {
            System.out.println("executor2的回调："+ex);
        });*/
        System.out.println("controller方法执行结束--------------------"+(System.currentTimeMillis()-now));
        return "hello,Test";
    }

}
