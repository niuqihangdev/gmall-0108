package com.atguigu.gmall.sms.mapper;

import com.atguigu.gmall.sms.entity.CouponSpuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券与产品关联
 * 
 * @author niuqihang
 * @email 1208936851@qq.com
 * @date 2021-06-22 22:27:35
 */
@Mapper
public interface CouponSpuMapper extends BaseMapper<CouponSpuEntity> {
	
}
