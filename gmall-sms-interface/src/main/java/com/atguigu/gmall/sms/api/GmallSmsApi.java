package com.atguigu.gmall.sms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface GmallSmsApi {
    /**
     * 添加营销信息
     */
    @PostMapping("/sms/skubounds/skusale/save")
    public ResponseVo<SkuSaleVo> skuSaleInfo(@RequestBody SkuSaleVo skuSaleVo);

    /**
     * 根据skuId查询营销信息
     */
    @GetMapping("sms/skubounds/salesItem/{skuId}")
    public ResponseVo<List<ItemSaleVo>> queryItemSalesBySkuId(@PathVariable("skuId")Long skuId);
}
