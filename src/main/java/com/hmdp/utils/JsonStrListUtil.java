package com.hmdp.utils;

import cn.hutool.json.JSONUtil;
import com.hmdp.entity.ShopType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @Author: SongZeyao
 * @Date: 2023/3/2 - 21:12
 * @Description: String集合与对象集合的互相转换
 */
public class JsonStrListUtil {

    /**
     * 将对象集合转换为jsonString集合
     * @param objectList 对象集合
     * @param <E> 对象类型
     * @return jsonString集合
     */
    public static <E>List<String> toJsonStrList(Collection<E> objectList) {
        List<String> jsonStrList = new ArrayList<>();
        for (E e : objectList) {
            jsonStrList.add(JSONUtil.toJsonStr(e));
        }
        return jsonStrList;

    }

    /**
     * 将jsonString集合转换为对象集合
     * @param jsonStringList jsonString集合
     * @param beanClass 对象类型字节码
     * @param <T> 对象类型
     * @return 对象集合
     */
    public static <T>List<T> toBeanList(Collection<String> jsonStringList, Class<T> beanClass){
        List<T> beanList = new ArrayList<>();
        for (String jsonString : jsonStringList) {
            beanList.add(JSONUtil.toBean(jsonString,beanClass));
        }
        return beanList;
    }
}
