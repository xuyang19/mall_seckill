package com.nowcoder.mall.service;

import com.nowcoder.mall.entity.Item;
import com.nowcoder.mall.entity.ItemStockLog;

import java.util.List;

public interface ItemService {

    /*
    查询参加活动的商品
     */
    List<Item> findItemOnPromotion();

    /*
    通过id查询商品
     */
    Item findItemId(int id);

    /*
    通过id查缓存商品
     */
    Item findItemInCache(int id);

    /*
    通过商品id判断是否能减库存
     */
    boolean decreaseStock(int itemId,int amount);

    /*
    增加销量
     */
    void increaseSales(int itemId,int amount);

    /*
    在缓存中增加销量
     */
    boolean increaseStockInCache(int itemId,int amount);

    /*
    在缓存中减销量
     */
    boolean decreaseStockInCache(int itemId,int amount);

    /*
    创建流水日志
     */
    ItemStockLog createItemStockLog(int itemId,int amount);

    /*
    更新流水日志状态
     */
    void updateItemStockLogStatus(String id,int status);

    /*
    根据流水号查日志号
     */
    ItemStockLog findItemStockLogById(String id);

}
