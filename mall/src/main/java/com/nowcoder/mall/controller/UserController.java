package com.nowcoder.mall.controller;

import com.nowcoder.mall.common.BusinessException;
import com.nowcoder.mall.common.ErrorCode;
import com.nowcoder.mall.common.ResponseModel;
import com.nowcoder.mall.common.Toolbox;
import com.nowcoder.mall.dao.UserMapper;
import com.nowcoder.mall.entity.User;
import com.nowcoder.mall.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping(path = "/user")
@CrossOrigin(origins = "${nowcoder.web.path}", allowedHeaders = "*", allowCredentials = "true")
public class UserController implements ErrorCode {

    private Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;


    /*
    产生四位的验证码
     */
    private String generateOTP(){
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 4; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /*
    将验证码绑定到session 返回给前端
     */
    @RequestMapping(path = "/otp/{phone}",method = RequestMethod.GET)
    public ResponseModel getOTP(@PathVariable("phone") String phone/*, HttpSession session*/){
        // 生成OTP
        String otp = this.generateOTP();
        // 绑定OTP
        //session.setAttribute(phone,otp);
        redisTemplate.opsForValue().set(phone,otp,5, TimeUnit.MINUTES);
        // 发送OTP
        logger.info("[牛客网]尊敬的{}您好，您的验证码是{}，请查收！",phone,otp);
        return new ResponseModel();
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST )
    @ResponseBody
    public ResponseModel register(String otp, User user/*,HttpSession session*/){
       // String realOTP = (String)session.getAttribute(user.getPhone());
        String realOTP = (String)redisTemplate.opsForValue().get(user.getPhone());
        if(StringUtils.isEmpty(realOTP) ||
        StringUtils.isEmpty(otp) || !StringUtils.equals(otp,realOTP)){
            throw new BusinessException(PARAMETER_ERROR,"验证码错误！");
        }

        // 密码加密
        user.setPassword(Toolbox.md5(user.getPassword()));

        // 注册用户
        userService.register(user);

        return new ResponseModel();
    }

    /*
    登录操作
     */
    @RequestMapping(path = "/login",method = RequestMethod.POST)
    @ResponseBody
    public ResponseModel login(String phone,String password/*,HttpSession session*/){
        if(StringUtils.isEmpty(phone)
                || StringUtils.isEmpty(password)){
            throw new BusinessException(PARAMETER_ERROR,"参数不合法！");
        }

        // 获得user
        String md5Pwd = Toolbox.md5(password);
        User user = userService.login(phone, md5Pwd);

        // 记录user的状态
        // token 生成
        String token = UUID.randomUUID().toString().replace("-","");
       // session.setAttribute("loginUser",user);
        redisTemplate.opsForValue().set(token,user,1,TimeUnit.DAYS);
        return new ResponseModel(token);
    }
    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    @ResponseBody
    public ResponseModel logout(/*HttpSession session*/String token) {
        //session.invalidate();
        if(StringUtils.isNotEmpty(token)){
            redisTemplate.delete(token);
        }

        return new ResponseModel();
    }

    @RequestMapping(path = "/status", method = RequestMethod.GET)
    @ResponseBody
    public ResponseModel getUser(/*HttpSession session*/String token) {
        // User user = (User) session.getAttribute("loginUser");
        User user = (User) redisTemplate.opsForValue().get(token);
        return new ResponseModel(user);
    }
}
