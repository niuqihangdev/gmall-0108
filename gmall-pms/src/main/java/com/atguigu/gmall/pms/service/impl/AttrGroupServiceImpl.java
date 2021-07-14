package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.mapper.SpuAttrValueMapper;
import com.atguigu.gmall.pms.vo.AttrValueVo;
import com.atguigu.gmall.pms.vo.GroupVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.AttrGroupMapper;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrMapper attrMapper;
    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    SpuAttrValueMapper spuAttrValueMapper;
    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<AttrGroupEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageResultVo(page);
    }

    /**
     * 查询spu属性通过categoryId
     * @param categoryId
     * @return
     */
    @Override
    public List<AttrGroupEntity> queryGroupWithAttrByCid(Long categoryId) {
        //查询AttrGroup中所有的属性
        List<AttrGroupEntity> attrGroups=this.list(
                new QueryWrapper<AttrGroupEntity>().eq("category_id",categoryId));
        //判断AttrGroups是否为空
        if(CollectionUtils.isEmpty(attrGroups)){
            return null;
        }
        //遍历查询AttrGroup中Attr的信息
        attrGroups.forEach(attrGroup->{
            List<AttrEntity> attrs=attrMapper.selectList(
                    new QueryWrapper<AttrEntity>().eq("group_id",attrGroup.getId())
                            .eq("type",1)//查询type为1的销售属性)
            );
            attrGroup.setAttrEntities(attrs);
        });
        return attrGroups;
    }

    /**
     * ***根据分类id，spuId，skuId查询所有的规格参数
     */
    @Override
    public List<GroupVo> queryAttrGroupByCidAndSpuIdAndSkuId(Long categoryId, Long spuId, Long skuId) {
        //通过分类id查询所有的属性分组
        List<AttrGroupEntity> attrGroupEntityList = this.list(new QueryWrapper<AttrGroupEntity>().eq("category_id", categoryId));
        if(CollectionUtils.isEmpty(attrGroupEntityList)){
            return null;
        }
        //遍历属性分组下的所有参数
        List<GroupVo> groupVos=attrGroupEntityList.stream().map(attrGroupEntity -> {
            GroupVo groupVo = new GroupVo();
            groupVo.setName(attrGroupEntity.getName());
            Long groupId = attrGroupEntity.getId();
            //获取规格属性信息
            List<AttrEntity> attrEntities = this.attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("group_id", groupId));
            if(!CollectionUtils.isEmpty(attrEntities)){
                List<AttrValueVo> attrValueVos=attrEntities.stream().map(attrEntity -> {
                    AttrValueVo attrValueVo = new AttrValueVo();
                    attrValueVo.setAttrId(attrEntity.getId());
                    attrValueVo.setAttrName(attrEntity.getName());
                    //根据type判断value为营销属性还是基本属性
                    if(attrEntity.getType()==1){
                        //基本属性，查询spuAttrValue
                        SpuAttrValueEntity spuAttrValueEntity = this.spuAttrValueMapper.selectOne(new QueryWrapper<SpuAttrValueEntity>()
                                .eq("spu_id", spuId).eq("attr_id", attrEntity.getId()));
                        attrValueVo.setAttrValue(spuAttrValueEntity.getAttrValue());
                    }else{
                        //销售属性，查询skuAttrValue
                        SkuAttrValueEntity skuAttrValueEntity = this.skuAttrValueMapper.selectOne(new QueryWrapper<SkuAttrValueEntity>()
                                .eq("sku_id", skuId).eq("attr_id", attrEntity.getId()));
                        attrValueVo.setAttrValue(skuAttrValueEntity.getAttrValue());
                    }
                    return attrValueVo;
                }).collect(Collectors.toList());

                groupVo.setAttrs(attrValueVos);
            }

            return groupVo;
        }).collect(Collectors.toList());
        return groupVos;
    }

}