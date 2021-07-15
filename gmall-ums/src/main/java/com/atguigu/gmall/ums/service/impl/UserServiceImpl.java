package com.atguigu.gmall.ums.service.impl;

import com.atguigu.gmall.ums.config.AliyunPhoneCodeConfig;
import com.atguigu.gmall.ums.util.FormUtils;
import com.atguigu.gmall.ums.util.HttpUtils;
import com.atguigu.gmall.ums.util.RandomUtils;
import com.google.gson.Gson;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.ums.mapper.UserMapper;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.ums.service.UserService;
import org.apache.http.HttpResponse;
import org.springframework.util.CollectionUtils;


@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    AliyunPhoneCodeConfig aliyunPhoneCodeConfig;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserEntity>()
        );

        return new PageResultVo(page);
    }

    /**
     * 数据校验
     * @param data
     * @param type
     * @return
     */
    @Override
    public Boolean checkDataAndType(String data, Integer type) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        switch (type){
            case 1:queryWrapper.eq("username",data); break;
            case 2:queryWrapper.eq("phone",data); break;
            case 3:queryWrapper.eq("email",data); break;
            default:
                return null;
        }
        return this.count(queryWrapper)==0;
    }

    /**
     * 获取手机验证码
     * @param phone
     */
    @Override
    public void sendPhoneCode(String phone) {
        //1、验证手机格式是否正确
        boolean isMobile = FormUtils.isMobile(phone);
        if(!isMobile){
            throw new RuntimeException("手机号格式错误");
        }
        //设置redis的键和值
        String codeKey = "code:codekey:" + phone;  //存储十分钟的验证码键
        String codeKeyMinute = "code:codekeyMinute:" + phone;  //验证一分钟内是否发送的键
        String codeKeyDay = "code:codeKeyDay:" + phone;  //验证一天内是否超过五次的键
        //2、在一分钟内是否已发送过验证码
        if(redisTemplate.hasKey(codeKeyMinute)){
            throw new RuntimeException("在一分钟内已发送过验证码");
        }
        //3、一天内发送验证码的次数是否超过五次
        Object countCode= redisTemplate.opsForValue().get(codeKeyDay);
        if(countCode!=null){
            int i=Integer.parseInt(countCode.toString());
            if(i>=5){
                throw  new RuntimeException("一天内发送验证码的次数超过五次");
            }
        }
        //4、发送验证码
        String host = aliyunPhoneCodeConfig.getHost();
        String path = aliyunPhoneCodeConfig.getPath();
        String method = aliyunPhoneCodeConfig.getMethod();
        String appcode = aliyunPhoneCodeConfig.getAppcode();
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", phone);
        //随机生成六位验证码
        String code = RandomUtils.getSixBitRandom();
        querys.put("param", "**code**:"+code+",**minute**:10");
        querys.put("smsSignId", aliyunPhoneCodeConfig.getSmsSignId());
        querys.put("templateId", aliyunPhoneCodeConfig.getTemplateId());
        Map<String, String> bodys = new HashMap<String, String>();
        try {
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            //将运行结果返回的结果转换为一个json指定的map集合
            Gson gson = new Gson();
            Map map = gson.fromJson(EntityUtils.toString(response.getEntity()), Map.class);
            String returnCode = map.get("code").toString();
            //如果返回值不为0，报错
            if(!"0".equals(returnCode)){
                throw new RuntimeException("短信发送失败");
            }
            //存入Redis
            //5、保存验证码十分钟
            redisTemplate.opsForValue().set(codeKey,code,10, TimeUnit.MINUTES); //设置手机验证码，十分钟过期
            redisTemplate.opsForValue().set(codeKeyMinute,"213123",1,TimeUnit.MINUTES);//设置一个存入一分钟的键用来验证一分钟之内是否再次发送
            Boolean haskey = redisTemplate.hasKey(codeKeyDay);
            if(haskey){
                //如果redis中存在，redis的value+1
                redisTemplate.opsForValue().increment(codeKeyDay);
            }else{
                redisTemplate.opsForValue().set(codeKeyDay,"1",24,TimeUnit.HOURS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 用户注册
     * @param userEntity
     * @param code
     */
    @Override
    public void register(UserEntity userEntity, String code) {
        //验证短信验证码
        //生成盐
        String salt = StringUtils.substring(UUID.randomUUID().toString(), 0, 6);
        userEntity.setSalt(salt);
        //对密码加盐加密
        userEntity.setPassword(DigestUtils.md5Hex(userEntity.getPassword()+salt));
        //新增用户
        userEntity.setLevelId(1L);
        userEntity.setNickname(userEntity.getUsername());
        userEntity.setSourceType(1);
        userEntity.setIntegration(1000);
        userEntity.setGrowth(1000);
        userEntity.setStatus(1);
        userEntity.setCreateTime(new Date());
        this.save(userEntity);


        //删除redis中的短信验证码
    }

    /**
     * 查询用户
     * @param loginName
     * @param password
     * @return
     */
    @Override
    public UserEntity queryUser(String loginName, String password) {
        //根据登录名查询用户信息，是否存在
        List<UserEntity> userEntities = this.list(new QueryWrapper<UserEntity>().eq("username", loginName)
                .or().eq("email", loginName).or().eq("phone", loginName));
        //如果用户信息为空，返回null
        if(CollectionUtils.isEmpty(userEntities)){
            return null;
        }
        //从数据库中获取盐，对用户输入的密码加密
        for (UserEntity userEntity : userEntities) {
            String salt = userEntity.getSalt();
            String newPassword = DigestUtils.md5Hex(password + salt);
            //生成的加密密码和数据库的密码对比
            if(StringUtils.equals(newPassword,userEntity.getPassword())){
                return userEntity;
            }
        }


        return null;
    }

}