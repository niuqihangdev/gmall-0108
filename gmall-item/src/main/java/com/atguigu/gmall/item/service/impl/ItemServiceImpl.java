package com.atguigu.gmall.item.service.impl;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.ItemException;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.vo.ItemVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    GmallPmsClient pmsClient;
    @Autowired
    GmallWmsClient wmsClient;
    @Autowired
    GmallSmsClient smsClient;
    @Autowired
    ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    TemplateEngine templateEngine;

    @Override
    public ItemVo lodeData(Long skuId) {
        ItemVo itemVo = new ItemVo();
        CompletableFuture<SkuEntity> skuFuture=new CompletableFuture<>().supplyAsync(()->{
            //商品详情信息
            SkuEntity skuEntity = this.pmsClient.querySkuById(skuId).getData();
            if(skuEntity==null){
                throw new ItemException("该skuId对应的商品信息不存在");
            }
            itemVo.setSkuId(skuEntity.getId());
            itemVo.setTitle(skuEntity.getTitle());
            itemVo.setSubtitle(skuEntity.getSubtitle());
            itemVo.setPrice(skuEntity.getPrice());
            itemVo.setWeight(skuEntity.getWeight());
            itemVo.setDefaultImage(skuEntity.getDefaultImage());
            return skuEntity;
        },threadPoolExecutor);

        CompletableFuture<Void> cateFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            //一二三级分类信息
            List<CategoryEntity> categoryEntities = this.pmsClient.queryLv123ByCid(skuEntity.getCategoryId()).getData();
            itemVo.setCategories(categoryEntities);
        }, threadPoolExecutor);

        CompletableFuture<Void> brandFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            //品牌信息
            BrandEntity brandEntity = this.pmsClient.queryBrandById(skuEntity.getBrandId()).getData();
            itemVo.setBrandId(brandEntity.getId());
            itemVo.setBrandName(brandEntity.getName());
        }, threadPoolExecutor);

        CompletableFuture<Void> spuFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            //spu信息
            SpuEntity spuEntity = this.pmsClient.querySpuById(skuEntity.getSpuId()).getData();
            if (spuEntity != null) {
                itemVo.setSpuId(spuEntity.getId());
                itemVo.setSpuName(spuEntity.getName());
            }
        }, threadPoolExecutor);

        CompletableFuture<Void> itemSaleFuture = CompletableFuture.runAsync(() -> {
            //营销信息
            List<ItemSaleVo> itemSaleVos = this.smsClient.queryItemSalesBySkuId(skuId).getData();
            if (!CollectionUtils.isEmpty(itemSaleVos)) {
                itemVo.setSales(itemSaleVos);
            }
        }, threadPoolExecutor);


        CompletableFuture<Void> wareSkuFuture = CompletableFuture.runAsync(() -> {
            //库存信息
            List<WareSkuEntity> wareSkuEntities = this.wmsClient.queryWareskuBySkuId(skuId).getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                itemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity ->
                        wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0
                ));
            }
        }, threadPoolExecutor);


        CompletableFuture<Void> skuImageFuture = CompletableFuture.runAsync(() -> {
            //sku图片列表
            List<SkuImagesEntity> skuImagesEntities = this.pmsClient.queryImagesBySkuId(skuId).getData();
            if (!CollectionUtils.isEmpty(skuImagesEntities)) {
                itemVo.setImages(skuImagesEntities);
            }
        }, threadPoolExecutor);


        CompletableFuture<Void> saleAttrFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            //销售属性列表
            List<SaleAttrVo> saleAttrVos = this.pmsClient.querySaleAttrVoBySpuId(skuEntity.getSpuId()).getData();
            if (!CollectionUtils.isEmpty(saleAttrVos)) {
                itemVo.setSaleAttrs(saleAttrVos);
            }
        }, threadPoolExecutor);


        CompletableFuture<Void> skuAttrValueFuture = CompletableFuture.runAsync(() -> {
            //当前sku的销售属性
            List<SkuAttrValueEntity> skuAttrValueEntities = this.pmsClient.querySkuAttrValueBySkuId(skuId).getData();
            if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                Map<Long, String> skuAttrValueMap = skuAttrValueEntities.stream().collect(Collectors.toMap(SkuAttrValueEntity::getAttrId, SkuAttrValueEntity::getAttrValue));
                itemVo.setSaleAttr(skuAttrValueMap);
            }
        }, threadPoolExecutor);


        CompletableFuture<Void> skuJsonsFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            //映射关系
            String json = this.pmsClient.queryMappingBySpuId(skuEntity.getSpuId()).getData();
            if (!StringUtils.isEmpty(json)) {
                itemVo.setSkuJsons(json);
            }
        }, threadPoolExecutor);


        CompletableFuture<Void> spuDescFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            //商品描述
            SpuDescEntity spuDescEntity = this.pmsClient.querySpuDescById(skuEntity.getSpuId()).getData();
            if (spuDescEntity != null) {
                //误用org.springframework.util.StringUtils.split会导致空指针异常，会判定如果结果集或者分隔符为空，则返回null
                itemVo.setSpuImages(Arrays.asList(StringUtils.split(spuDescEntity.getDecript(), ",")));
            }
        }, threadPoolExecutor);


        CompletableFuture<Void> groupFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            //规格参数
            List<GroupVo> groupVoList = this.pmsClient.queryAttrGroupByCidAndSpuIdAndSkuId(skuEntity.getCategoryId(), skuEntity.getSpuId(), skuId).getData();
            if (!CollectionUtils.isEmpty(groupVoList)) {
                itemVo.setGroups(groupVoList);
            }

        }, threadPoolExecutor);
        
        CompletableFuture.allOf(cateFuture,brandFuture,spuFuture,itemSaleFuture,wareSkuFuture,skuImageFuture,
                saleAttrFuture,skuAttrValueFuture,skuJsonsFuture,spuDescFuture,groupFuture).join();


        this.asyncMethod(itemVo);

        return itemVo;
    }

    //异步执行生成静态页面
    public void asyncMethod(ItemVo itemVo){
        this.threadPoolExecutor.execute(()->{
            this.generateHtml(itemVo);
        });
    }



    //生成静态页面
    public void generateHtml(ItemVo itemVo){
        //模板引擎的上下文对象，通过模板引擎传递动态数据
        Context context = new Context();
        context.setVariable("itemVo",itemVo);

        try ( PrintWriter printWriter = new PrintWriter("D:\\JavaEE\\谷粒商城\\Test\\html\\"+itemVo.getSkuId()+".html")){
            //模板引擎生成静态页面， 网站模板名称   上下文对象   文件流
            this.templateEngine.process("item",context,printWriter);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
