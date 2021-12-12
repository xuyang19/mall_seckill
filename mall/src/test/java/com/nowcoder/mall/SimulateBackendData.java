package com.nowcoder.mall;

import com.nowcoder.mall.dao.ItemMapper;
import com.nowcoder.mall.dao.ItemStockMapper;
import com.nowcoder.mall.dao.PromotionMapper;
import com.nowcoder.mall.dao.UserMapper;
import com.nowcoder.mall.entity.Item;
import com.nowcoder.mall.entity.ItemStock;
import com.nowcoder.mall.entity.Promotion;
import com.nowcoder.mall.entity.User;
import com.nowcoder.mall.service.ItemService;
import com.nowcoder.mall.service.PromotionService;
import com.nowcoder.mall.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

// @SpringBootTest
public class SimulateBackendData {
    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PromotionMapper promotionMapper;

    @Autowired
    private ItemStockMapper itemStockMapper;

    @Autowired
    private ItemService itemService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private PromotionService promotionService;
    
    @Autowired
    private UserService userService;

    @Test
    public void initItems(){
        for (int i = 0; i < 30; i++) {
            Item item = new Item();
            item.setTitle("伊利 安慕希 常温希腊风味酸牛奶 原味230g*10瓶/箱（新年年货礼盒装）高端畅饮 迪丽热巴同款");
            item.setPrice(new BigDecimal((i + 1) * 10));
            item.setSales((i + 1) * 100);
            item.setImageUrl("http://img13.360buyimg.com/seckillcms/s500x500_jfs/t1/170165/16/3762/83664/600a6ae7E854b96d1/2135d5e2b052f37c.jpg");
            String description =
                    "<div class=\"row goods-parameter\">" +
                            "<div class=\"col-sm-12 brand\">品牌：伊利</div>" +
                            "<div class=\"col-sm-3\">商品名称：伊利安慕希</div>" +
                            "<div class=\"col-sm-3\">商品编号：4790297</div>" +
                            "<div class=\"col-sm-3\">商品毛重：2.945kg</div>" +
                            "<div class=\"col-sm-3\">商品产地：中国大陆</div>" +
                            "<div class=\"col-sm-3\">是否含果肉：无果肉</div>" +
                            "<div class=\"col-sm-3\">适用人群：成人</div>" +
                            "<div class=\"col-sm-3\">脂肪含量：全脂</div>" +
                            "<div class=\"col-sm-3\">净含量：其它</div>" +
                            "<div class=\"col-sm-3\">是否添加糖：添加糖</div>" +
                            "<div class=\"col-sm-3\">口味：原味</div>" +
                            "<div class=\"col-sm-3\">包装：整箱装</div>" +
                            "<div class=\"col-sm-3\">是否有机：非有机</div>" +
                            "</div>" +
                            "<div class=\"row goods-pictures\">" +
                            "<div class=\"col-sm-12\"><img src=\"http://img30.360buyimg.com/jgsq-productsoa/jfs/t1/163023/15/3412/115466/600b994eEc1507c70/d3b3c5c3f38dd38e.jpg\"/></div>" +
                            "<div class=\"col-sm-12\"><img src=\"http://img30.360buyimg.com/sku/jfs/t1/153362/3/12954/94425/5fef3a91E0e76b821/d7c7094abf9eb5c0.jpg\"/></div>" +
                            "<div class=\"col-sm-12\"><img src=\"http://img30.360buyimg.com/sku/jfs/t1/140813/29/4024/84987/5f1f9b21E67a55958/d860fb1ea09c7c5d.jpg\"/></div>" +
                            "<div class=\"col-sm-12\"><img src=\"http://img30.360buyimg.com/sku/jfs/t1/112319/11/13417/115512/5f1f9b22E42819010/215b184c1c74220b.jpg\"/></div>" +
                            "<div class=\"col-sm-12\"><img src=\"http://img30.360buyimg.com/sku/jfs/t1/117116/11/13309/151047/5f1f9b22E90e4f251/503917d2d87c3d9f.jpg\"/></div>" +
                            "<div class=\"col-sm-12\"><img src=\"http://img30.360buyimg.com/sku/jfs/t1/112780/22/13557/74246/5f1f9b21Ee0a2f5ae/b49d75ce59180823.jpg\"/></div>" +
                            "<div class=\"col-sm-12\"><img src=\"http://img30.360buyimg.com/sku/jfs/t1/111141/9/13367/97512/5f1f9b22E4c20c016/75bc1c2f9b2950a8.jpg\"/></div>" +
                            "</div>";
            item.setDescription(description);
            itemMapper.insert(item);

            ItemStock stock = new ItemStock();
            stock.setItemId(item.getId());
            stock.setStock((i + 1) * 20);
            itemStockMapper.insert(stock);

            Promotion promotion = new Promotion();
            promotion.setName("2021年货节");
            promotion.setStartTime(new Timestamp(System.currentTimeMillis()));
            promotion.setEndTime(new Timestamp(System.currentTimeMillis() + (long) 365 * 24 * 3600 * 1000));
            promotion.setItemId(item.getId());
            promotion.setPromotionPrice(new BigDecimal((i + 1) * 5));
            promotionMapper.insert(promotion);

        }
    }

    @Test
    public void cacheItemStock(){
        List<Item> list = itemService.findItemOnPromotion();
        for(Item item : list){
            Integer stock = item.getItemStock().getStock();
            redisTemplate.opsForValue().set("item:stock:" + item.getId(),stock);
        }

    }

    @Test
    public void initPromotionGate() {
        List<Item> list = itemService.findItemOnPromotion();
        for (Item item : list) {
            int stock = item.getItemStock().getStock();
            Promotion promotion = item.getPromotion();
            redisTemplate.opsForValue().set("promotion:gate:" + promotion.getId(), stock * 5);
        }
    }
    @Test
    public void testPromotionService(){
      //  User user = userService.findUserById(19);
        //redisTemplate.opsForValue().set("user:"+19,user);
        //Item item = itemService.findItemInCache(161);
//        List<Item> itemOnPromotion = itemService.findItemOnPromotion();
//        int id  = itemOnPromotion.get(0).getPromotion().getId();
//        System.out.println(id);
        // itemService.
//        Item item = itemService.findItemId(161);
//        redisTemplate.opsForValue().set("item:"+ 161,item);

//        Item item = itemService.findItemId(161);
//        redisTemplate.opsForValue().set("item:"+161,item);
        String token = promotionService.generateToken(19, 161, 92);
//
        System.out.println(token);

    }

    
    
    
    
    
}
