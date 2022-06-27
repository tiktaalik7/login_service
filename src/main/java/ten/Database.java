package ten;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class Database {
    private static final JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
    private static final String ip = "210.115.43.158";
    private static final int port = 6379;
    private static final int timeOut = 10000;
    private static final String requirepass = "pass";
    private static final JedisPool pool = new JedisPool(jedisPoolConfig, ip, port, timeOut, requirepass);
    private static final ThreadLocal<Jedis> jedis = new ThreadLocal<>();

    private static final Database mInstance = new Database();

    public static Database getInstance() {
        return mInstance;
    }

    public Jedis getJedis() {
        var result = jedis.get();
        if (result == null) {
            result = pool.getResource();
            jedis.set(result);
        }
        return result;
    }
}
