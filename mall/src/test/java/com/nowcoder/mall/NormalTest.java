package com.nowcoder.mall;

//import net.minidev.json;
import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

public class NormalTest {
    @Test
    public void getBytesTest(){
        String str = "我是小明";
        System.out.println(JSON.toJSONString(str));

    }

}
