package com.atguigu.gmall.search.vo;

import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import lombok.Data;

import java.util.List;

@Data
public class SearchResponseAttrValueVo {
    private Long attrId;

    private String attrName;

    private List<String> attrValues;
}
