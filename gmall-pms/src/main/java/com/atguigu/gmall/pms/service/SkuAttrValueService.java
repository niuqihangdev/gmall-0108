package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.SaleAttrVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author niuqihang
 * @email 1208936851@qq.com
 * @date 2021-06-22 18:40:50
 */
public interface SkuAttrValueService extends IService<SkuAttrValueEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<SkuAttrValueEntity> querySearchBySkuId(Long categoryId, Long skuId);

    List<SaleAttrVo> querySaleAttrVoBySpuId(Long spuId);

    String queryMappingBySpuId(Long spuId);
}

