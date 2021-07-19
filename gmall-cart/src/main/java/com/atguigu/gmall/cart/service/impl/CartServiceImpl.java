package com.atguigu.gmall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.entity.CartEntity;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.feign.GmallSmsClient;
import com.atguigu.gmall.cart.feign.GmallUmsClient;
import com.atguigu.gmall.cart.feign.GmallWmsClient;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.pojo.UserInfo;
import com.atguigu.gmall.common.exception.CartException;
import com.atguigu.gmall.pms.api.GmallPmsApi;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import com.atguigu.gmall.cart.service.CartService;
import org.springframework.util.CollectionUtils;
import org.springframework.util.concurrent.ListenableFuture;
import springfox.documentation.spring.web.json.Json;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private GmallSmsClient smsClient;
    @Autowired
    private GmallWmsClient wmsClient;
    @Autowired
    AsyncCartService asyncCartService;
    /**
     * 添加购物车
     * @param cart
     */
    private static final String KEY_PREFIX="cart:info:";
    private static final String PRICE_PREFIX="cart:price:";
    @Override
    public void addCart(CartEntity cart) {
        //1、查询用户登录状态,登录使用userId，未登录使用userKey
        String userId = getUserInfo();
        //2、获取redis中该用户的购物车
        BoundHashOperations<String, Object, Object> boundHashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
        //3、判断购物车中是否存在该商品
        String skuId = cart.getSkuId().toString();
        BigDecimal count = cart.getCount();//用户向购物车中添加的商品数量
        if(boundHashOps.hasKey(skuId)){
            //4、存在，更新购物车和数据库
            String skuJackson = boundHashOps.get(skuId).toString();
            cart = JSON.parseObject(skuJackson, CartEntity.class);
            cart.setCount(cart.getCount().add(count));

            //更新数据库
            this.asyncCartService.updateCart(cart,userId);
        }else{
            //5、不存在，新增购物车
            cart.setUserId(userId);

            //添加sku信息
            SkuEntity skuEntity = this.pmsClient.querySkuById(cart.getSkuId()).getData();
            if(skuEntity!=null){
                cart.setDefaultImage(skuEntity.getDefaultImage());
                cart.setTitle(skuEntity.getTitle());
                cart.setPrice(skuEntity.getPrice());
            }

            cart.setCheck(true);

            //设置销售属性
            List<SkuAttrValueEntity> skuAttrValueEntities = this.pmsClient.querySkuAttrValueBySkuId(cart.getSkuId()).getData();
            if(!CollectionUtils.isEmpty(skuAttrValueEntities)){
                cart.setSaleAttrs(JSON.toJSONString(skuAttrValueEntities));
            }

            //设置是否有货
            List<WareSkuEntity> wareSkuEntities = this.wmsClient.queryWareskuBySkuId(cart.getSkuId()).getData();
            if(!CollectionUtils.isEmpty(wareSkuEntities)){
                cart.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity ->
                        wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }

            //设置营销信息
            List<ItemSaleVo> itemSaleVos = this.smsClient.queryItemSalesBySkuId(cart.getSkuId()).getData();
            if(!CollectionUtils.isEmpty(itemSaleVos)){
                cart.setSales(JSON.toJSONString(itemSaleVos));
            }

            //异步新增
            this.asyncCartService.insertCart(cart);
            //添加实时价格缓存
            this.redisTemplate.opsForValue().set(PRICE_PREFIX+skuId,skuEntity.getPrice().toString());

        }
        boundHashOps.put(skuId,JSON.toJSONString(cart));



    }

    /**
     * 回显购物车
     * @param cart
     * @return
     */
    @Override
    public CartEntity queryCartBySkuId(CartEntity cart) {
        //查询用户登录信息
        String userId = getUserInfo();
        //从redis中获取购物车信息
        BoundHashOperations<String, Object, Object> boundHashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
        String skuId = cart.getSkuId().toString();
        if(boundHashOps.hasKey(skuId)){
            String cartJackson = boundHashOps.get(skuId).toString();
            return JSON.parseObject(cartJackson,CartEntity.class);
        }
        throw new RuntimeException("你的购物车为空");
    }


    //查询用户登录信息
    public String getUserInfo(){
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userId = userInfo.getUserKey();
        if(userInfo.getUserId()!=null){
            userId=userInfo.getUserId().toString();
        }
        return userId;
    }

    /**
     * 查询购物车
     * @return
     */
    @Override
    public List<CartEntity> queryCart() {
        //1、查询未登录购物车的记录
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userKey=userInfo.getUserKey();
        //根据userkey查询未登录购物车
        BoundHashOperations<String, Object, Object> unLoginHashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userKey);
        List<Object> cartJsons = unLoginHashOps.values();
        List<CartEntity> unloginCart=null;
        if(!CollectionUtils.isEmpty(cartJsons)){
            unloginCart=cartJsons.stream().map(cartJson-> {
                CartEntity cart = JSON.parseObject(cartJson.toString(), CartEntity.class);
                //设置实时价格
                cart.setCurrentPrice(new BigDecimal(this.redisTemplate.opsForValue().get(PRICE_PREFIX+cart.getSkuId())));
                return cart;
            }).collect(Collectors.toList());
        }

        //2、判断用户是否登录，未登录直接返回未登录购物车
        Long userId = userInfo.getUserId();
        if(userId==null){
            return unloginCart;
        }

        //3、已登录，合并购物车记录，并删除未登录的购物车
        BoundHashOperations<String, Object, Object> loginHashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId.toString());
        if(!CollectionUtils.isEmpty(unloginCart)){
            unloginCart.forEach(cart->{
                String skuId = cart.getSkuId().toString();
                //如果登录用户的购物车里存在该商品
                if(loginHashOps.hasKey(skuId)){
                    BigDecimal count = cart.getCount();
                    String loginJsons = loginHashOps.get(skuId).toString();
                    cart = JSON.parseObject(loginJsons, CartEntity.class);
                    cart.setCount(cart.getCount().add(count));
                    this.asyncCartService.updateCart(cart,userId.toString());
                }else{
                    //如果不存在该商品，新增购物车
                    this.asyncCartService.insertCart(cart);
                }
                //新增购物车到redis
                loginHashOps.put(skuId,JSON.toJSONString(cart));
                //删除未登录的购物车
                this.redisTemplate.delete(KEY_PREFIX+userKey);
                this.asyncCartService.deleteCart(userKey);
            });
        }

        //4、查询购物车记录
        List<Object> carts = loginHashOps.values();
        if(!CollectionUtils.isEmpty(carts)){
            return carts.stream().map(loginCart-> {
                CartEntity cart = JSON.parseObject(loginCart.toString(), CartEntity.class);
                cart.setCurrentPrice(new BigDecimal(this.redisTemplate.opsForValue().get(PRICE_PREFIX+cart.getSkuId())));
                return cart;
            }).collect(Collectors.toList());
        }
        return null;

    }

    /**
     * 更新购物车
     */

    @Override
    public void updateCart(CartEntity cart) {
        //查询登录状态
        String userId = this.getUserInfo();
        //获取购物车
        BoundHashOperations<String, Object, Object> boundHashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
        if(!boundHashOps.hasKey(cart.getSkuId().toString())){
            throw new CartException("购物车为空");
        }
        //从页面传来的cart中有count值
        BigDecimal count = cart.getCount();
        String cartJson = boundHashOps.get(cart.getSkuId().toString()).toString();
        cart = JSON.parseObject(cartJson, CartEntity.class);
        cart.setCount(count);
        boundHashOps.put(cart.getSkuId().toString(),JSON.toJSONString(cart));
        this.asyncCartService.updateCart(cart,userId);



    }

    /**
     * 删除购物车
     * @param skuId
     */
    @Override
    public void deleteCart(Long skuId) {
        String userId = this.getUserInfo();
        BoundHashOperations<String, Object, Object> boundHashOps = this.redisTemplate.boundHashOps(KEY_PREFIX+userId);
        boundHashOps.delete(skuId.toString());
        this.asyncCartService.deleteCartByUserIdAndSkuId(userId,skuId);
    }


    @Async
    public ListenableFuture<String> executor1(){
        try {
            System.out.println("executor1开始执行-----------------------");
            TimeUnit.SECONDS.sleep(4);
            System.out.println("executor1执行结束-----------------------");
        } catch (InterruptedException e) {
            return AsyncResult.forExecutionException(e);
        }
        return AsyncResult.forValue("hello executor1");
    }
    @Async
    public ListenableFuture<String> executor2(){
        try {
            System.out.println("executor2开始执行-----------------------");
            TimeUnit.SECONDS.sleep(5);
            System.out.println("executor2执行结束-----------------------");
        } catch (InterruptedException e) {
            return AsyncResult.forExecutionException(e);
        }
        return AsyncResult.forValue("hello executors2");
    }



}