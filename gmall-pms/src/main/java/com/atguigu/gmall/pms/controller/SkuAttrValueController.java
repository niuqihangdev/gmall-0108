package com.atguigu.gmall.pms.controller;

import java.util.List;

import com.atguigu.gmall.pms.vo.SaleAttrVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * sku销售属性&值
 *
 * @author niuqihang
 * @email 1208936851@qq.com
 * @date 2021-06-22 18:40:50
 */
@Api(tags = "sku销售属性&值 管理")
@RestController
@RequestMapping("pms/skuattrvalue")
public class SkuAttrValueController {

    @Autowired
    private SkuAttrValueService skuAttrValueService;
    /**
     * ***根据spuId查询json信息
     */
    @GetMapping("mapping/{spuId}")
    public ResponseVo<String> queryMappingBySpuId(@PathVariable("spuId")Long spuId){
        String mapper=this.skuAttrValueService.queryMappingBySpuId(spuId);
        return ResponseVo.ok(mapper);
    }

    /**
     * ***根据skuId查询销售属性信息
     */
    @GetMapping("sku/{skuId}")
    public ResponseVo<List<SkuAttrValueEntity>> querySkuAttrValueBySkuId(@PathVariable("skuId")Long skuId){
        List<SkuAttrValueEntity> list=this.skuAttrValueService.list(new QueryWrapper<SkuAttrValueEntity>().eq("sku_id",skuId));
        return ResponseVo.ok(list);
    }

    /**
     * ***根据spuId查询销售属性信息
     */
    @GetMapping("spu/{spuId}")
    public ResponseVo<List<SaleAttrVo>> querySaleAttrVoBySpuId(@PathVariable("spuId")Long spuId){
        List<SaleAttrVo> list=this.skuAttrValueService.querySaleAttrVoBySpuId(spuId);
        return ResponseVo.ok(list);
    }

    /**
     * ***根据categoryId和skuId获取属性信息
     */
    @GetMapping("search/{categoryId}")
    @ApiOperation("根据categoryId和skuId获取属性信息")
    public ResponseVo<List<SkuAttrValueEntity>> querySearchBySkuId(
            @PathVariable("categoryId")Long categoryId,
            @RequestParam("skuId")Long skuId){
        List<SkuAttrValueEntity> list=skuAttrValueService.querySearchBySkuId(categoryId,skuId);
        return ResponseVo.ok(list);
    }

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> querySkuAttrValueByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = skuAttrValueService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<SkuAttrValueEntity> querySkuAttrValueById(@PathVariable("id") Long id){
		SkuAttrValueEntity skuAttrValue = skuAttrValueService.getById(id);

        return ResponseVo.ok(skuAttrValue);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody SkuAttrValueEntity skuAttrValue){
		skuAttrValueService.save(skuAttrValue);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody SkuAttrValueEntity skuAttrValue){
		skuAttrValueService.updateById(skuAttrValue);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		skuAttrValueService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
