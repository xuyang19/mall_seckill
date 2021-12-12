package com.nowcoder.mall.rocket;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.mall.entity.ItemStockLog;
import com.nowcoder.mall.entity.Order;
import com.nowcoder.mall.service.ItemService;
import com.nowcoder.mall.service.OrderService;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
@RocketMQTransactionListener
public class LocalTransactionListenerImpl implements RocketMQLocalTransactionListener {

    private Logger logger = LoggerFactory.getLogger(LocalTransactionListenerImpl.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private ItemService itemService;
    /*
     项目中只能有一个 @RocketMQTransactionListener，所以本地事务都是在这里执行的，我们通过获取的标签
     来确定我们使用相应的逻辑
     */
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        try {
            String tag = msg.getHeaders().get("rocketmq_TAGS").toString();
            if("decrease_stock".equals(tag)){
                return this.createOrder(msg,arg);
            }else{
                return RocketMQLocalTransactionState.UNKNOWN;
            }
        } catch (Exception e) {
            logger.error("执行MQ本地事务时发生错误", e);
            return RocketMQLocalTransactionState.ROLLBACK;
           // e.printStackTrace();
        }
    }

    private RocketMQLocalTransactionState createOrder(Message msg, Object arg) {
        JSONObject param = new JSONObject();
        int userId = (int) param.get("userId");
        int itemId = (int) param.get("itemId");
        int amount = (int) param.get("amount");
        int promotionId = (int) param.get("promotionId");
        String itemStockLogId = (String) param.get("itemStockLogId");

        try {
            Order order = orderService.createOrder(userId, itemId, amount, promotionId, itemStockLogId);
            logger.debug("本地事务提交完成 [" + order.getId() + "]");
            return RocketMQLocalTransactionState.COMMIT;
        } catch (Exception e) {
            logger.error("创建订单失败", e);
            itemService.updateItemStockLogStatus(itemStockLogId, 3);
            logger.debug("更新流水完成 [" + itemStockLogId + "]");
            return RocketMQLocalTransactionState.ROLLBACK;
            //e.printStackTrace();
        }

    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        try {
            String tag = (String) msg.getHeaders().get("rocketmq_TAGS");
            if ("decrease_stock".equals(tag)) {
                return this.checkStockStatus(msg);
            } else {
                return RocketMQLocalTransactionState.UNKNOWN;
            }
        } catch (Exception e) {
            logger.error("检查MQ本地事务时发生错误", e);
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    private RocketMQLocalTransactionState checkStockStatus(Message msg) {
        JSONObject body = JSONObject.parseObject(new String((byte[]) msg.getPayload()));
        String itemStockLogId = (String) body.get("itemStockLogId");
        ItemStockLog itemStockLog = itemService.findItemStockLogById(itemStockLogId);
        logger.debug("检查事务状态完成 [" + itemStockLog + "]");
        if (itemStockLog == null) {
            return RocketMQLocalTransactionState.ROLLBACK;
        } else if (itemStockLog.getStatus() == 0) {
            return RocketMQLocalTransactionState.UNKNOWN;
        } else if (itemStockLog.getStatus() == 1) {
            return RocketMQLocalTransactionState.COMMIT;
        } else {
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

}
