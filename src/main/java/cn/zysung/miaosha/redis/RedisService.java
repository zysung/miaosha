package cn.zysung.miaosha.redis;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

//Redis服务实现get、set、incr、decr多个接口
@Service
public class RedisService {

        @Autowired
        JedisPool jedisPool;

    /**
     * 获取单个对象
     * @param prefix
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
        public <T> T get(KeyPrefix prefix,String key,Class<T> clazz){
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                //生成真正的key
                String realKey = prefix.getPrefix()+key;
                String str = jedis.get(realKey);
                T t = stringToBean(str,clazz);
                return t;
            }finally {
                returnToPool(jedis);
            }
        }

    /**
     * 设置对象
     * @param prefix
     * @param key
     * @param value
     * @param <T>
     * @return
     */
        public <T> boolean set(KeyPrefix prefix,String key,T value){
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                String str = beanToString(value);
                if(str==null || str.length()==0){
                    return false;
                }
                String realKey = prefix.getPrefix()+key;
                int seconds = prefix.expireSeconds();
                if(seconds<=0){   //设置过期时间
                    jedis.set(realKey,str);
                }else {
                    jedis.setex(realKey,seconds,str);
                }
                return true;
            }finally {
                returnToPool(jedis);
            }

        }

    /**
     * 判断是否存在
     * @param prefix
     * @param key
     * @param <T>
     * @return
     */
        public <T> boolean exists(KeyPrefix prefix,String key){
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                String realKey = prefix.getPrefix()+key;
                jedis.exists(realKey);
                return true;
            }finally {
                returnToPool(jedis);
            }
        }

    /**
     * 增加value的值,若value是字符串，变0再+1
     * @param prefix
     * @param key
     * @param <T>
     * @return
     */
        public <T> Long incr(KeyPrefix prefix,String key){
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                String realKey = prefix.getPrefix()+key;
                return jedis.incr(realKey);
            }finally {
                returnToPool(jedis);
            }
        }
    /**
     * 增加value的值
     * @param prefix
     * @param key
     * @param <T>
     * @return
     */
        public <T> Long decr(KeyPrefix prefix,String key){
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                String realKey = prefix.getPrefix()+key;
                return jedis.decr(realKey);
            }finally {
                returnToPool(jedis);
            }
        }

    /**
     * 删除某键值对
     * @param prefix
     * @param key
     * @return
     */
    public boolean delete(KeyPrefix prefix,String key){
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                String realKey = prefix.getPrefix()+key;
                long ret = jedis.del(realKey);
                return ret >0;
            }finally {
                returnToPool(jedis);
            }
        }


        public static <T> String beanToString(T value) {
            if(value == null){
                return null;
            }
            Class<?> clz = value.getClass();
            if(clz == int.class || clz == Integer.class){
                return ""+value;
            }else if(clz == String.class){
                return (String)value;
            }else if(clz == long.class || clz == Long.class){
                return ""+value;
            }else {
                return JSON.toJSONString(value);
            }
        }

        public static <T> T stringToBean(String str,Class<T> clz) {
            if(str==null || str.length()==0 || clz== null){
                return null;
            }
            if(clz == int.class || clz == Integer.class){
                return (T) Integer.valueOf(str);
            }else if(clz == String.class){
                return (T) str;
            }else if(clz == long.class || clz == Long.class){
                return (T) Long.valueOf(str);
            }else {
                return JSON.toJavaObject(JSON.parseObject(str),clz);
            }
        }


        private void returnToPool(Jedis jedis){
            if(jedis!=null) {
                jedis.close();
            }
        }








}
