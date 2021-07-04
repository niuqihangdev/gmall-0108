package com.atguigu.gmall.search.vo;

import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.search.pojo.Goods;
import lombok.Data;

import java.util.List;

@Data
public class SearchResponseVo {
    //品牌列表
    private List<BrandEntity> brands;

    //分类列表
    private List<CategoryEntity> categories;

    //规格参数过滤
    private List<SearchResponseAttrValueVo> filters;

    //分页参数
    private Long total;//总记录数
    private Integer pageNum;
    private Integer pageSize;

    //商品列表
    private List<Goods> goodsList;

}
