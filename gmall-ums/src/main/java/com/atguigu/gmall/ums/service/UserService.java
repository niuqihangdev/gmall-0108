package com.atguigu.gmall.ums.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.ums.entity.UserEntity;

import java.util.Map;

/**
 * 用户表
 *
 * @author niuqihang
 * @email 1208936851@qq.com
 * @date 2021-07-15 13:14:32
 */
public interface UserService extends IService<UserEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    Boolean checkDataAndType(String data, Integer type);

    void sendPhoneCode(String phone);

    void register(UserEntity userEntity, String code);

    UserEntity queryUser(String loginName, String password);
}

