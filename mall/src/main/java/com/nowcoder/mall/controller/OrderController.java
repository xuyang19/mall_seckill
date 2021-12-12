package com.nowcoder.mall.controller;

import com.google.common.util.concurrent.RateLimiter;
import com.nowcoder.mall.common.BusinessException;
import com.nowcoder.mall.common.ErrorCode;
import com.nowcoder.mall.common.ResponseModel;
import com.nowcoder.mall.entity.User;
import com.nowcoder.mall.service.OrderService;
import com.nowcoder.mall.service.PromotionService;
import com.wf.captcha.SpecCaptcha;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/order")
@CrossOrigin(origins = "${nowcoder.web.path}", allowedHeaders = "*", allowCredentials = "true")
public class OrderController implements ErrorCode {
    private Logger logger = LoggerFactory.getLogger(OrderController.class);

    private RateLimiter rateLimiter = RateLimiter.create(1000);

    @Autowired
    private OrderService orderService;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @RequestMapping(path = "/captcha", method = RequestMethod.GET)
    public void getCaptcha(String token, HttpServletResponse response) {
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 4);

        if (token != null) {
            User user = (User) redisTemplate.opsForValue().get(token);
            if (user != null) {
                String key = "captcha:" + user.getId();
                redisTemplate.opsForValue().set(key, specCaptcha.text(), 1, TimeUnit.MINUTES);
            }
        }

        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            specCaptcha.out(os);
        } catch (IOException e) {
            logger.error("发送验证码失败：" + e.getMessage());
        }
    }

    @RequestMapping(path = "/token", method = RequestMethod.POST)
    @ResponseBody
    public ResponseModel generateToken(int itemId, int promotionId, String token, String captcha) {
        User user = (User) redisTemplate.opsForValue().get(token);

        if (StringUtils.isEmpty(captcha)) {
            throw new BusinessException(PARAMETER_ERROR, "请输入正确的验证码！");
        }

        String key = "captcha:" + user.getId();
        String realCaptcha = (String) redisTemplate.opsForValue().get(key);
        if (!captcha.equalsIgnoreCase(realCaptcha)) {
            throw new BusinessException(PARAMETER_ERROR, "请输入正确的验证码！");
        }

        String promotionToken = promotionService.generateToken(user.getId(), itemId, promotionId);
        if (StringUtils.isEmpty(promotionToken)) {
            throw new BusinessException(CREATE_ORDER_FAILURE, "下单失败！");
        }
        return new ResponseModel(promotionToken);
    }

//    @RequestMapping(path = "/create",method = RequestMethod.POST)
//    @ResponseBody
//    public ResponseModel create(/*HttpSession session,*/ int itemId, int amount, Integer promotionId, String token,String itemStockLogId){
//       // User user = (User) session.getAttribute("loginUser");
//
//         User user= (User)redisTemplate.opsForValue().get(token);
//        orderService.createOrder(user.getId(),itemId,amount,promotionId,itemStockLogId);
//        return new ResponseModel();
//    }

    @RequestMapping(path = "/create", method = RequestMethod.POST)
    @ResponseBody
    public ResponseModel create(/*HttpSession session, */
            int itemId, int amount, Integer promotionId, String promotionToken, String token) {
//        User user = (User) session.getAttribute("loginUser");
//        if (!rateLimiter.tryAcquire()) {
//            throw new BusinessException(OUT_OF_LIMIT, "服务器繁忙，请稍后再试！");
//        }

        User user = (User) redisTemplate.opsForValue().get(token);
        logger.debug("登录用户 [" + token + ": " + user + "]");

        if (promotionId != null) {
            String key = "promotion:token:" + user.getId() + ":" + itemId + ":" + promotionId;
            String realPromotionToken = (String) redisTemplate.opsForValue().get(key);
            if (StringUtils.isEmpty(promotionToken) || !promotionToken.equals(realPromotionToken)) {
                throw new BusinessException(CREATE_ORDER_FAILURE, "下单失败！");
            }
        }

//        Future future = taskExecutor.submit(new Callable() {
//            @Override
//            public Object call() throws Exception {
////              orderService.createOrder(user.getId(), itemId, amount, promotionId);
//                //orderService.createOrderAsync(user.getId(), itemId, amount, promotionId);
//                orderService.creatOrderAsync(user.getId(), itemId, amount, promotionId);
//                return null;
//            }
//        });

        Future fu = taskExecutor.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                orderService.creatOrderAsync(user.getId(), itemId, amount, promotionId);
                return null;
            }
        });

        try {
            fu.get();
        } catch (Exception e) {
            throw new BusinessException(UNDEFINED_ERROR, "下单失败！");
        }

        return new ResponseModel();
    }
}
