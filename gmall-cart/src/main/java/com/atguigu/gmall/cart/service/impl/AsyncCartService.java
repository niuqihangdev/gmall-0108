package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.entity.CartEntity;
import com.atguigu.gmall.cart.mapper.CartMapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncCartService {
    @Autowired
    CartMapper cartMapper;

    /**
     * 异步执行更新和新增
     * @param cart
     * @param userId
     */
    @Async
    public void updateCart(CartEntity cart,String userId){
        this.cartMapper.update(cart,new UpdateWrapper<CartEntity>().eq("user_id",userId).eq("sku_id",cart.getSkuId()));
    }

    @Async
    public void insertCart(CartEntity cart){
        this.cartMapper.insert(cart);
    }

    @Async
    public void deleteCart(String userId) {
        this.cartMapper.delete(new UpdateWrapper<CartEntity>().eq("user_id",userId));
    }

    @Async
    public void deleteCartByUserIdAndSkuId(String userId, Long skuId) {
        this.cartMapper.delete(new UpdateWrapper<CartEntity>().eq("user_id",userId).eq("sku_id",skuId));
    }
}
