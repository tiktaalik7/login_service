package ten;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class Database {
    private static final JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
    private static final JedisPool pool = new JedisPool(jedisPoolConfig, "210.115.43.158", 6379, 10000, "pass");
    private static final ThreadLocal<Jedis> jedis = new ThreadLocal<>();
    
    private static Database mInstance = new Database();

    /**
     * return Database instance for singleton design pattern
     */
    public static Database getInstance() {
        return mInstance;
    }

    /**
     * return Jedis instance thread by thread
     */
    public Jedis getJedis() {
        var result = jedis.get();
        if (result == null) {
            result = pool.getResource();
            jedis.set(result);
        }
        return result;
    }
}
