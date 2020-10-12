package com.guman.raft.common.util;

import com.alibaba.fastjson.JSON;

/**
 * @author duanhaoran
 * @since 2020/4/12 6:29 PM
 */
public class JsonUtil {

    public static <T> T fromJson(String jsonStr, Class<T> clazz){
        return JSON.parseObject(jsonStr, clazz);
    }

    public static String toJson(Object object){
        return JSON.toJSONString(object);
    }
}
