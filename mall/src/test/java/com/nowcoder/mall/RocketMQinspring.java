package com.nowcoder.mall;

import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

//@SpringBootApplication
public class RocketMQinspring implements CommandLineRunner {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;


    public static void main(String[] args) {
        SpringApplication.run(RocketMQinspring.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // testProducer();
        testProduceInTransaction();

    }

    private void testProducer() throws Exception{
        for (int i = 0; i < 100; i++) {
            String destination = "seckillTest:tag" + (i % 2);

            Message message = MessageBuilder.withPayload("message " + i).build();
            rocketMQTemplate.asyncSend(destination, message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    System.out.println("SUCCESS:" + sendResult);
                }

                @Override
                public void onException(Throwable throwable) {
                    throwable.printStackTrace();
                }
            },3000);

        }
    }

    @Service
    @RocketMQMessageListener(topic = "seckillTest",
    consumerGroup = "seckill_comsumer_0",selectorExpression = "*")
    private static class StringConsumer0 implements RocketMQListener{

        @Override
        public void onMessage(Object message) {
            System.out.println("StringConsumer0" + message);
        }
    }

    private void testProduceInTransaction() throws Exception {
        String destination = "seckillTest:tagT";
        for (int i = 0; i < 10; i++) {
            Message message = MessageBuilder.withPayload("message " + i).build();
            TransactionSendResult sendResult = rocketMQTemplate.sendMessageInTransaction(destination, message, null);
            System.out.println(sendResult);
        }
    }

    //@RocketMQTransactionListener
    private class TransactionListenerImpl implements RocketMQLocalTransactionListener {

        @Override
        public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
            System.out.println("executeLocalTransaction: " + msg + ", " + arg);
            return RocketMQLocalTransactionState.COMMIT;
        }

        @Override
        public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
            System.out.println("checkLocalTransaction: " + msg);
            return RocketMQLocalTransactionState.COMMIT;
        }
    }
}
