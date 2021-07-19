package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.entity.CartEntity;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.Future;

/**
 * 
 *
 * @author niuqihang
 * @email 1208936851@qq.com
 * @date 2021-07-16 19:42:04
 */
public interface CartService{


    void addCart(CartEntity cart);

    CartEntity queryCartBySkuId(CartEntity cart);

    List<CartEntity> queryCart();

    void updateCart(CartEntity cart);

    void deleteCart(Long skuId);

    ListenableFuture<String> executor1();
    ListenableFuture<String> executor2();



}

