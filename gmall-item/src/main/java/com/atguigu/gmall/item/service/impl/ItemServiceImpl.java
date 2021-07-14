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
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    GmallPmsClient pmsClient;
    @Autowired
    GmallWmsClient wmsClient;
    @Autowired
    GmallSmsClient smsClient;

    @Override
    public ItemVo lodeData(Long skuId) {
        ItemVo itemVo = new ItemVo();
        SkuEntity skuEntity = this.pmsClient.querySkuById(skuId).getData();
        if(skuEntity==null){
            throw new ItemException("该skuId对应的商品信息不存在");
        }
        //商品详情信息
        itemVo.setSkuId(skuEntity.getId());
        itemVo.setTitle(skuEntity.getTitle());
        itemVo.setSubtitle(skuEntity.getSubtitle());
        itemVo.setPrice(skuEntity.getPrice());
        itemVo.setWeight(skuEntity.getWeight());
        itemVo.setDefaultImage(skuEntity.getDefaultImage());

        //一二三级分类信息
        Long categoryId = skuEntity.getCategoryId();
        List<CategoryEntity> categoryEntities = this.pmsClient.queryLv123ByCid(categoryId).getData();
        if(CollectionUtils.isEmpty(categoryEntities)){
            throw new ItemException("该categoryId对应的分类信息不存在");
        }
        itemVo.setCategories(categoryEntities);

        //品牌信息
        BrandEntity brandEntity = this.pmsClient.queryBrandById(skuEntity.getBrandId()).getData();
        if(brandEntity==null){
            throw new ItemException("该品牌Id对应的品牌信息不存在");
        }
        itemVo.setBrandId(brandEntity.getId());
        itemVo.setBrandName(brandEntity.getName());

        //spu信息
        SpuEntity spuEntity = this.pmsClient.querySpuById(skuEntity.getSpuId()).getData();
        if(spuEntity==null){
            throw new ItemException("该spuId对应的商品信息不存在");
        }
        Long spuId = spuEntity.getId();
        itemVo.setSpuId(spuId);
        itemVo.setSpuName(spuEntity.getName());

        //营销信息
        List<ItemSaleVo> itemSaleVos = this.smsClient.queryItemSalesBySkuId(skuId).getData();
        if(CollectionUtils.isEmpty(itemSaleVos)){
            throw new ItemException("该skuId对应的营销信息不存在");
        }
        itemVo.setSales(itemSaleVos);

        //库存信息
        List<WareSkuEntity> wareSkuEntities = this.wmsClient.queryWareskuBySkuId(skuId).getData();
        if(CollectionUtils.isEmpty(wareSkuEntities)){
            throw new ItemException("该skuId对应的库存信息不存在");
        }
        itemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity ->
            wareSkuEntity.getStock()-wareSkuEntity.getStockLocked()>0
        ));

        //sku图片列表
        List<SkuImagesEntity> skuImagesEntities = this.pmsClient.queryImagesBySkuId(skuId).getData();
        if(CollectionUtils.isEmpty(skuImagesEntities)){
            throw new ItemException("该skuId对应的图片列表不存在");
        }
        itemVo.setImages(skuImagesEntities);

        //销售属性列表
        List<SaleAttrVo> saleAttrVos = this.pmsClient.querySaleAttrVoBySpuId(spuId).getData();
        if(CollectionUtils.isEmpty(saleAttrVos)){
            throw new ItemException("该spuId对应的销售属性不存在");
        }
        itemVo.setSaleAttrs(saleAttrVos);

        //当前sku的销售属性
        List<SkuAttrValueEntity> skuAttrValueEntities = this.pmsClient.querySkuAttrValueBySkuId(skuId).getData();
        if(CollectionUtils.isEmpty(skuAttrValueEntities)){
            throw new ItemException("该skuId对应的销售属性不存在");
        }
        Map<Long, String> skuAttrValueMap = skuAttrValueEntities.stream().collect(Collectors.toMap(SkuAttrValueEntity::getAttrId, SkuAttrValueEntity::getAttrValue));
        itemVo.setSaleAttr(skuAttrValueMap);


        //映射关系
        String json = this.pmsClient.queryMappingBySpuId(spuId).getData();
        if(StringUtils.isEmpty(json)){
            throw new ItemException("该spuId对应的映射关系不存在");
        }
        itemVo.setSkuJsons(json);

        //商品描述
        SpuDescEntity spuDescEntity = this.pmsClient.querySpuDescById(spuId).getData();
        if(spuDescEntity==null){
            throw new ItemException("该spuId对应的商品描述不存在");
        }
        itemVo.setSpuImages(Arrays.asList(StringUtils.split(spuDescEntity.getDecript(),",")));

        //规格参数
        List<GroupVo> groupVoList = this.pmsClient.queryAttrGroupByCidAndSpuIdAndSkuId(categoryId, spuId, skuId).getData();
        if(CollectionUtils.isEmpty(groupVoList)){
            throw new ItemException("该规格参数信息不存在");
        }
        itemVo.setGroups(groupVoList);

        return itemVo;
    }
}
