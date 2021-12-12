package com.nowcoder.mall.service.impl;

import com.nowcoder.mall.common.ErrorCode;
import com.nowcoder.mall.entity.Item;
import com.nowcoder.mall.entity.User;
import com.nowcoder.mall.service.ItemService;
import com.nowcoder.mall.service.UserService;
import com.nowcoder.mall.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
@Service
public class promotionServiceImpl implements PromotionService, ErrorCode {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;


    @Override
    public String generateToken(int userId, int itemId, int promotionId) {
        // 1 校验参数
        if(userId <= 0 || itemId <= 0 || promotionId <= 0){
            return null;
        }
        // 2 售罄标志
        if(redisTemplate.hasKey("item:stock:over" + itemId)){
            return null;
        }

        // 3 校验用户
        User user = userService.findUserFromCache(userId);
        if(user == null){
            return null;
        }

        // 4 校验商品
        Item item = itemService.findItemInCache(itemId);
        if(item == null){
            return null;
        }

         // 5 校验活动  知道
        if(item.getPromotion() == null ||
            !item.getPromotion().getId().equals(promotionId) ||
            item.getPromotion().getStatus() != 0){
            return null;
        }
        // 6 秒杀大闸
        ValueOperations vo = redisTemplate.opsForValue();
        if(vo.decrement("promotion:gata:" + promotionId,1) < 0){
            return null;
        }
        String key = "promotion:token:" + userId + itemId + promotionId;
        String token = UUID.randomUUID().toString().replace("-","");
        vo.set(key,token,10, TimeUnit.MINUTES);
        return token;
    }
}
