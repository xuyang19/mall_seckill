# mall_seckill
本项目是电商项目中的秒杀模块：
主要是实现用户登录、注册、秒杀、下单操作
适用于编程经验较少的同学入门、也可以用来当做找实习、工作的一个项目
技术栈：springboot、spring、mybatis、springmvc、mysql、redis、rocketMQ、maven、git等
前端轻量级框架：bootstrap


1 第一版：完成登录注册和秒杀购买的基本功能


2 第二版：引入redis
    2.1 redis做分布式状态管理：将session转换成token存在redis中
        其中（phone,token）目的为了多种客户端可以访问到
    2.2 item引入redis:做商品的缓存
        2.2.1 引入两层缓存，可以在缓存中查找商品，命中后返回
        2.2.2 可以在缓存中扣减库存、增加库存（第三版引入MQ保证一致性）
    2.3 在user中引入缓存，命中后返回，redis未命中，则找DB，同时同步到redis中

3 第三版：引入了rocketmq
    3.1 下载mq 配置 mqnamesrv、mqbroker、 runbroker(conf)
    3.2 test 单测：rocketMQ、rocketMQ in spring  (@RocketMQTransactionListener)全局只能有一个
    3.3 增加相应的方法，引入流水，根据流水状态来判断mq.mq收到消息就会执行相关操作（一般是对DB的更改）
