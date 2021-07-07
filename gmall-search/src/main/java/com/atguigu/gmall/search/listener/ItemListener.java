package com.atguigu.gmall.search.listener;


import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.search.vo.SearchAttrValueVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.rabbitmq.client.Channel;
import io.jsonwebtoken.lang.Collections;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class ItemListener {
    @Autowired
    GmallPmsClient pmsClient;
    @Autowired
    GmallWmsClient wmsClient;
    @Autowired
    GoodsRepository goodsRepository;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("SEARCH_ITEM_QUEUE"),
            exchange = @Exchange(value = "PMS_ITEM_EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = {"item.insert"}
    ))
    public void listen(Long spuId, Channel channel, Message message) throws IOException {
        //判断是否是垃圾消息
        if(spuId==null){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            return;
        }
        ResponseVo<SpuEntity> spuEntityResponseVo = pmsClient.querySpuById(spuId);
        SpuEntity spuEntity = spuEntityResponseVo.getData();
        if(spuEntity==null){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            return;
        }

        //查询品牌信息
        ResponseVo<BrandEntity> brandEntityResponseVo = pmsClient.queryBrandById(spuEntity.getBrandId());
        BrandEntity brandEntity= brandEntityResponseVo.getData();
        //查询分类信息
        ResponseVo<CategoryEntity> categoryEntityResponseVo = pmsClient.queryCategoryById(spuEntity.getCategoryId());
        CategoryEntity categoryEntity = categoryEntityResponseVo.getData();

        //查询spu下的sku信息
        ResponseVo<List<SkuEntity>> skuResponse = this.pmsClient.querySkuBySpuId(spuId);
        List<SkuEntity> skuEntities = skuResponse.getData();
        if(!Collections.isEmpty(skuEntities)){
            List<Goods> goodsList = skuEntities.stream().map(skuEntity -> {
                //将sku信息保存到Goods对象中
                Goods goods=new Goods();
                goods.setSkuId(skuEntity.getId());
                goods.setDefaultImage(skuEntity.getDefaultImage());
                goods.setTitle(skuEntity.getTitle());
                goods.setSubTitle(skuEntity.getSubtitle());
                goods.setPrice(skuEntity.getPrice().doubleValue());

                //通过sku查询库存信息
                goods.setCreateTime(spuEntity.getCreateTime());
                ResponseVo<List<WareSkuEntity>> wmsSkuResponse = wmsClient.queryWareskuBySkuId(skuEntity.getId());
                List<WareSkuEntity> wareSkuEntities = wmsSkuResponse.getData();
                if(!CollectionUtils.isEmpty(wareSkuEntities)){
                    goods.setSales(wareSkuEntities.stream().mapToLong(WareSkuEntity::getSales).reduce((a, b) -> a + b).getAsLong());
                    goods.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock()-wareSkuEntity.getStockLocked()>0));
                }

                //将品牌信息存入goods中
                if(brandEntity!=null){
                    goods.setBrandId(brandEntity.getId());
                    goods.setBrandName(brandEntity.getName());
                    goods.setLogo(brandEntity.getLogo());
                }

                //将分类信息存入goods中
                if(categoryEntity!=null){
                    goods.setCategoryId(categoryEntity.getId());
                    goods.setCategoryName(categoryEntity.getName());
                }

                //查询SkuAttrValue 信息 SpuAttrValue 将信息放入集合中存入goods
                List<SearchAttrValueVo> searchAttrValueVos = new ArrayList<>();
                ResponseVo<List<SkuAttrValueEntity>> skuAttrResponse = pmsClient.querySearchBySkuId(spuEntity.getCategoryId(), skuEntity.getId());
                List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrResponse.getData();
                //将skuAttrValue 复制给SearchAttrValueVo对象存入集合中
                if(!CollectionUtils.isEmpty(skuAttrValueEntities)){
                    List<SearchAttrValueVo> skuList=skuAttrValueEntities.stream().map(skuAttrValueEntity ->{
                                SearchAttrValueVo search=new SearchAttrValueVo();
                                BeanUtils.copyProperties(skuAttrValueEntity,search);
                                return search;
                            }
                    ).collect(Collectors.toList());
                    searchAttrValueVos.addAll(skuList);
                }
                //spuAttrValue
                ResponseVo<List<SpuAttrValueEntity>> spuAttrResponse = pmsClient.querySearchBySpuId(spuEntity.getCategoryId(), spuEntity.getId());
                List<SpuAttrValueEntity> spuAttrValueEntities = spuAttrResponse.getData();
                if(!CollectionUtils.isEmpty(spuAttrValueEntities)){
                    List<SearchAttrValueVo> spuList=spuAttrValueEntities.stream().map(spuAttrValueEntity ->{
                                SearchAttrValueVo search=new SearchAttrValueVo();
                                BeanUtils.copyProperties(spuAttrValueEntity,search);
                                return search;
                            }
                    ).collect(Collectors.toList());
                    searchAttrValueVos.addAll(spuList);
                }

                goods.setSearchAttrs(searchAttrValueVos);

                return goods;
            }).collect(Collectors.toList());
            this.goodsRepository.saveAll(goodsList);
        }





    }
}
