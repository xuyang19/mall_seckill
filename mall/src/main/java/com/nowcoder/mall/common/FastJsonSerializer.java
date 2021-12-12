package com.nowcoder.mall.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.nio.charset.Charset;

public class FastJsonSerializer implements RedisSerializer<Object> {

    public static final Charset UTF_8 = Charset.forName("UTF-8");

    @Override
    public byte[] serialize(Object obj) throws SerializationException {
        if (obj == null) {
            return null;
        }

        String json = JSON.toJSONString(obj, SerializerFeature.WriteClassName);
        return json.getBytes(UTF_8);
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length <= 0) {
            return null;
        }

        String json = new String(bytes, UTF_8);
        return JSON.parseObject(json, Object.class, Feature.SupportAutoType);
    }
}
