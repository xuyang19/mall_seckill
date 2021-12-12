package com.nowcoder.mall.service;

import com.nowcoder.mall.entity.Order;

public interface OrderService {
    Order createOrder(int userId,int itemId,int amount,Integer promotionId,String itemStockLogId);

    void creatOrderAsync(int userId,int itemId,int amount,Integer promotionId);
}
