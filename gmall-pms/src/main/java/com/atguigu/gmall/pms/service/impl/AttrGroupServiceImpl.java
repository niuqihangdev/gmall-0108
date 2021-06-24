package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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

}