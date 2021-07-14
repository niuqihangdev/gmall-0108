package com.atguigu.gmall.pms.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.CategoryMapper;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryMapper categoryMapper;
    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<CategoryEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageResultVo(page);
    }

    /**
     * 分类查询
     * @param parentId
     * @return
     */
    @Override
    public List<CategoryEntity> queryCategoryEntityByPid(Long parentId) {
        if(parentId==-1){
            return this.list();
        }
        QueryWrapper<CategoryEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id",parentId);
        return this.list(queryWrapper);
    }

    /**
     * 查询二级、三级标题
     * @param pid
     * @return
     */
    @Override
    public List<CategoryEntity> queryLvl2ByPid(Long pid) {
        return this.categoryMapper.queryLvl2ByPid(pid);
    }

    /**
     * ***根据三级分类Id，查询一二三级分类
     */
    @Override
    public List<CategoryEntity> queryLv123ByCid(Long categoryId) {
        //获取三级分类信息
        CategoryEntity category3 = this.getById(categoryId);
        if(category3==null){
            return null;
        }
        //获取二级分类信息
        CategoryEntity category2 = this.getById(category3.getParentId());
        //获取一级分类信息
        CategoryEntity category1 = this.getById(category2.getParentId());

        return Arrays.asList(category3,category2,category1);
    }

}