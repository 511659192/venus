package com.ym.materials.pool2.redis.jedis.exceptions;

/**
 * Created by ym on 2018/6/13.
 */
public class JedisBusyException extends JedisException {
    public JedisBusyException(String msg) {
        super(msg);
    }

    public JedisBusyException(Throwable cause) {
        super(cause);
    }

    public JedisBusyException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
