package com.atguigu.gmall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.service.SearchService;
import com.atguigu.gmall.search.vo.SearchParamVo;
import com.atguigu.gmall.search.vo.SearchResponseAttrValueVo;
import com.atguigu.gmall.search.vo.SearchResponseVo;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("searchService")
public class SearchServiceImpl implements SearchService {
    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Override
    public SearchResponseVo search(SearchParamVo searchParamVo) {
        try {
            SearchResponse response = this.restHighLevelClient.search(
                    new SearchRequest(new String[]{"goods"},builderDsl(searchParamVo)), RequestOptions.DEFAULT);
            //解析结果集
            System.out.println(response);
            SearchResponseVo responseVo = this.parseResult(response);
            //设置分页参数
            responseVo.setPageNum(searchParamVo.getPageNum());
            responseVo.setPageSize(searchParamVo.getPageSize());
            return responseVo;


        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解析结果集
     */
    private SearchResponseVo parseResult(SearchResponse response){
        SearchResponseVo responseVo = new SearchResponseVo();
        //获取hits结果集
        SearchHits hits = response.getHits();
        SearchHit[] hitsHits = hits.getHits();
        List<Goods> goodsList=Arrays.stream(hitsHits).map(hitsHit->{
            //json反序列化Goods对象
            String json = hitsHit.getSourceAsString();//获取_source
            Goods goods = JSON.parseObject(json, Goods.class);
            //获取高亮标题，代替原来的标题
            Map<String, HighlightField> highlightFields = hitsHit.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            goods.setTitle(title.fragments()[0].string());
            return goods;
        }).collect(Collectors.toList());
        responseVo.setGoodsList(goodsList);

        //获取聚合结果集
        Aggregations aggregations = response.getAggregations();
        //获取聚合中brandId聚合
        ParsedLongTerms brandIdAgg = (ParsedLongTerms)aggregations.get("brandIdAgg");
        List<? extends Terms.Bucket> brandBuckets = brandIdAgg.getBuckets();//获取brandId桶
        if(!CollectionUtils.isEmpty(brandBuckets)){
            List<BrandEntity> brandEntities = brandBuckets.stream().map(brandBucket->{//将每个桶转换为品牌
                BrandEntity brandEntity = new BrandEntity();
                brandEntity.setId(((Terms.Bucket)brandBucket).getKeyAsNumber().longValue());
                
                //获取品牌中的子聚合
                Aggregations subAggregations = ((Terms.Bucket)brandBucket).getAggregations();
                ParsedStringTerms brandNameAgg = (ParsedStringTerms) subAggregations.get("brandNameAgg");
                List<? extends Terms.Bucket> brandNameBuckets = brandNameAgg.getBuckets();
                //获取桶中的name信息
                if(!CollectionUtils.isEmpty(brandNameBuckets)){
                    brandEntity.setName(brandNameBuckets.get(0).getKeyAsString());
                }

                //获取品牌中的logo信息
                ParsedStringTerms brandLogoAgg=(ParsedStringTerms)subAggregations.get("logoAgg");
                List<? extends Terms.Bucket> logoBuckets = brandLogoAgg.getBuckets();
                if(!CollectionUtils.isEmpty(logoBuckets)){
                    brandEntity.setLogo(logoBuckets.get(0).getKeyAsString());
                }
                return brandEntity;
            }).collect(Collectors.toList());
            responseVo.setBrands(brandEntities);
        }
        //获取categoryIdAgg中的聚合
        ParsedLongTerms categoryIdAgg = (ParsedLongTerms)aggregations.get("categoryIdAgg");
        List<? extends Terms.Bucket> categoryIdAggBuckets = categoryIdAgg.getBuckets();
        if(!CollectionUtils.isEmpty(categoryIdAggBuckets)){
            List<CategoryEntity> categoryEntities=categoryIdAggBuckets.stream().map(categoryIdBucket->{
                //设置categoryId
                CategoryEntity categoryEntity = new CategoryEntity();
                categoryEntity.setId(categoryIdBucket.getKeyAsNumber().longValue());
                Aggregations categoryNameAggregations = categoryIdBucket.getAggregations();
                ParsedStringTerms categoryNameAgg= (ParsedStringTerms)categoryNameAggregations.get("categoryNameAgg");
                List<? extends Terms.Bucket> categoryNameBuckets = categoryNameAgg.getBuckets();
                if(!CollectionUtils.isEmpty(categoryNameBuckets)){
                    categoryEntity.setName(categoryNameBuckets.get(0).getKeyAsString());
                }
                return categoryEntity;
            }).collect(Collectors.toList());
            responseVo.setCategories(categoryEntities);
        }

        //获取规格参数的的聚合，解析聚合列表
        ParsedNested attrAgg=(ParsedNested)aggregations.get("attrAgg");
        //获取嵌套集合中规格参数id的聚合
        ParsedLongTerms attrIdAgg= (ParsedLongTerms)attrAgg.getAggregations().get("attrIdAgg");
        //获取id聚合中的桶
        List<? extends Terms.Bucket> attrIdAggBuckets = attrIdAgg.getBuckets();
        if(!CollectionUtils.isEmpty(attrIdAggBuckets)){
            List<SearchResponseAttrValueVo> searchResponseAttrValueVos=attrIdAggBuckets.stream().map(attrIdAggBucket->{
                SearchResponseAttrValueVo searchResponseAttrValueVo = new SearchResponseAttrValueVo();
                //桶中的key就是attrId
                searchResponseAttrValueVo.setAttrId(((Terms.Bucket)attrIdAggBucket).getKeyAsNumber().longValue());
                //获取attrId中的子聚合
                Aggregations attrAggregations = ((Terms.Bucket)attrIdAggBucket).getAggregations();
                //获取attrName的聚合
                ParsedStringTerms attrNameAgg=(ParsedStringTerms)attrAggregations.get("attrNameAgg");
                //获取attrName的桶
                List<? extends Terms.Bucket> attrNameAggBuckets = attrNameAgg.getBuckets();
                if(!CollectionUtils.isEmpty(attrNameAggBuckets)){
                    searchResponseAttrValueVo.setAttrName(attrNameAggBuckets.get(0).getKeyAsString());
                }
                //获取attrValue的聚合
                ParsedStringTerms attrValueAgg=(ParsedStringTerms)attrAggregations.get("attrValueAgg");
                //获取attrValue的桶
                List<? extends Terms.Bucket> attrValueAggBuckets = attrValueAgg.getBuckets();
                if(!CollectionUtils.isEmpty(attrValueAggBuckets)){
                    List<String> attrValues = attrValueAggBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
                    searchResponseAttrValueVo.setAttrValues(attrValues);
                }
                return searchResponseAttrValueVo;
            }).collect(Collectors.toList());
            responseVo.setFilters(searchResponseAttrValueVos);
        }


        //设置总记录数
        long totalHits = hits.getTotalHits();
        responseVo.setTotal(totalHits);



        return responseVo;
    }
    /**
     * 搜索功能
     * @param searchParamVo
     * @return
     */
    private SearchSourceBuilder builderDsl(SearchParamVo searchParamVo){

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        String keyword = searchParamVo.getKeyword();
        if(StringUtils.isBlank(keyword)){
            return sourceBuilder;
        }

        //1、构建查询以及过滤
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        sourceBuilder.query(boolQueryBuilder);
        //1.1、匹配查询
        boolQueryBuilder.must(QueryBuilders.matchQuery("title",keyword).operator(Operator.AND));

        //1.2、过滤
        //1.2.1、品牌过滤
        List<Long> brandId = searchParamVo.getBrandId();
        if(!CollectionUtils.isEmpty(brandId)){
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId",brandId));
        }

        //1.2.2、分类过滤
        List<Long> categoryId = searchParamVo.getCategoryId();
        if(!CollectionUtils.isEmpty(categoryId)){
            boolQueryBuilder.filter(QueryBuilders.termsQuery("categoryId",categoryId));
        }


        //1.2.3、价格区间过滤
        Double priceFrom = searchParamVo.getPriceFrom();
        Double priceTo = searchParamVo.getPriceTo();
        if(priceFrom!=null||priceTo!=null){
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("price");//构建price的范围查询
            if(priceFrom!=null){
                rangeQuery.gte(priceFrom);
            }
            if(priceTo!=null){
                rangeQuery.lte(priceTo);
            }
            boolQueryBuilder.filter(rangeQuery);
        }

        //1.2.4、仅显示有货过滤
        Boolean store = searchParamVo.getStore();
        if(store!=null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("store",store));
        }

        //1.2.5、规格参数过滤
        List<String> props = searchParamVo.getProps();
        if(!CollectionUtils.isEmpty(props)){
            props.forEach(prop->{
                String[] attr = StringUtils.split(prop, ":");
                if(attr!=null&&attr.length==2){
                    //使用分割工具类将获取的参数分开 4:8G-12G
                    String attrId=attr[0];
                    String attrValueString = attr[1];
                    String[] attrValues = StringUtils.split(attrValueString, "-");
                    //将参数放入
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    boolQuery.must(QueryBuilders.termQuery("searchAttrs.attrId",attrId));
                    boolQuery.must(QueryBuilders.termsQuery("searchAttrs.attrValue",attrValues));
                    boolQueryBuilder.filter(QueryBuilders.nestedQuery("searchAttrs",boolQuery,ScoreMode.None));
                }
                
            });
        }

        //2、构建排序
        Integer sort = searchParamVo.getSort();
        switch (sort){
            case 0: sourceBuilder.sort("_score", SortOrder.DESC); break;
            case 1: sourceBuilder.sort("price", SortOrder.DESC); break;
            case 2: sourceBuilder.sort("price", SortOrder.ASC); break;
            case 3: sourceBuilder.sort("sales", SortOrder.DESC); break;
            case 4: sourceBuilder.sort("createTime", SortOrder.DESC); break;
            default:
                throw new RuntimeException("您的搜索条件不合法！");
        }


        //3、分页
        Integer pageNum = searchParamVo.getPageNum();
        Integer pageSize = searchParamVo.getPageSize();
        sourceBuilder.from((pageNum-1)*pageSize);
        sourceBuilder.size(pageSize);

        //4、高亮
        sourceBuilder.highlighter(new HighlightBuilder().field("title").preTags("<font style='color:red;'>").postTags("</font>"));

        //5、聚合
        //5.1、品牌聚合
        sourceBuilder.aggregation(AggregationBuilders.terms("brandIdAgg").field("brandId")
                        .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName"))
                        .subAggregation(AggregationBuilders.terms("logoAgg").field("logo")));
        //5.2、分类聚合
        sourceBuilder.aggregation(AggregationBuilders.terms("categoryIdAgg").field("categoryId")
                        .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName")));

        //5.3、规格参数聚合
        sourceBuilder.aggregation(AggregationBuilders.nested("attrAgg", "searchAttrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("searchAttrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("searchAttrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("searchAttrs.attrValue"))
                )
        );

        //6、结果集过滤
        sourceBuilder.fetchSource(new String[]{"skuId","title","subTitle","price","defaultImage"},null);

        System.out.println(sourceBuilder);
        return sourceBuilder;

    }
}
