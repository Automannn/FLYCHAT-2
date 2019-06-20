package com.gameex.dw.justtalk.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SharedPreference缓存工具类
 *
 * @author 丁威
 * @author lenovo
 * 参考：http://blog.csdn.net/a512337862/article/details/73633420
 */
public class SharedPreferenceUtil {

    private static SharedPreferenceUtil util;
    private static SharedPreferences pref;

    public SharedPreferenceUtil(Context context, String name) {
        if (name == null) {
            pref = PreferenceManager.getDefaultSharedPreferences(context);
            return;
        }
        pref = context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    /**
     * 初始化SharedPreferenceUtil,只初始化一次
     *
     * @param context 上下文
     * @param name    SharedPreference name
     */
    public static void getInstance(Context context, String name) {
        if (util == null) {
            util = new SharedPreferenceUtil(context, name);
        }
    }

    /**
     * 缓存日常数据
     *
     * @param key   键值
     * @param value 待缓存数据
     * @return boolean
     */
    public static boolean putData(String key, Object value) {
        boolean result;
        SharedPreferences.Editor editor = pref.edit();
        String type = value.getClass().getSimpleName();
        try {
            switch (type) {
                case "Boolean":
                    editor.putBoolean(key, (Boolean) value);
                    break;
                case "Long":
                    editor.putLong(key, (Long) value);
                    break;
                case "Float":
                    editor.putFloat(key, (Float) value);
                    break;
                case "String":
                    editor.putString(key, (String) value);
                    break;
                case "Integer":
                    editor.putInt(key, (Integer) value);
                    break;
                default:
                    editor.putString(key, GsonUtil.transObj(value));
                    break;
            }
            result = true;

        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        editor.apply();
        return result;
    }

    /**
     * 获取缓存的日常数据
     *
     * @param key          键值
     * @param defaultValue 默认值
     * @return obj
     */
    public static Object getData(String key, Object defaultValue) {
        Object result;
        String type = defaultValue.getClass().getSimpleName();
        try {
            switch (type) {
                case "Boolean":
                    result = pref.getBoolean(key, (Boolean) defaultValue);
                    break;
                case "Long":
                    result = pref.getLong(key, (Long) defaultValue);
                    break;
                case "Float":
                    result = pref.getFloat(key, (Float) defaultValue);
                    break;
                case "String":
                    result = pref.getString(key, (String) defaultValue);
                    break;
                case "Integer":
                    result = pref.getInt(key, (Integer) defaultValue);
                    break;
                default:
                    String json = pref.getString(key, "");
                    if (!json.equals("") && json.length() > 0) {
                        result = GsonUtil.getObj(json, defaultValue.getClass());
                    } else {
                        result = defaultValue;
                    }
                    break;
            }
        } catch (Exception e) {
            result = null;
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 缓存list集合
     *
     * @param key  键值
     * @param list 待缓存集合
     * @param <T>  t
     * @return boolean
     */
    public static <T> boolean putList(String key, List<T> list) {
        return putData(key, GsonUtil.transList(list));
    }

    /**
     * 获取list集合
     *
     * @param key  键值
     * @param type type
     * @param <T>  t
     * @return list
     */
    public static <T> List<T> getList(String key, Type type) {
        return GsonUtil.getList((String) getData(key, ""), type);
    }

    /**
     * 缓存HashMap集合
     *
     * @param key 键值
     * @param map 待缓存的map
     * @param <K> map键值
     * @param <V> map value
     * @return boolean
     */
    public static <K, V> boolean putHashMap(String key, Map<K, V> map) {
        return putData(key, GsonUtil.transHashMap(map));
    }

    /**
     * 获取HashMap集合
     *
     * @param key  键值
     * @param type type
     * @param <V>  value
     * @return HashMap
     */
    public static <V> HashMap<String, V> getHashMap(String key, Type type) {
        return GsonUtil.getHashMap((String) getData(key, ""), type);
    }
}
