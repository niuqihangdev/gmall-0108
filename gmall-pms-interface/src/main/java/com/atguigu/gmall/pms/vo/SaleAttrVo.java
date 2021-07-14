package com.atguigu.gmall.pms.vo;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class SaleAttrVo {
    private Long attrId;
    private String attrName;
    private Set<String> attrValue;
}
