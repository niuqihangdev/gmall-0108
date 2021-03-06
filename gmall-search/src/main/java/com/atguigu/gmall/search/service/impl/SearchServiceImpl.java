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
            //???????????????
            System.out.println(response);
            SearchResponseVo responseVo = this.parseResult(response);
            //??????????????????
            responseVo.setPageNum(searchParamVo.getPageNum());
            responseVo.setPageSize(searchParamVo.getPageSize());
            return responseVo;


        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * ???????????????
     */
    private SearchResponseVo parseResult(SearchResponse response){
        SearchResponseVo responseVo = new SearchResponseVo();
        //??????hits?????????
        SearchHits hits = response.getHits();
        SearchHit[] hitsHits = hits.getHits();
        List<Goods> goodsList=Arrays.stream(hitsHits).map(hitsHit->{
            //json????????????Goods??????
            String json = hitsHit.getSourceAsString();//??????_source
            Goods goods = JSON.parseObject(json, Goods.class);
            //??????????????????????????????????????????
            Map<String, HighlightField> highlightFields = hitsHit.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            goods.setTitle(title.fragments()[0].string());
            return goods;
        }).collect(Collectors.toList());
        responseVo.setGoodsList(goodsList);

        //?????????????????????
        Aggregations aggregations = response.getAggregations();
        //???????????????brandId??????
        ParsedLongTerms brandIdAgg = (ParsedLongTerms)aggregations.get("brandIdAgg");
        List<? extends Terms.Bucket> brandBuckets = brandIdAgg.getBuckets();//??????brandId???
        if(!CollectionUtils.isEmpty(brandBuckets)){
            List<BrandEntity> brandEntities = brandBuckets.stream().map(brandBucket->{//???????????????????????????
                BrandEntity brandEntity = new BrandEntity();
                brandEntity.setId(((Terms.Bucket)brandBucket).getKeyAsNumber().longValue());
                
                //???????????????????????????
                Aggregations subAggregations = ((Terms.Bucket)brandBucket).getAggregations();
                ParsedStringTerms brandNameAgg = (ParsedStringTerms) subAggregations.get("brandNameAgg");
                List<? extends Terms.Bucket> brandNameBuckets = brandNameAgg.getBuckets();
                //???????????????name??????
                if(!CollectionUtils.isEmpty(brandNameBuckets)){
                    brandEntity.setName(brandNameBuckets.get(0).getKeyAsString());
                }

                //??????????????????logo??????
                ParsedStringTerms brandLogoAgg=(ParsedStringTerms)subAggregations.get("logoAgg");
                List<? extends Terms.Bucket> logoBuckets = brandLogoAgg.getBuckets();
                if(!CollectionUtils.isEmpty(logoBuckets)){
                    brandEntity.setLogo(logoBuckets.get(0).getKeyAsString());
                }
                return brandEntity;
            }).collect(Collectors.toList());
            responseVo.setBrands(brandEntities);
        }
        //??????categoryIdAgg????????????
        ParsedLongTerms categoryIdAgg = (ParsedLongTerms)aggregations.get("categoryIdAgg");
        List<? extends Terms.Bucket> categoryIdAggBuckets = categoryIdAgg.getBuckets();
        if(!CollectionUtils.isEmpty(categoryIdAggBuckets)){
            List<CategoryEntity> categoryEntities=categoryIdAggBuckets.stream().map(categoryIdBucket->{
                //??????categoryId
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

        //???????????????????????????????????????????????????
        ParsedNested attrAgg=(ParsedNested)aggregations.get("attrAgg");
        //?????????????????????????????????id?????????
        ParsedLongTerms attrIdAgg= (ParsedLongTerms)attrAgg.getAggregations().get("attrIdAgg");
        //??????id???????????????
        List<? extends Terms.Bucket> attrIdAggBuckets = attrIdAgg.getBuckets();
        if(!CollectionUtils.isEmpty(attrIdAggBuckets)){
            List<SearchResponseAttrValueVo> searchResponseAttrValueVos=attrIdAggBuckets.stream().map(attrIdAggBucket->{
                SearchResponseAttrValueVo searchResponseAttrValueVo = new SearchResponseAttrValueVo();
                //?????????key??????attrId
                searchResponseAttrValueVo.setAttrId(((Terms.Bucket)attrIdAggBucket).getKeyAsNumber().longValue());
                //??????attrId???????????????
                Aggregations attrAggregations = ((Terms.Bucket)attrIdAggBucket).getAggregations();
                //??????attrName?????????
                ParsedStringTerms attrNameAgg=(ParsedStringTerms)attrAggregations.get("attrNameAgg");
                //??????attrName??????
                List<? extends Terms.Bucket> attrNameAggBuckets = attrNameAgg.getBuckets();
                if(!CollectionUtils.isEmpty(attrNameAggBuckets)){
                    searchResponseAttrValueVo.setAttrName(attrNameAggBuckets.get(0).getKeyAsString());
                }
                //??????attrValue?????????
                ParsedStringTerms attrValueAgg=(ParsedStringTerms)attrAggregations.get("attrValueAgg");
                //??????attrValue??????
                List<? extends Terms.Bucket> attrValueAggBuckets = attrValueAgg.getBuckets();
                if(!CollectionUtils.isEmpty(attrValueAggBuckets)){
                    List<String> attrValues = attrValueAggBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
                    searchResponseAttrValueVo.setAttrValues(attrValues);
                }
                return searchResponseAttrValueVo;
            }).collect(Collectors.toList());
            responseVo.setFilters(searchResponseAttrValueVos);
        }


        //??????????????????
        long totalHits = hits.getTotalHits();
        responseVo.setTotal(totalHits);



        return responseVo;
    }
    /**
     * ????????????
     * @param searchParamVo
     * @return
     */
    private SearchSourceBuilder builderDsl(SearchParamVo searchParamVo){

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        String keyword = searchParamVo.getKeyword();
        if(StringUtils.isBlank(keyword)){
            return sourceBuilder;
        }

        //1???????????????????????????
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        sourceBuilder.query(boolQueryBuilder);
        //1.1???????????????
        boolQueryBuilder.must(QueryBuilders.matchQuery("title",keyword).operator(Operator.AND));

        //1.2?????????
        //1.2.1???????????????
        List<Long> brandId = searchParamVo.getBrandId();
        if(!CollectionUtils.isEmpty(brandId)){
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId",brandId));
        }

        //1.2.2???????????????
        List<Long> categoryId = searchParamVo.getCategoryId();
        if(!CollectionUtils.isEmpty(categoryId)){
            boolQueryBuilder.filter(QueryBuilders.termsQuery("categoryId",categoryId));
        }


        //1.2.3?????????????????????
        Double priceFrom = searchParamVo.getPriceFrom();
        Double priceTo = searchParamVo.getPriceTo();
        if(priceFrom!=null||priceTo!=null){
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("price");//??????price???????????????
            if(priceFrom!=null){
                rangeQuery.gte(priceFrom);
            }
            if(priceTo!=null){
                rangeQuery.lte(priceTo);
            }
            boolQueryBuilder.filter(rangeQuery);
        }

        //1.2.4????????????????????????
        Boolean store = searchParamVo.getStore();
        if(store!=null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("store",store));
        }

        //1.2.5?????????????????????
        List<String> props = searchParamVo.getProps();
        if(!CollectionUtils.isEmpty(props)){
            props.forEach(prop->{
                String[] attr = StringUtils.split(prop, ":");
                if(attr!=null&&attr.length==2){
                    //????????????????????????????????????????????? 4:8G-12G
                    String attrId=attr[0];
                    String attrValueString = attr[1];
                    String[] attrValues = StringUtils.split(attrValueString, "-");
                    //???????????????
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    boolQuery.must(QueryBuilders.termQuery("searchAttrs.attrId",attrId));
                    boolQuery.must(QueryBuilders.termsQuery("searchAttrs.attrValue",attrValues));
                    boolQueryBuilder.filter(QueryBuilders.nestedQuery("searchAttrs",boolQuery,ScoreMode.None));
                }
                
            });
        }

        //2???????????????
        Integer sort = searchParamVo.getSort();
        switch (sort){
            case 0: sourceBuilder.sort("_score", SortOrder.DESC); break;
            case 1: sourceBuilder.sort("price", SortOrder.DESC); break;
            case 2: sourceBuilder.sort("price", SortOrder.ASC); break;
            case 3: sourceBuilder.sort("sales", SortOrder.DESC); break;
            case 4: sourceBuilder.sort("createTime", SortOrder.DESC); break;
            default:
                throw new RuntimeException("??????????????????????????????");
        }


        //3?????????
        Integer pageNum = searchParamVo.getPageNum();
        Integer pageSize = searchParamVo.getPageSize();
        sourceBuilder.from((pageNum-1)*pageSize);
        sourceBuilder.size(pageSize);

        //4?????????
        sourceBuilder.highlighter(new HighlightBuilder().field("title").preTags("<font style='color:red;'>").postTags("</font>"));

        //5?????????
        //5.1???????????????
        sourceBuilder.aggregation(AggregationBuilders.terms("brandIdAgg").field("brandId")
                        .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName"))
                        .subAggregation(AggregationBuilders.terms("logoAgg").field("logo")));
        //5.2???????????????
        sourceBuilder.aggregation(AggregationBuilders.terms("categoryIdAgg").field("categoryId")
                        .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName")));

        //5.3?????????????????????
        sourceBuilder.aggregation(AggregationBuilders.nested("attrAgg", "searchAttrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("searchAttrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("searchAttrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("searchAttrs.attrValue"))
                )
        );

        //6??????????????????
        sourceBuilder.fetchSource(new String[]{"skuId","title","subTitle","price","defaultImage"},null);

        System.out.println(sourceBuilder);
        return sourceBuilder;

    }
}
