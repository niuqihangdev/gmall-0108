package com.atguigu.gmall.pms.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CategoryMapperTest {
    @Autowired
    CategoryMapper categoryMapper;

    @Test
    public void testMapper(){
        this.categoryMapper.queryLvl2ByPid(1L).forEach(System.out::println);
    }
}