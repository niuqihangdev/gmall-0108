package com.atguigu.gmall.pms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.vo.SaleAttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import org.springframework.util.CollectionUtils;
import sun.reflect.misc.ConstructorUtil;


@Service("skuAttrValueService")
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValueEntity> implements SkuAttrValueService {

    @Autowired
    AttrMapper attrMapper;
    @Autowired
    SkuMapper skuMapper;
    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;
    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

    /**
     * 根据categoryId和skuId获取属性信息
     * @param categoryId
     * @param skuId
     * @return
     */
    @Override
    public List<SkuAttrValueEntity> querySearchBySkuId(Long categoryId, Long skuId) {
        List<AttrEntity> attrs = attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("category_id", categoryId)
                .eq("search_type", 1));
        if(CollectionUtils.isEmpty(attrs)){
            return  null;
        }
        List<Long> attrIds = attrs.stream().map(AttrEntity::getId).collect(Collectors.toList());

        return this.list(new QueryWrapper<SkuAttrValueEntity>().eq("sku_id",skuId).in("attr_id",attrIds));
    }

    /**
     * ***根据spuId查询销售属性信息
     */
    @Override
    public List<SaleAttrVo> querySaleAttrVoBySpuId(Long spuId) {
        //根据spuId查询sku集合信息
        List<SkuEntity> skuEntities = this.skuMapper.selectList(new QueryWrapper<SkuEntity>().eq("spu_id", spuId));
        if(CollectionUtils.isEmpty(skuEntities)){
            return null;
        }
        //获取sku集合中所有的skuid放入一个集合中
        List<Long> skuIdList = skuEntities.stream().map(SkuEntity::getId).collect(Collectors.toList());
        //通过skuId获取销售信息
        List<SkuAttrValueEntity> skuAttrValueEntities = this.list(new QueryWrapper<SkuAttrValueEntity>()
                .in("sku_id", skuIdList)
                .orderByAsc("attr_id"));
        if(CollectionUtils.isEmpty(skuAttrValueEntities)){
            return null;
        }
        //将销售属性处理成
        //[{attrId:'3',attrName:'机身颜色',attrValue:['黑色','白色']},
        // {attrId:'4',attrName:'内存',attrValue:['8g','12g']},
        // {attrId:'5',attrName:'存储',attrValue:['128g','256g']},]

        //分组以attrId作为key，以attrId对应的四条数据作为value放入map集合
        Map<Long, List<SkuAttrValueEntity>> map = skuAttrValueEntities.stream()
                .collect(Collectors.groupingBy(SkuAttrValueEntity::getAttrId));
        List<SaleAttrVo> saleAttrVos = new ArrayList<>();
        map.forEach((attrId,skuAttrValueEntityList)->{
            //需要把每一个kv结构转换成一个saleAttrVo的数据模型
            SaleAttrVo saleAttrVo = new SaleAttrVo();
            saleAttrVo.setAttrId(attrId);
            //如果存在这样的kv结构，这组数组如果存在，从中取出第一条数据
            saleAttrVo.setAttrName(skuAttrValueEntityList.get(0).getAttrName());
            saleAttrVo.setAttrValue(skuAttrValueEntityList.stream()
                    .map(SkuAttrValueEntity::getAttrValue).collect(Collectors.toSet()));
            saleAttrVos.add(saleAttrVo);
        });
        return saleAttrVos;
    }

    /**
     * ***根据spuId查询json信息
     */
    @Override
    public String queryMappingBySpuId(Long spuId) {
        //查询所有的sku信息
        List<SkuEntity> skuEntities = this.skuMapper.selectList(new QueryWrapper<SkuEntity>().eq("spu_id", spuId));
        if(CollectionUtils.isEmpty(skuEntities)){
            return null;
        }
        //查询sku所有的id信息
        List<Long> skuIds = skuEntities.stream().map(SkuEntity::getId).collect(Collectors.toList());
        //查询映射信息
        List<Map<String,Object>> maps=this.skuAttrValueMapper.queryMappingBySpuId(skuIds);
        if(CollectionUtils.isEmpty(maps)){
            return null;
        }
        //将list集合通过流的形式放入map集合中
        Map<String, Long> mapper = maps.stream().collect(Collectors.toMap(map -> map.get("attr_values").toString(), map ->(Long)map.get("sku_id")));
        //将map序列化成一个json字符串
        return JSON.toJSONString(mapper);

    }


}