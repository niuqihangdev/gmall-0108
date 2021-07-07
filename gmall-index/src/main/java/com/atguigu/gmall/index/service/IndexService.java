package com.atguigu.gmall.index.service;

import com.atguigu.gmall.pms.entity.CategoryEntity;

import java.util.List;

public interface IndexService {
    List<CategoryEntity> queryLvl1Categories();

    List<CategoryEntity> queryLvl2Categories(Long pid);
}
