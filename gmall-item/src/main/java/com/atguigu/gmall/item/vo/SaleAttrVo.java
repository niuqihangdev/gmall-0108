package com.atguigu.gmall.item.vo;

import lombok.Data;

import java.util.List;

@Data
public class SaleAttrVo {
    private Long attrId;
    private String attrName;
    private List<String> attrValue;
}
