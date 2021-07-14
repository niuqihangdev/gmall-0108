package com.atguigu.gmall.pms.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SkuImagesMapper;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.service.SkuImagesService;


@Service("skuImagesService")
public class SkuImagesServiceImpl extends ServiceImpl<SkuImagesMapper, SkuImagesEntity> implements SkuImagesService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuImagesEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuImagesEntity>()
        );

        return new PageResultVo(page);
    }

    /**
     * ***根据skuId查询图片信息
     * @param skuId
     * @return
     */
    @Override
    public List<SkuImagesEntity> queryImagesBySkuId(Long skuId) {
        List<SkuImagesEntity> skuImagesEntities = this.list(new QueryWrapper<SkuImagesEntity>().eq("sku_id", skuId));
        return skuImagesEntities;
    }

}