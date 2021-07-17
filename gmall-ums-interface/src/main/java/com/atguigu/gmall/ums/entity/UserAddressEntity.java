package com.atguigu.gmall.ums.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 收货地址表
 * 
 * @author niuqihang
 * @email 1208936851@qq.com
 * @date 2021-07-15 13:14:32
 */
@Data
@TableName("ums_user_address")
public class UserAddressEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * member_id
	 */
	private Long userId;
	/**
	 * 收货人
	 */
	private String name;
	/**
	 * 电话
	 */
	private String phone;
	/**
	 * 右边
	 */
	private String postCode;
	/**
	 * 省份
	 */
	private String province;
	/**
	 * 城市
	 */
	private String city;
	/**
	 * 区
	 */
	private String region;
	/**
	 * 详细地址
	 */
	private String address;
	/**
	 * 是否默认地址
	 */
	private Integer defaultStatus;

}
