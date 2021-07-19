package com.atguigu.gmall.cart.listener;

import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;


@Component
public class CartListener {
    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String PRICE_PREFIX="cart:price:";
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("CART_ITEM_QUEUE"),
            exchange = @Exchange(value = "PMS_ITEM_EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = {"item_update"}
    ))
    public void listen(Long spuId, Channel channel, Message message) throws IOException {
        //判断是否为垃圾信息
        if(spuId==null){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            return;
        }
        List<SkuEntity> skuEntities = this.pmsClient.querySkuBySpuId(spuId).getData();
        if(CollectionUtils.isEmpty(skuEntities)){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            return;
        }
        skuEntities.forEach(skuEntity -> {
            this.redisTemplate.opsForValue().setIfPresent(PRICE_PREFIX+skuEntity.getId(),skuEntity.getPrice().toString());
        });
        //确认消息
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);


    }
}
