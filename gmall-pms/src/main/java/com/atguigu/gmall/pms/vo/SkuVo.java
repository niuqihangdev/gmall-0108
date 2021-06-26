package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuVo extends SkuEntity {
    //图片集合
    private List<String> images;

    //积分表
    private BigDecimal growBounds;
    private BigDecimal buyBounds;
    private List<Integer> work;

    //满减
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private Integer fullAddOther;

    //折扣优惠
    private Integer fullCount;
    private BigDecimal discount;
    private Integer ladderAddOther;

    //商品属性
    private List<SkuAttrValueEntity> saleAttrs;
}
