package com.gameex.dw.justtalk.util;

import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GsonUtil {

    private static GsonUtil util;
    private static Gson gson;

    private GsonUtil() {
        gson = new Gson();
    }

    /**
     * 初始化GsonUtil，只初始化一次
     */
    private static void getInstance() {
        if (util == null) {
            util = new GsonUtil();
        }
    }

    /**
     * 转化日常数据类型以外的数据类型
     *
     * @param value 带转化的数据
     * @return 字符串
     */
    public static String transObj(Object value) {
        gson = new Gson();
        return gson.toJson(value);
    }

    /**
     * 获取日常数据类型以外的数据
     *
     * @param json 待获取的数据
     * @param c    class
     * @return obj
     */
    public static Object getObj(String json, Class c) {
        gson = new Gson();
        return gson.fromJson(json, c);
    }

    /**
     * list转json串
     *
     * @param list 要转化的集合
     * @param <T>  集合元素类型
     * @return string
     */
    public static <T> String transList(List<T> list) {
        gson = new Gson();
        return gson.toJson(list);
    }

    /**
     * 获取对应list
     *
     * @param json 要获取的list串
     * @param type type
     * @param <T>  T
     * @return list
     */
    public static <T> List<T> getList(String json, Type type) {
        gson = new Gson();
        return gson.fromJson(json, type);
    }

    /**
     * map转json
     *
     * @param map 需要转化的map
     * @param <K> key
     * @param <V> value
     * @return string
     */
    public static <K, V> String transHashMap(Map<K, V> map) {
        gson = new Gson();
        return gson.toJson(map);
    }

    /**
     * 获取map
     *
     * @param json 需要获取的map串
     * @param type type
     * @param <V>  value
     * @return map
     */
    public static <V> HashMap<String, V> getHashMap(String json, Type type) {
        gson = new Gson();
        return gson.fromJson(json, type);
    }

}
