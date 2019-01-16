package com.bingo.acache;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by bingo on 2019/1/11.
 * Time:2019/1/11
 */

public class Acache {
    private static final String KEY_CACHE = "key_cache";
    private SharedPreferences sharedPreferences;

    private Acache() {
    }

    private static Acache aCache;
    private static Gson mGson;

    public static Acache get() {
        if (aCache == null) {
            synchronized (Acache.class) {
                if (aCache == null) {
                    aCache = new Acache();
                    //mGson = new Gson();
                    //对json处理，避免数据类型被转换
                    mGson = new GsonBuilder()
                            .registerTypeAdapter(new TypeToken<TreeMap<String, Object>>() {
                                    }.getType(),
                                    new JsonDeserializer<TreeMap<String, Object>>() {
                                        @Override
                                        public TreeMap<String, Object> deserialize(JsonElement json, Type typeOfT,
                                                                                   JsonDeserializationContext context) throws JsonParseException {
                                            TreeMap<String, Object> treeMap = new TreeMap<>();
                                            JsonObject jsonObject = json.getAsJsonObject();
                                            Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
                                            for (Map.Entry<String, JsonElement> entry : entrySet) {
                                                treeMap.put(entry.getKey(), entry.getValue());
                                            }
                                            return treeMap;
                                        }
                                    }).create();
                }
            }
        }
        return aCache;
    }

    public void init(Context context) {
        sharedPreferences = context.getSharedPreferences(KEY_CACHE, Context.MODE_PRIVATE);
    }

    /**
     * 获取所有缓存数据
     *
     * @return String
     */
    public String getCache() {
        if (sharedPreferences == null) return null;
        return sharedPreferences.getString(KEY_CACHE, "");
    }

    /**
     * 获取值
     *
     * @param key
     * @return
     */
    public Object getCache(String key) {
        if (check()) {
            Map cacheMap = getCacheMap(getCache());
            return cacheMap.get(key);
        }
        return null;
    }

    /**
     * 获取值
     *
     * @param key
     * @return
     */
    public <T> T getObject(String key, Type typeOfT) {
        if (check()) {
            Map cacheMap = getCacheMap(getCache());
            if (cacheMap.get(key) != null) {
                return (T) mGson.fromJson(cacheMap.get(key).toString(), typeOfT);
            }
        }
        return null;
    }

    /**
     * 获取值
     *
     * @param key
     * @return
     */
    public Boolean getBoolean(String key) {
        if (check()) {
            Map cacheMap = getCacheMap(getCache());
            if (cacheMap.get(key) != null) {
                return Boolean.parseBoolean(cacheMap.get(key).toString());
            }
        }
        return null;
    }

    /**
     * 获取值
     *
     * @param key
     * @return
     */
    public Integer getInt(String key) {
        if (check()) {
            Map cacheMap = getCacheMap(getCache());
            if (cacheMap.get(key) != null) {
                return Integer.parseInt(cacheMap.get(key).toString());
            }
        }
        return null;
    }

    /**
     * 获取值
     *
     * @param key
     * @return
     */
    public Long getLong(String key) {
        if (check()) {
            Map cacheMap = getCacheMap(getCache());
            if (cacheMap.get(key) != null) {
                return Long.parseLong(cacheMap.get(key).toString());
            }
        }
        return null;
    }

    /**
     * 获取值
     *
     * @param key
     * @return
     */
    public String getString(String key) {
        if (check()) {
            Map cacheMap = getCacheMap(getCache());
            if (cacheMap.get(key) != null) {
                return cacheMap.get(key).toString();
            }
        }
        return null;
    }

    /**
     * 设置缓存
     *
     * @param map
     */
    public void setCache(Map<String, Object> map) {
        if (map == null || sharedPreferences == null) return;
        Map cacheMap = getCacheMap();
        Map<String, Object> temp = new HashMap<>();
        if (cacheMap != null) {
            Iterator<Map.Entry<String, Object>> entries = map.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, Object> entry = entries.next();
                String key = entry.getKey();
                Object value = entry.getValue();
                cacheMap.put(key, value);
            }
            temp.putAll(cacheMap);
        } else {
            temp.putAll(map);
        }
        String json = mGson.toJson(temp);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(KEY_CACHE, json);
        edit.commit();
    }

    /**
     * 设置缓存
     */
    public void setCache(String key, Object value) {
        if (check()) {
            Map cacheMap = getCacheMap(getCache());
            cacheMap.put(key, value);
            setCache(cacheMap);
        } else {
            Map<String, Object> map = new HashMap<>();
            map.put(key, value);
            setCache(map);
        }
    }

    /**
     * 判断是否存在键
     *
     * @param key
     * @return
     */
    public boolean isExist(String key) {
        if (TextUtils.isEmpty(key)) return false;
        if (check()) {
            Map cacheMap = getCacheMap(getCache());
            return cacheMap.containsKey(key);
        }
        return false;
    }

    /**
     * 是否存在缓存
     *
     * @return
     */
    private boolean check() {
        if (sharedPreferences == null) return false;
        String cache = sharedPreferences.getString(KEY_CACHE, "");
        return !TextUtils.isEmpty(cache);
    }

    /**
     * 将缓存字符串转换成map
     *
     * @param json
     * @return
     */
    private Map getCacheMap(String json) {
        //return mGson.fromJson(json, Map.class);
        return mGson.fromJson(json, new TypeToken<TreeMap<String, Object>>() {
        }.getType());
    }

    /**
     * 将缓存字符串转换成map
     *
     * @return
     */
    private Map getCacheMap() {
        String cache = getCache();
        if (TextUtils.isEmpty(cache)) return null;
        //return mGson.fromJson(cache, Map.class);
        return mGson.fromJson(cache, new TypeToken<TreeMap<String, Object>>() {
        }.getType());
    }

    /**
     * 清除
     */
    public void clear() {
        if (sharedPreferences == null) return;
        sharedPreferences.edit().clear().commit();
    }

    /**
     * 刪除值
     *
     * @param key
     */
    public void remove(String key) {
        if (check()) {
            Map cacheMap = getCacheMap(getCache());
            if (cacheMap != null || !cacheMap.isEmpty()) {
                if (cacheMap.containsKey(key)) {
                    cacheMap.remove(key);
                    setCache(cacheMap);
                }
            }
        }
    }
}