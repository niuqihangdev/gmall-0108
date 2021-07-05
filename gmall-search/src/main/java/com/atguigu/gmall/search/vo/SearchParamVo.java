package com.atguigu.gmall.search.vo;

import io.swagger.models.auth.In;
import lombok.Data;

import java.util.List;

/*
* 搜索条件: search.gmall.com/search?keyword=手机&brandId=1,2,3&categoryId=225,250&props=4:8G-12G&props=5:128G-256G
* */
@Data
public class SearchParamVo {
    //查询关键字
    private String keyword;

    //品牌Id过滤
    private List<Long> brandId;

    //分类Id过滤
    private List<Long> categoryId;

    //规格参数的过滤条件["4:8G-12G","5:128G-256G"]
    private List<String> props;

    //价格区间过滤
    private Double priceFrom;
    private Double priceTo;

    //仅显示有货
    private Boolean store;

    //排序 0-默认 1-价格降序 2-价格升序 3-销量降序 4-新品降序
    private Integer sort=0;

    //分页查询
    private Integer pageNum=1;
    private final Integer pageSize=20;


}
