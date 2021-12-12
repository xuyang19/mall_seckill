package com.nowcoder.mall.service;

public interface PromotionService {

    String generateToken(int userId,int itemId,int promotionId);
}
