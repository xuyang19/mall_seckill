package com.nowcoder.mall.service.impl;

import com.nowcoder.mall.common.BusinessException;
import com.nowcoder.mall.common.ErrorCode;
import com.nowcoder.mall.component.ObjectValidator;
import com.nowcoder.mall.dao.UserMapper;
import com.nowcoder.mall.entity.User;
import com.nowcoder.mall.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class UserServiceImpl implements UserService, ErrorCode {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ObjectValidator objectValidator;

    @Autowired
    private RedisTemplate redisTemplate;

    Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);


    @Transactional
    public void register(User user) {
        // 1 判断user 是否为空
        if(user == null){
            throw new BusinessException(PARAMETER_ERROR,"参数不合法！");
        }

        // 2 validate 验证
        Map<String, String> result = objectValidator.validate(user);
        if(result != null && result.size() > 0){
            throw new BusinessException(PARAMETER_ERROR,
                    StringUtils.join(result.values().toArray(),",") + "!");
        }

        // 3 插入db中
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            //e.printStackTrace();
            throw new BusinessException(PARAMETER_ERROR,"该手机号已经被注册");
        }

    }

    @Override
    public User login(String phone, String password) {
        if(StringUtils.isEmpty(phone) || StringUtils.isEmpty(password)){
            throw new BusinessException(PARAMETER_ERROR,"参数不合法！");
        }
        User user = userMapper.selectByPhone(phone);

        if(user == null || !password.equals(user.getPassword())){
            throw new BusinessException(USER_LOGIN_FAULURE,"账号或者密码错误！");
        }

        return user;
    }

    @Override
    public User findUserById(int id) {
        return userMapper.selectByPrimaryKey(id);
    }

    @Override
    public User findUserFromCache(int id) {
        // 1 校验参数
        if(id <= 0){
            return null;
        }

        User user = null;
        String key = "user:" + id;
        // 2 redis
        user = (User) redisTemplate.opsForValue().get(key);
        if(user != null){
            logger.debug("缓存命中[" + user + "]");
            return user;
        }

        // 3 mysql
        user = this.findUserById(id);
        if(user != null){
            logger.debug("同步缓存[" + user + "]");
            redisTemplate.opsForValue().set(key,user);
            return user;
        }
        return null;

    }
}
