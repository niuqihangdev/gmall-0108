package com.atguigu.gmall.search.service;

import com.atguigu.gmall.search.vo.SearchParamVo;
import com.atguigu.gmall.search.vo.SearchResponseVo;

public interface SearchService {
    SearchResponseVo search(SearchParamVo searchParamVo);
}
