package com.ym.materials.pool2.redis.jedis.exceptions;

/**
 * Created by ym on 2018/6/11.
 */
public class JedisException extends RuntimeException {

    public JedisException(String msg) {
        super(msg);
    }

    public JedisException(Throwable cause) {
        super(cause);
    }

    public JedisException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
