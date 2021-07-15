package com.atguigu.gmall.ums.mapper;

import com.atguigu.gmall.ums.entity.UserEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表
 * 
 * @author niuqihang
 * @email 1208936851@qq.com
 * @date 2021-07-15 13:14:32
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
	
}
