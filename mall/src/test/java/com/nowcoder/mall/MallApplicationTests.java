package com.nowcoder.mall;

import com.alibaba.fastjson.JSON;
import com.nowcoder.mall.dao.ItemMapper;
import com.nowcoder.mall.dao.ItemStockMapper;
import com.nowcoder.mall.dao.PromotionMapper;
import com.nowcoder.mall.dao.UserMapper;
import com.nowcoder.mall.entity.*;
import com.nowcoder.mall.service.ItemService;
import com.nowcoder.mall.service.OrderService;
import com.nowcoder.mall.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MallApplicationTests {

	@Autowired
	private ItemMapper itemMapper;

	@Autowired
	private ItemStockMapper itemStockMapper;

	@Autowired
	private PromotionMapper promotionMapper;

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private OrderService orderService;

	@Autowired
	private ItemService itemService;

	@Autowired
	private UserService userService;

	@Test
	void contextLoads() {
	}

	/*
	1 需要引用对象的时候是需要引用springboot的
	 */
	@Test
	public void testSeckillService(){
		Item item = itemMapper.selectByPrimaryKey(161);
		// System.out.println(item);
		User user = userMapper.selectByPhone("17808082128");
		// System.out.println(user);
		Promotion promotion = promotionMapper.selectByItemId(item.getId());
		ItemStock itemStock = itemStockMapper.selectByItemId(item.getId());
		//System.out.println(itemStock.getStock());
		//Order order = orderService.createOrder(user.getId(), item.getId(), 1,promotion.getId());
		//System.out.println(order);

	}

	/*

	 */
	@Test
	public void userTranJsom(){
		User user = userMapper.selectByPhone("17808082128");
		System.out.println(JSON.toJSONString(user));

	}

	@Test
	public void itemServiceRedis(){
		Item item = itemMapper.selectByPrimaryKey(161);
		//System.out.println(item);
		boolean b = itemService.increaseStockInCache(item.getId(), 10);
		// System.out.println(b);
		// Item itemInCache = itemService.findItemInCache(item.getId());
		boolean b1 = itemService.decreaseStockInCache(item.getId(), 5);
		// System.out.println(b1);
		// System.out.println(itemService.findItemInCache(item.getId()));
		//System.out.println(itemInCache);
		System.out.println(itemService.decreaseStock(item.getId(),1));
	}

	@Test
	public void userServiceRedis(){
		User user = userMapper.selectByPhone("1780808");

		User userFromCache = userService.findUserFromCache(user.getId());
		System.out.println(userFromCache);
	}

}
