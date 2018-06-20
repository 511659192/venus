package com.ym.materials.pool2.redis.jedis.exceptions;

/**
 * Created by ym on 2018/6/11.
 */
public class JedisConnectionException extends JedisException {

    public JedisConnectionException(String msg) {
        super(msg);
    }

    public JedisConnectionException(Throwable cause) {
        super(cause);
    }

    public JedisConnectionException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
