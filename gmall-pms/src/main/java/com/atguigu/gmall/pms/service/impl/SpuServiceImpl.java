package com.atguigu.gmall.pms.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuMapper;
import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.pms.service.SpuService;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    /**
     * 查询商品信息
     * @param pageParamVo
     * @param categoryId
     * @return
     */
    @Override
    public PageResultVo querySpuByCategoryId(PageParamVo pageParamVo, Long categoryId) {
        QueryWrapper<SpuEntity> queryWrapper = new QueryWrapper<>();
        if(categoryId!=0){
            //如果id不等于0，根据id查询，否则全查
            queryWrapper.eq("category_id",categoryId);
        }
        //获取路径中的关键字
        String key = pageParamVo.getKey();
        //相当于SELECT * FROM pms_spu WHERE category_id=225 AND (id=7 OR `name` LIKE '%7%');
        queryWrapper.and(t->t.eq("id",key).or().like("name",key));
        //分页查询
        IPage<SpuEntity> page = this.page(
                pageParamVo.getPage(),
               queryWrapper
        );
        return new PageResultVo(page);
    }

}