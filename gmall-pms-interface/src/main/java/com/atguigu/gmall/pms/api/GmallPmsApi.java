package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrVo;
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
     * 根据spuId查询spuEntity
     * @param id
     * @return
     */
    @GetMapping("pms/spu/{id}")
    public ResponseVo<SpuEntity> querySpuById(@PathVariable("id") Long id);



    /**
     * 根据spuId查询sku信息
     * @param spuId
     * @return
     */
    @GetMapping("pms/sku/spu/{spuId}")
    public ResponseVo<List<SkuEntity>> querySkuBySpuId(@PathVariable("spuId") Long spuId);

    /**
     * 根据skuId查询sku信息
     */
    @GetMapping("pms/sku/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<SkuEntity> querySkuById(@PathVariable("id") Long id);




    /**
     * 根据品牌id查询品牌
     * @param id
     * @return
     */
    @GetMapping("pms/brand/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);




    /**
     * 根据categoryId和skuId获取属性信息
     */
    @GetMapping("pms/skuattrvalue/search/{categoryId}")
    public ResponseVo<List<SkuAttrValueEntity>> querySearchBySkuId(
            @PathVariable("categoryId")Long categoryId,
            @RequestParam(value = "skuId")Long skuId);

    /**
     * 根据categoryId和spuId获取属性信息
     */
    @GetMapping("pms/spuattrvalue/search/{categoryId}")
    public ResponseVo<List<SpuAttrValueEntity>> querySearchBySpuId(
            @PathVariable("categoryId") Long categoryId,
            @RequestParam("spuId") Long spuId);

    /**
     * ***根据spuId查询销售属性信息
     */
    @GetMapping("pms/skuattrvalue/spu/{spuId}")
    public ResponseVo<List<SaleAttrVo>> querySaleAttrVoBySpuId(@PathVariable("spuId")Long spuId);

    /**
     * ***根据skuId查询销售属性信息
     */
    @GetMapping("pms/skuattrvalue/sku/{skuId}")
    public ResponseVo<List<SkuAttrValueEntity>> querySkuAttrValueBySkuId(@PathVariable("skuId")Long skuId);

    /**
     * ***根据spuId查询json信息
     */
    @GetMapping("pms/skuattrvalue/mapping/{spuId}")
    public ResponseVo<String> queryMappingBySpuId(@PathVariable("spuId")Long spuId);




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

    /**
     * 根据分类id查询分类信息
     * @param id
     * @return
     */
    @GetMapping("pms/category/{id}")
    public ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);

    /**
     * 根据三级分类Id，查询一二三级分类
     */
    @GetMapping("pms/category/sub/{categoryId}")
    public ResponseVo<List<CategoryEntity>> queryLv123ByCid(@PathVariable("categoryId")Long categoryId);

    /**
     * ***根据分类id，spuId，skuId查询所有的规格参数
     */
    @GetMapping("pms/attrgroup/withCPK/{categoryId}")
    public ResponseVo<List<GroupVo>> queryAttrGroupByCidAndSpuIdAndSkuId(@PathVariable("categoryId")Long categoryId,
                                                                         @RequestParam("spuId")Long spuId, @RequestParam("skuId") Long skuId);




    /**
     * 根据skuId查询图片信息
     */
    @GetMapping("pms/skuimages/images/{skuId}")
    public ResponseVo<List<SkuImagesEntity>> queryImagesBySkuId(@PathVariable("skuId")Long skuId);



    /**
     * 根据spuId查询描述信息
     */
    @GetMapping("pms/spudesc/{spuId}")
    public ResponseVo<SpuDescEntity> querySpuDescById(@PathVariable("spuId") Long spuId);





}


