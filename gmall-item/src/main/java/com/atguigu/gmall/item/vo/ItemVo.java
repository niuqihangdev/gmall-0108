package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ItemVo {

    //一二三级分类信息
    private List<CategoryEntity> categories;

    //品牌信息
    private Long brandId;
    private String brandName;

    //spu信息
    private Long spuId;
    private String spuName;

    //商品详情信息
    private Long skuId;
    private String title;
    private String subtitle;
    private BigDecimal price;
    private Integer weight;
    private String defaultImage;

    //营销信息
    private List<ItemSaleVo> sales;

    //是否有货
    private Boolean store;

    //sku的图片列表
    private List<SkuImagesEntity> images;

    //销售属性列表
    //[{attrId:'3',attrName:'机身颜色',attrValue:['黑色','白色']},
    // {attrId:'4',attrName:'内存',attrValue:['8g','12g']},
    // {attrId:'5',attrName:'存储',attrValue:['128g','256g']},]
    private List<SaleAttrVo> saleAttrs;

    //当前sku的销售属性:{3:'黑色', 4:'12g', 5:'256g'}
    private Map<Long,String> saleAttr;

    //为了页面跳转，需要销售属性组合与skuId的映射关系
    //{'白色,8g,128g':100,'白色,8g,256g':101,}
    private String skuJsons;

    //商品描述
    private List<String> spuImages;

    //规格参数分组
    private List<GroupVo> groups;
}
