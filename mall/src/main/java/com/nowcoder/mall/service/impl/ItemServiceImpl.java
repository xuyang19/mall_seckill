package com.nowcoder.mall.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.nowcoder.mall.common.BusinessException;
import com.nowcoder.mall.common.ErrorCode;
import com.nowcoder.mall.component.ObjectValidator;
import com.nowcoder.mall.dao.ItemMapper;
import com.nowcoder.mall.dao.ItemStockLogMapper;
import com.nowcoder.mall.dao.ItemStockMapper;
import com.nowcoder.mall.dao.PromotionMapper;
import com.nowcoder.mall.entity.Item;
import com.nowcoder.mall.entity.ItemStock;
import com.nowcoder.mall.entity.ItemStockLog;
import com.nowcoder.mall.entity.Promotion;
import com.nowcoder.mall.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService, ErrorCode {

    private Logger logger = LoggerFactory.getLogger(ItemServiceImpl.class);

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private ItemStockMapper itemStockMapper;

    @Autowired
    private PromotionMapper promotionMapper;

    @Autowired
    private ObjectValidator validator;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ItemStockLogMapper itemStockLogMapper;

    // 本地缓存
    private Cache<String,Object> cache;

    @PostConstruct
    public void init(){
        cache = CacheBuilder.newBuilder()
                .initialCapacity(10)
                .maximumSize(100)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();
    }

    @Override
    public List<Item> findItemOnPromotion() {
        List<Item> items = itemMapper.selectOnPromotion();
        /*
        需要加上 promotion 和 stock两个属性 、练练Lammda
         */
        return items.stream().map(item -> {
            // 查库存
            ItemStock itemStock = itemStockMapper.selectByItemId(item.getId());
            item.setItemStock(itemStock);
            // 查活动
            Promotion promotion = promotionMapper.selectByItemId(item.getId());
            if(promotion != null && promotion.getStatus() == 0){
                item.setPromotion(promotion);
            }
            return item;
        }).collect(Collectors.toList());
    }

    @Override
    public Item findItemId(int id) {
        // 查商品
        Item item = itemMapper.selectByPrimaryKey(id);

        // 查库存
        ItemStock itemStock = itemStockMapper.selectByItemId(id);
        item.setItemStock(itemStock);

        // 查活动
        Promotion promotion = promotionMapper.selectByItemId(id);
        if(promotion != null && promotion.getStatus() == 0){
            item.setPromotion(promotion);
        }

        return item;
    }

    /*
    设置二级缓存
     */
    public Item findItemInCache(int id){
        if(id <= 0){
            throw new BusinessException(PARAMETER_ERROR,"参数不合法！");
        }
        Item item = null;
        String key = "item:" + id;

        // guava
        item = (Item)cache.getIfPresent(key);
        if(item != null){
            return item;
        }

        // redis
        item = (Item)redisTemplate.opsForValue().get(key);
        if(item != null){
            cache.put(key,item);
            return item;
        }
        // mysql
        item = this.findItemId(id);
        if(item != null){
            cache.put(key,item);
            redisTemplate.opsForValue().set(key,item,3,TimeUnit.MINUTES);

        }
        return item;
    }

    @Transactional
    public boolean decreaseStock(int itemId, int amount) {
        int rows = itemStockMapper.decreaseStock(itemId, amount);
        return rows > 0;
    }

    @Transactional
    public void increaseSales(int itemId, int amount) {
        itemMapper.increaseSales(itemId,amount);
    }

    @Override
    public boolean increaseStockInCache(int itemId, int amount) {
        if(itemId <= 0 || amount <= 0){
            throw new BusinessException(PARAMETER_ERROR,"参数不合法！");
        }

        String key = "item:" + itemId;
        redisTemplate.opsForValue().increment(key,amount);
        return true;
    }

    /*
    预减库存 在缓存中
     */

    @Override
    public boolean decreaseStockInCache(int itemId, int amount) {
        if(itemId <= 0 || amount <= 0){
            throw new BusinessException(PARAMETER_ERROR,"参数不合法！");
        }
        String key = "item:" + itemId;
        Long result = redisTemplate.opsForValue().decrement(key, amount);

        if(result < 0){
            // 回补库存
            this.increaseStockInCache(itemId,amount);
            logger.debug("回补库存完成[" + itemId + "]");
        }else if(result == 0){
            // 售罄标志
            redisTemplate.opsForValue().set("item:stock:over:" + itemId,1);
            logger.debug("售罄标识完成 [" + itemId + "]");
        }

        return result >= 0;
    }

    @Override
    public ItemStockLog createItemStockLog(int itemId, int amount) {
        // 1 参数判断
        if(itemId <= 0 || amount <= 0){
            throw new BusinessException(PARAMETER_ERROR,"参数不合法！");
        }
        // 2 创建Log
        ItemStockLog log = new ItemStockLog();
        log.setId(UUID.randomUUID().toString().replace("-",""));
        log.setItemId(itemId);
        log.setAmount(amount);
        log.setStatus(0);

        // 3 插入日志
        itemStockLogMapper.insert(log);

        return log;
    }

    @Override
    public void updateItemStockLogStatus(String id, int status) {
        ItemStockLog log = itemStockLogMapper.selectByPrimaryKey(id);
        log.setStatus(status);
        itemStockLogMapper.updateByPrimaryKey(log);
    }

    @Override
    public ItemStockLog findItemStockLogById(String id) {
        if(StringUtils.isEmpty(id)){
            throw new BusinessException(PARAMETER_ERROR,"参数不合法！");
        }
        return itemStockLogMapper.selectByPrimaryKey(id);
    }

}
