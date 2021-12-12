package com.nowcoder.mall.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.mall.common.BusinessException;
import com.nowcoder.mall.common.ErrorCode;
import com.nowcoder.mall.common.Toolbox;
import com.nowcoder.mall.dao.OrderMapper;
import com.nowcoder.mall.dao.SerialNumberMapper;
import com.nowcoder.mall.entity.*;
import com.nowcoder.mall.service.ItemService;
import com.nowcoder.mall.service.OrderService;
import com.nowcoder.mall.service.UserService;
import com.sun.org.apache.xpath.internal.operations.Or;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
@Service
public class OrderServiceImpl implements OrderService, ErrorCode {

    Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private SerialNumberMapper serialNumberMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     格式：日期 + 流水s
     示例：20210123000000000001
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateOrderID(){
        // 拼入日期流水
        StringBuilder sb = new StringBuilder();
        sb.append(Toolbox.format(new Date(),"yyyyMMdd"));
        // 获取流水号
        SerialNumber serial = serialNumberMapper.selectByPrimaryKey("order_serial");
        Integer value = serial.getValue();

        // 更新流水号
        serial.setValue(value + serial.getStep());
        serialNumberMapper.updateByPrimaryKey(serial);
        // 拼入流水号
        String prefix = "000000000000".substring(value.toString().length());
        sb.append(prefix).append(value);
        return sb.toString();
    }



    /**
     *
     * @param userId
     * @param itemId
     * @param promotionId
     * @param amount
     * @return
     */
    @Override
    @Transactional
    public Order createOrder(int userId, int itemId, int amount, Integer promotionId,String itemStockLogId) {
        // 1 校验参数
        if(amount < 1 || promotionId != null&&promotionId.intValue() <= 0){
            throw new BusinessException(PARAMETER_ERROR,"参数不合法！");
        }
//        // 2 校验用户
//        User user = userService.findUserById(userId);
//        if(user == null){
//            throw new BusinessException(PARAMETER_ERROR,"指定用户不存在!");
//        }

        // 3 校验商品
       // Item item = itemService.findItemId(itemId);
        Item item = itemService.findItemInCache(itemId);
        if(item == null){
            throw new BusinessException(PARAMETER_ERROR,"指定商品不存在！");
        }
//        // 4 校验库存
//        int stock = item.getItemStock().getStock();
//        if(amount > stock){
//            throw new BusinessException(PARAMETER_ERROR,"库存不足！");
//        }

//        // 5 校验活动
//        if(promotionId != null){
//            if(item.getPromotion().getId() == null){
//                throw new BusinessException(PARAMETER_ERROR,"指定商品无活动！");
//            }else if(!item.getPromotion().getId().equals(promotionId)){
//                throw new BusinessException(PARAMETER_ERROR,"指定的活动不存在！");
//            }else if(item.getPromotion().getStatus() == 1){
//                throw new BusinessException(PARAMETER_ERROR,"指定的活动未开始！");
//            }
//        }


        // 扣减库存
        // 原来的方法增加了行锁
        // 缓存库存最终保持一致性



        // 6 扣减库存
       // boolean successful = itemService.decreaseStock(itemId, amount);
        boolean successful = itemService.decreaseStockInCache(itemId, amount);
        logger.debug("预减库存完成[" + successful + "]");
        if(!successful){
            throw new BusinessException(STOCK_NOT_ENOUGH,"库存不足！");
        }
        // 7 生成订单
        Order order = new Order();
        order.setId(this.generateOrderID());
        order.setUserId(userId);
        order.setItemId(itemId);
        order.setPromotionId(promotionId);
        order.setOrderPrice(promotionId != null ? item.getPromotion().getPromotionPrice() : item.getPrice());
        order.setOrderAmount(amount);
        order.setOrderTotal(order.getOrderPrice().multiply(new BigDecimal(amount)));
        order.setOrderTime(new Timestamp(System.currentTimeMillis()));

        // 8 插入订单
        orderMapper.insert(order);
        logger.debug("生成订单完成 [" + order.getId() + "]");


        // 9 更新销量
        // itemService.increaseSales(itemId,amount);
        // itemService.increaseSales()


        // 增加销量
        JSONObject body = new JSONObject();
        body.put("itemId",itemId);
        body.put("amount",amount);

        Message msg = MessageBuilder.withPayload(body.toString()).build();
        rocketMQTemplate.asyncSend("seckill:increase_sales", msg, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                logger.debug("投递增加商品销量消息成功");
            }

            @Override
            public void onException(Throwable throwable) {
                logger.error("投递增加商品销量消息失败", throwable);
            }
        });

        // 更新库存流水状态
        itemService.updateItemStockLogStatus(itemStockLogId,1);
        logger.debug("更新流水完成 [" + itemStockLogId + "]");

        return order;
    }

    @Override
    public void creatOrderAsync(int userId, int itemId, int amount, Integer promotionId) {
        // 1 售罄标识
        if(redisTemplate.hasKey("item:stock:over:" + itemId)){
            throw new BusinessException(STOCK_NOT_ENOUGH,"已经售罄！");
        }

        // 2 生成库存流水
        ItemStockLog itemStockLog = itemService.createItemStockLog(itemId, amount);
        logger.debug("生成库存流水完成[" + itemStockLog.getId() + "]");


        // 3 构建消息体
        JSONObject body = new JSONObject();
        body.put("itemId",itemId);
        body.put("amount",amount);
        body.put("itemStockLogId",itemStockLog.getId());


        // 4 构建参数
        JSONObject arg = new JSONObject();
        arg.put("userId", userId);
        arg.put("itemId", itemId);
        arg.put("amount", amount);
        arg.put("promotionId", promotionId);
        arg.put("itemStockLogId", itemStockLog.getId());

        // 4 尝试发送消息
        String dest = "seckill:decrease_stock";
        // 4.1 创建消息
        Message msg = MessageBuilder.withPayload(body.toString()).build();
        // 4.2
        try {
            logger.debug("尝试扣减库存消息 [" + body.toString() + "]");
            TransactionSendResult sendResult = rocketMQTemplate.sendMessageInTransaction(dest, msg, arg);
            if (sendResult.getLocalTransactionState() == LocalTransactionState.UNKNOW){
                throw new BusinessException(UNDEFINED_ERROR, "创建订单失败！");
            }else if (sendResult.getLocalTransactionState() == LocalTransactionState.ROLLBACK_MESSAGE){
                throw new BusinessException(CREATE_ORDER_FAILURE, "创建订单失败！");
            }
        } catch (Exception e) {
            throw new BusinessException(UNDEFINED_ERROR, "创建订单失败！");
        }


    }
}
