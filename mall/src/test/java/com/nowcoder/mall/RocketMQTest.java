package com.nowcoder.mall;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
//import sun.plugin2.message.Message;

import java.nio.charset.Charset;
import java.util.List;

/*
test 单线程
main 多线程
 */
public class RocketMQTest {

    public static final Charset UTF_8 = Charset.forName("UTF-8");

    public static void main(String[] args) {

//        try {
//            testDefaultMQProducer();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        try {
            testTransactionMQProducer();
        } catch (MQClientException e) {
            e.printStackTrace();
        }
        try {
            testDefaultMQConsumer();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void testDefaultMQProducer() throws Exception{
        DefaultMQProducer producer = new DefaultMQProducer("seckill_producer");
        producer.setNamesrvAddr("106.14.71.219:9876");
        producer.start();

        String topic = "seckillTest";
        String tag = "tag1";
        for (int i = 0; i < 100; i++) {
            String body = "message " + i;
           // Message message = new Message(topic, tag, body.getBytes(UTF_8));
           // new org.apache.rocketmq.common.message.Message()
            Message message = new Message(topic, tag, body.getBytes(UTF_8));

            SendResult re = producer.send(message);
            System.out.println(re);
        }
    }

    public static void testDefaultMQConsumer() throws Exception{
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("seckill_consumer");
        consumer.setNamesrvAddr("106.14.71.219:9876");
        consumer.subscribe("seckillTest","*");
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                System.out.println(Thread.currentThread().getName() + ": " + list);
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
    }

    /*
    事务型 生产者
     */
    public static void testTransactionMQProducer() throws MQClientException {
        TransactionMQProducer producer = new TransactionMQProducer("seckill_producer");
        producer.setNamesrvAddr("106.14.71.219:9876");
        producer.setTransactionListener(new TransactionListenerImpl());
        producer.start();

        String topic = "seckillTest";
        String tag = "tag2";
        for (int i = 0; i < 10; i++) {
            String body = "message " + i;
            Message message = new Message(topic, tag, body.getBytes(UTF_8));
            SendResult result = producer.sendMessageInTransaction(message, null);
            System.out.println(result);
        }


    }
    static class TransactionListenerImpl implements TransactionListener{


        @Override
        public LocalTransactionState executeLocalTransaction(Message message, Object o) {
            System.out.println("executeLocalTransaction: " + message + ", " + o);
            return LocalTransactionState.UNKNOW;
        }

        @Override
        public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
            System.out.println("checkLocalTransaction: " + messageExt);
            return LocalTransactionState.UNKNOW;
        }
    }






}
