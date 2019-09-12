package com.yolo.common.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.Response;

/**
 * @author jeffer
 *
 */
public class RedisUtils {

	private static final Logger logger = LoggerFactory.getLogger(RedisUtils.class);
	
	private static JedisPool jedisPool = null;
	private static Properties properties = null;
	public static String redisPoolHost = "localhost";
	public static int redisPoolPort = 6379;
	public static int redisPoolMaxTotal = 50;
	public static int redisPoolMaxIdle = 5;
	public static boolean redisPoolTestOnReturn = true;
	public static boolean redisPoolTestOnBorrow = true;
	
	public static void init(Properties properties){
		RedisUtils.properties = properties;
		if(RedisUtils.properties!=null){
			redisPoolHost = properties.getProperty("redisPoolHost", redisPoolHost);
			redisPoolPort = Integer.parseInt(properties.getProperty("redisPoolPort", Integer.toString(redisPoolPort)));
			redisPoolMaxTotal = Integer.parseInt(properties.getProperty("redisPoolMaxTotal", Integer.toString(redisPoolMaxTotal)));
			redisPoolMaxIdle = Integer.parseInt(properties.getProperty("redisPoolMaxIdle",Integer.toString(redisPoolMaxIdle)));
			redisPoolTestOnReturn = Boolean.parseBoolean(properties.getProperty("redisPoolTestOnReturn", Boolean.toString(redisPoolTestOnReturn)));
			redisPoolTestOnBorrow = Boolean.parseBoolean(properties.getProperty("redisPoolTestOnBorrow", Boolean.toString(redisPoolTestOnBorrow)));
		}
		logger.info("properties:"+JSON.toJSONString(RedisUtils.properties));
	}
	
	private static JedisPool getJedisPool(){
		if(RedisUtils.jedisPool == null){
			synchronized (RedisUtils.class) {
				if(RedisUtils.jedisPool == null){
					JedisPoolConfig config = new JedisPoolConfig();
					// 控制一个pool可分配多少个jedis实例，通过pool.getResource()来获取；
					// 如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
					config.setMaxTotal(redisPoolMaxTotal);
					// 控制一个pool最多有多少个状态为idle(空闲的)的jedis实例。
					config.setMaxIdle(redisPoolMaxIdle);
					// 表示当borrow(引入)一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；
					config.setTestOnReturn(redisPoolTestOnReturn);
					// 在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
					config.setTestOnBorrow(redisPoolTestOnBorrow);
					config.setMaxWaitMillis(100000);
					JedisPool pool = new JedisPool(config, redisPoolHost, redisPoolPort, Protocol.DEFAULT_TIMEOUT, null, 0);
					RedisUtils.jedisPool = pool;
				}
			}
		}
		return RedisUtils.jedisPool;
	}
	
	public static void closePool() {
		if(RedisUtils.jedisPool !=null){
			synchronized (RedisUtils.class) {
				if(!RedisUtils.jedisPool.isClosed()){
					RedisUtils.jedisPool.close();
				}
			}
		}
	}
	
	private static Jedis getJedis(){
		JedisPool pool = RedisUtils.getJedisPool();
		return pool.getResource();
	}
	
	/**
	 * *********************************************
	 * 
	 * @Title: returnResource
	 * @Description: return a connection to pool
	 * @param redis
	 */
	private static void returnResource(Jedis redis) {
		if (redis != null) {
			redis.close();
		}
	}
	
	
	
	
	
	
	
	public static void delByKeypatten(String pattern){
		Set<String> keys = RedisUtils.getKeys(pattern);
		for(String key : keys){
			del(key);
		}
	}

	public static Set<String> getKeys(String pattern){
		Set<String> keys = new HashSet<String>();
		Jedis jedis = RedisUtils.getJedis();
		try {
			keys = jedis.keys(pattern);
		}finally {
			RedisUtils.returnResource(jedis);
		}
		return keys;		
	}

	public static String set(String key, String value) {
		String result = null;
		Jedis jedis = RedisUtils.getJedis();
		try {
			result = jedis.set(key, value);
		}finally {
			RedisUtils.returnResource(jedis);
		}
		return result;
	}

	public static String setex(String key,int seconds, String value) {
        String result = null;
        Jedis jedis = RedisUtils.getJedis();
        try {
            result = jedis.setex(key, seconds, value);
        }finally {
        	RedisUtils.returnResource(jedis);
        }
        return result;
    }
	
	
	
	/**
	 * *********************************************
	 * 
	 * @Title: sadd
	 * @Description: Add one member into set
	 * @param key
	 * @param value
	 * @return
	 */
	public static Long sadd(String key, String value) {
		Long result = null;
		Jedis jedis = RedisUtils.getJedis();
		try {
			result = jedis.sadd(key, value);
		}finally {
			RedisUtils.returnResource(jedis);
		}
		return result;
	}

	/**
	 * *********************************************
	 * 
	 * @Title: sismember
	 * @Description: Validate if the member exist in the set
	 * @param key
	 * @param member
	 * @return
	 */
	public static Boolean sismember(String key, String member) {
		Boolean result = null;
		Jedis jedis = RedisUtils.getJedis();
		try {
			result = jedis.sismember(key, member);
		}finally {
			RedisUtils.returnResource(jedis);
		}
		return result;
	}

	/**
	 * *********************************************
	 * 
	 * @Title: hlenByKey
	 * @Description: Get count of map's keys
	 * @param key
	 * @return
	 */
	public static Long hlenByKey(String key) {
		Long length = null;
		Jedis jedis = RedisUtils.getJedis();
		try {
			length = jedis.hlen(key);
		}finally {
			RedisUtils.returnResource(jedis);
		}
		return length;
	}

	/**
	 * *********************************************
	 * 
	 * @Title: hgetByKeyField
	 * @Description: Get value from map by key and map's field
	 * @param key:
	 *            redis's key
	 * @param field:
	 *            map's key
	 * @return
	 */
	public static String hgetByKeyField(String key, String field) {
		String value = null;
		Jedis jedis = RedisUtils.getJedis();
		try {
			value = jedis.hget(key, field);
		}finally {
			RedisUtils.returnResource(jedis);
		}
		return value;
	}

	public static Map<String, String> hgetallByKey(String key) {
		Map<String, String> value = null;
		Jedis jedis = RedisUtils.getJedis();
		try {
			Pipeline ppl = jedis.pipelined();
			Response<Map<String, String>> rs = ppl.hgetAll(key);
			ppl.sync();
			value = rs.get();
		}finally {
			RedisUtils.returnResource(jedis);
		}
		return value;
	}


	public static long hset(String key, String field, String value) {
        long result = 0;
        Jedis jedis = RedisUtils.getJedis();
        try {
            result = jedis.hset(key, field, value);
        }finally {
            RedisUtils.returnResource(jedis);
        }
        return result;
    }
	
	/**
	 * *********************************************
	 * 
	 * @Title: get
	 * @Description: Get value by key
	 * @param key
	 * @return
	 */
	public static String get(String key) {
		String value = null;
		Jedis jedis = RedisUtils.getJedis();
		try {
			value = jedis.get(key);
		}finally {
			// 返还到连接池
			RedisUtils.returnResource(jedis);
		}

		return value;
	}

	public static long del(String key) {
		long result = 0;
		Jedis jedis = RedisUtils.getJedis();
		try {
			result = jedis.del(key);
		}finally {
			// 返还到连接池
			RedisUtils.returnResource(jedis);
		}
		return result;
	}
	
	public static long hdel(String rkey,String hkey) {
        long result = 0;
        Jedis jedis = RedisUtils.getJedis();
        try {
            result = jedis.hdel(rkey,hkey);
        }finally {
            // 返还到连接池
            RedisUtils.returnResource(jedis);
        }
        return result;
    }
	
	public static long pipelineDel(String rkey,String hkey) {
        Response<Long> result = null ;
        Jedis jedis = RedisUtils.getJedis();
        Pipeline pipeline = null;
        try {
            pipeline = jedis.pipelined();
            result = pipeline.hdel(rkey,hkey);
            pipeline.sync();
        }finally {
            // 返还到连接池
            RedisUtils.returnResource(jedis);
        }
        return result.get();
    }

	public  static void psubscribe(JedisPubSub listen,String pattern) {
        Jedis jedis = RedisUtils.getJedis();
        try {
            jedis.psubscribe(listen,pattern);
        }finally {
            // 返还到连接池
            RedisUtils.returnResource(jedis);
        }
    }
	
	public  static void publish(String channel ,String message) {
        Jedis jedis = RedisUtils.getJedis();
        try {
            jedis.publish(channel, message);
        }finally {
            // 返还到连接池
            RedisUtils.returnResource(jedis);
        }
    }

	public static void setexRawString(String key, int seconds, String value) {
		Jedis jedis = RedisUtils.getJedis();
		try {
			jedis.setex(key, seconds, value);
		}finally {
			RedisUtils.returnResource(jedis);
		}
	}

	public static void expire(String key, int seconds) {
		Jedis jedis = RedisUtils.getJedis();
		try {
			jedis.expire(key, seconds);
		}finally {
			RedisUtils.returnResource(jedis);
		}
	}

	public static void lpush(String key, String... items) {
		Jedis jedis = RedisUtils.getJedis();
		try {
			jedis.lpush(key, items);
		}finally {
			RedisUtils.returnResource(jedis);
		}
	}

	public static List<String> lrange(String key, long start, long end) {
		List<String> result = null;
		Jedis jedis = RedisUtils.getJedis();
		try {
			result = jedis.lrange(key, start, end);
		}finally {
			RedisUtils.returnResource(jedis);
		}
		return result;
	}
	
	
}
