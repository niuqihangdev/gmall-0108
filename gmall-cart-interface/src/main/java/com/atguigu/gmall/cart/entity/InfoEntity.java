package com.atguigu.gmall.cart.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * 
 * @author niuqihang
 * @email 1208936851@qq.com
 * @date 2021-07-16 19:42:04
 */
@Data
@TableName("cart_info")
public class InfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	@TableId
	private Long id;
	/**
	 * 用户id或者userKey
	 */
	private String userId;
	/**
	 * skuId
	 */
	private Long skuId;
	/**
	 * 选中状态
	 */
	private Integer check;
	/**
	 * 标题
	 */
	private String title;
	/**
	 * 默认图片
	 */
	private String defaultImage;
	/**
	 * 加入购物车时价格
	 */
	private BigDecimal price;
	/**
	 * 数量
	 */
	private Integer count;
	/**
	 * 是否有货
	 */
	private Integer store;
	/**
	 * 销售属性（json格式）
	 */
	private String saleAttrs;
	/**
	 * 营销信息（json格式）
	 */
	private String sales;

}
