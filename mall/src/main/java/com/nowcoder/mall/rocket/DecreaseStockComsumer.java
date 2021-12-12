package com.nowcoder.mall.rocket;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.mall.service.ItemService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RocketMQMessageListener(topic = "seckill",
consumerGroup = "seckill_stock",selectorExpression = "decrease_stock")
public class DecreaseStockComsumer implements RocketMQListener<String> {

    private Logger logger = LoggerFactory.getLogger(DecreaseStockComsumer.class);

    @Autowired
    private ItemService itemService;

    @Override
    public void onMessage(String message) {
        // 解析消息体中的参数
        JSONObject param = JSONObject.parseObject(message);
        int itemId = (int) param.get("itemId");
        int amount = (int) param.get("amount");

        try {
            itemService.decreaseStock(itemId,amount);
            logger.debug("最终扣减库存完成 [" + param.get("itemStockLogId") + "]");
        } catch (Exception e) {
            //e.printStackTrace();
            logger.error("从DB扣减库存失败", e);
        }


    }
}
