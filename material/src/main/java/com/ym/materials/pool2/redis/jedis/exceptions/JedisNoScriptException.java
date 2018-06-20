package com.ym.materials.pool2.redis.jedis.exceptions;

/**
 * Created by ym on 2018/6/13.
 */
public class JedisNoScriptException extends JedisException {
    public JedisNoScriptException(String msg) {
        super(msg);
    }

    public JedisNoScriptException(Throwable cause) {
        super(cause);
    }

    public JedisNoScriptException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
