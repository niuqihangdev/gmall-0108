package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GmallPmsApi {
    /**
     * 分页查询spu信息
     * @param paramVo
     * @return
     */
    @PostMapping("pms/spu/page")
    public ResponseVo<List<SpuEntity>> querySpuByPageJson(@RequestBody(required = false) PageParamVo paramVo);

    /**
     * 根据spuId查询sku信息
     * @param spuId
     * @return
     */
    @GetMapping("pms/sku/spu/{spuId}")
    public ResponseVo<List<SkuEntity>> querySkuBySpuId(@PathVariable("spuId") Long spuId);

    /**
     * 根据品牌id查询品牌
     * @param id
     * @return
     */
    @GetMapping("pms/brand/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);

    /**
     * 根据分类id查询分类信息
     * @param id
     * @return
     */
    @GetMapping("pms/category/{id}")
    public ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);

    /**
     * ***根据categoryId和skuId获取属性信息
     */
    @GetMapping("pms/skuattrvalue/search/{categoryId}")
    public ResponseVo<List<SkuAttrValueEntity>> querySearchBySkuId(
            @PathVariable("categoryId")Long categoryId,
            @RequestParam(value = "skuId")Long skuId);

    /**
     * ***根据categoryId和spuId获取属性信息
     */
    @GetMapping("pms/spuattrvalue/search/{categoryId}")
    public ResponseVo<List<SpuAttrValueEntity>> querySearchBySpuId(
            @PathVariable("categoryId") Long categoryId,
            @RequestParam("spuId") Long spuId);

    /**
     * 根据spuId查询spuEntity
     * @param id
     * @return
     */
    @GetMapping("pms/spu/{id}")
    public ResponseVo<SpuEntity> querySpuById(@PathVariable("id") Long id);

    /**
     * 分类查询
     */
    @GetMapping("pms/category/parent/{parentId}")
    public ResponseVo<List<CategoryEntity>>queryCategoryEntityByPid(@PathVariable("parentId") Long parentId);

    /**
     * 查询二级、三级标题
     */
    @GetMapping("pms/category/subs/{pid}")
    public ResponseVo<List<CategoryEntity>> queryLvl2ByPid(@PathVariable("pid") Long pid);
}
