package com.ym.materials.pool2.redis.jedis.exceptions;

/**
 * Created by ym on 2018/6/13.
 */
public class JedisClusterException extends JedisException {
    public JedisClusterException(String msg) {
        super(msg);
    }

    public JedisClusterException(Throwable cause) {
        super(cause);
    }

    public JedisClusterException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
