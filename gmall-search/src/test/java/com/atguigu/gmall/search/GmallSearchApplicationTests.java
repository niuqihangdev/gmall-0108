package com.atguigu.gmall.search;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.search.vo.SearchAttrValueVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import io.jsonwebtoken.lang.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;
import java.util.stream.Collectors;

@SpringBootTest
class GmallSearchApplicationTests {
	@Autowired
	private ElasticsearchRestTemplate restTemplate;
	@Autowired
	private GmallPmsClient pmsClient;
	@Autowired
	private GmallWmsClient wmsClient;
	@Autowired
	GoodsRepository goodsRepository;

	@Test
	void contextLoads() {
		if(!this.restTemplate.indexExists(Goods.class)){
			restTemplate.createIndex(Goods.class);
			restTemplate.putMapping(Goods.class);
		}
		Integer pageNum=1;
		Integer pageSize=100;
		do{
			//获取查询分页信息,第一页，每页100条数据
			PageParamVo paramVo = new PageParamVo(pageNum, pageSize, null);
			ResponseVo<List<SpuEntity>> spuResponse = this.pmsClient.querySpuByPageJson(paramVo);
			List<SpuEntity> spuEntities = spuResponse.getData();
			if(CollectionUtils.isEmpty(spuEntities)){
				return;
			}

			spuEntities.forEach(spuEntity -> {
				//查询品牌信息
				ResponseVo<BrandEntity> brandEntityResponseVo = pmsClient.queryBrandById(spuEntity.getBrandId());
				BrandEntity brandEntity= brandEntityResponseVo.getData();
				//查询分类信息
				ResponseVo<CategoryEntity> categoryEntityResponseVo = pmsClient.queryCategoryById(spuEntity.getCategoryId());
				CategoryEntity categoryEntity = categoryEntityResponseVo.getData();

				//查询spu下的sku信息
				ResponseVo<List<SkuEntity>> skuResponse = this.pmsClient.querySkuBySpuId(spuEntity.getId());
				List<SkuEntity> skuEntities = skuResponse.getData();
				if(!Collections.isEmpty(skuEntities)){
					List<Goods> goodsList = skuEntities.stream().map(skuEntity -> {
						//将sku信息保存到Goods对象中
						Goods goods=new Goods();
						goods.setSkuId(skuEntity.getId());
						goods.setDefaultImage(skuEntity.getDefaultImage());
						goods.setTitle(skuEntity.getTitle());
						goods.setSubTitle(skuEntity.getSubtitle());
						goods.setPrice(skuEntity.getPrice().doubleValue());

						//通过sku查询库存信息
						goods.setCreateTime(spuEntity.getCreateTime());
						ResponseVo<List<WareSkuEntity>> wmsSkuResponse = wmsClient.queryWareskuBySkuId(skuEntity.getId());
						List<WareSkuEntity> wareSkuEntities = wmsSkuResponse.getData();
						if(!CollectionUtils.isEmpty(wareSkuEntities)){
							goods.setSales(wareSkuEntities.stream().mapToLong(WareSkuEntity::getSales).reduce((a, b) -> a + b).getAsLong());
							goods.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock()-wareSkuEntity.getStockLocked()>0));
						}

						//将品牌信息存入goods中
						if(brandEntity!=null){
							goods.setBrandId(brandEntity.getId());
							goods.setBrandName(brandEntity.getName());
							goods.setLogo(brandEntity.getLogo());
						}

						//将分类信息存入goods中
						if(categoryEntity!=null){
							goods.setCategoryId(categoryEntity.getId());
							goods.setCategoryName(categoryEntity.getName());
						}

						//查询SkuAttrValue 信息 SpuAttrValue 将信息放入集合中存入goods
						List<SearchAttrValueVo> searchAttrValueVos = new ArrayList<>();
						ResponseVo<List<SkuAttrValueEntity>> skuAttrResponse = pmsClient.querySearchBySkuId(spuEntity.getCategoryId(), skuEntity.getId());
						List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrResponse.getData();
						//将skuAttrValue 复制给SearchAttrValueVo对象存入集合中
						if(!CollectionUtils.isEmpty(skuAttrValueEntities)){
							List<SearchAttrValueVo> skuList=skuAttrValueEntities.stream().map(skuAttrValueEntity ->{
										SearchAttrValueVo search=new SearchAttrValueVo();
										BeanUtils.copyProperties(skuAttrValueEntity,search);
										return search;
									}
							).collect(Collectors.toList());
							searchAttrValueVos.addAll(skuList);
						}
						//spuAttrValue
						ResponseVo<List<SpuAttrValueEntity>> spuAttrResponse = pmsClient.querySearchBySpuId(spuEntity.getCategoryId(), spuEntity.getId());
						List<SpuAttrValueEntity> spuAttrValueEntities = spuAttrResponse.getData();
						if(!CollectionUtils.isEmpty(spuAttrValueEntities)){
							List<SearchAttrValueVo> spuList=spuAttrValueEntities.stream().map(spuAttrValueEntity ->{
										SearchAttrValueVo search=new SearchAttrValueVo();
										BeanUtils.copyProperties(spuAttrValueEntity,search);
										return search;
									}
							).collect(Collectors.toList());
							searchAttrValueVos.addAll(spuList);
						}

						goods.setSearchAttrs(searchAttrValueVos);

						return goods;
					}).collect(Collectors.toList());
					this.goodsRepository.saveAll(goodsList);
				}

			});


			//页码每次查询加1，若本页查询条数小于100，为最后一页，跳出循环
			pageNum++;
			pageSize=spuEntities.size();
		}while (pageSize==100);


	}

}
