package com.atguigu.gmall.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;

import java.util.Map;

/**
 * εεεΊε­
 *
 * @author niuqihang
 * @email 1208936851@qq.com
 * @date 2021-06-23 23:58:29
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

