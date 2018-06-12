package com.ym.materials.pool2.jedis.exceptions;

/**
 * Created by ym on 2018/6/12.
 */
public class JedisDataException extends JedisException {
    public JedisDataException(String msg) {
        super(msg);
    }

    public JedisDataException(Throwable cause) {
        super(cause);
    }

    public JedisDataException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
