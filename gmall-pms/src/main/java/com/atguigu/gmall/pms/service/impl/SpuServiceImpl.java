package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.service.*;
import com.atguigu.gmall.pms.vo.SkuVo;
import com.atguigu.gmall.pms.vo.SpuAttrValueVo;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuMapper;
import org.springframework.util.CollectionUtils;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {
    @Autowired
    SpuDescMapper spuDescMapper;
    @Autowired
    SpuAttrValueService spuAttrValueService;
    @Autowired
    SkuMapper skuMapper;
    @Autowired
    SkuImagesService skuImagesService;
    @Autowired
    SkuAttrValueService skuAttrValueService;
    @Autowired
    GmallSmsClient smsClient;
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    /**
     * 查询商品信息
     * @param pageParamVo
     * @param categoryId
     * @return
     */
    @Override
    public PageResultVo querySpuByCategoryId(PageParamVo pageParamVo, Long categoryId) {
        QueryWrapper<SpuEntity> queryWrapper = new QueryWrapper<>();
        if(categoryId!=0){
            //如果id不等于0，根据id查询，否则全查
            queryWrapper.eq("category_id",categoryId);
        }
        //获取路径中的关键字
        String key = pageParamVo.getKey();
        //相当于SELECT * FROM pms_spu WHERE category_id=225 AND (id=7 OR `name` LIKE '%7%');
        queryWrapper.and(t->t.eq("id",key).or().like("name",key));
        //分页查询
        IPage<SpuEntity> page = this.page(
                pageParamVo.getPage(),
                queryWrapper
        );
        return new PageResultVo(page);
    }

    /**
     * 添加商品详情信息，九表关联
     * @param spuVo
     */
    @GlobalTransactional
    @Override
    public void bigSave(SpuVo spuVo) {
        //设置spu的状态和上架时间
        spuVo.setPublishStatus(1);
        spuVo.setCreateTime(new Date());
        spuVo.setUpdateTime(spuVo.getCreateTime());
        //保存spu信息
        this.save(spuVo);
        Long spuId = spuVo.getId();

        //商品详情表
        SpuDescEntity spuDescEntity = new SpuDescEntity();
        spuDescEntity.setSpuId(spuId);
        spuDescEntity.setDecript(StringUtils.join(spuVo.getSpuImages(),","));
        this.spuDescMapper.insert(spuDescEntity);

        //商品属性表
        List<SpuAttrValueVo> baseAttrs = spuVo.getBaseAttrs();
        if(!CollectionUtils.isEmpty(baseAttrs)){
            List<SpuAttrValueEntity> spuAttrValues = baseAttrs.stream().map(spuAttrValueVo -> {
                spuAttrValueVo.setSpuId(spuId);
                spuAttrValueVo.setSort(0);
                return spuAttrValueVo;
            }).collect(Collectors.toList());
            this.spuAttrValueService.saveBatch(spuAttrValues);
        }

        //保存sku信息
        List<SkuVo> skus = spuVo.getSkus();
        if(CollectionUtils.isEmpty(skus)){
            return;
        }
        skus.forEach(skuVo->{
            SkuEntity skuEntity = new SkuEntity();
            BeanUtils.copyProperties(skuVo,skuEntity);
            skuEntity.setSpuId(spuId);
            skuEntity.setCategoryId(spuVo.getCategoryId());
            skuEntity.setBrandId(spuVo.getBrandId());
            //获取图片列表
            List<String> images = skuVo.getImages();
            if(!CollectionUtils.isEmpty(images)){
                skuEntity.setDefaultImage(skuEntity.getDefaultImage()==null
                        ?images.get(0):skuEntity.getDefaultImage());
            }
            this.skuMapper.insert(skuEntity);
            Long skuId = skuEntity.getId();

            //保存sku图片信息
            if(!CollectionUtils.isEmpty(images)){
                String defaultImage=images.get(0);
                List<SkuImagesEntity> skuImages = images.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setUrl(image);
                    skuImagesEntity.setSort(0);
                    skuImagesEntity.setDefaultStatus(StringUtils.equals(defaultImage, image) ? 1 : 0);
                    return skuImagesEntity;
                }).collect(Collectors.toList());
                this.skuImagesService.saveBatch(skuImages);
            }

            //保存sku属性信息
            List<SkuAttrValueEntity> saleAttrs = skuVo.getSaleAttrs();
            saleAttrs.forEach(saleAttr->{
                saleAttr.setSkuId(skuId);
                saleAttr.setSort(0);
            });
            this.skuAttrValueService.saveBatch(saleAttrs);

            //远程调用gmall-sms
            SkuSaleVo skuSaleVo = new SkuSaleVo();
            BeanUtils.copyProperties(skuVo,skuSaleVo);
            skuSaleVo.setSkuId(skuId);
            this.smsClient.skuSaleInfo(skuSaleVo);
        });
        this.rabbitTemplate.convertAndSend("PMS_ITEM_EXCHANGE","item.insert",spuId);//spuId 消息内容
    }

}