package com.nowcoder.mall.service;

import com.nowcoder.mall.entity.User;

public interface UserService {

    void register(User user);

    User login(String phone,String password);

    User findUserById(int id);

    /*
    从缓存中查找user
     */
    User findUserFromCache(int id);
}
