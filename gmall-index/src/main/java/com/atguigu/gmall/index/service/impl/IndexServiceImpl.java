package com.atguigu.gmall.index.service.impl;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IndexServiceImpl implements IndexService {
    @Autowired
    GmallPmsClient pmsClient;

    /**
     * 同步查询一级标题
     * @return
     */
    @Override
    public List<CategoryEntity> queryLvl1Categories() {
        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryCategoryEntityByPid(0L);
        return listResponseVo.getData();
    }

    /**
     * 异步查询二级、三级标题
     * @param pid
     * @return
     */
    @Override
    public List<CategoryEntity> queryLvl2Categories(Long pid) {
        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryLvl2ByPid(pid);
        return listResponseVo.getData();
    }
}
