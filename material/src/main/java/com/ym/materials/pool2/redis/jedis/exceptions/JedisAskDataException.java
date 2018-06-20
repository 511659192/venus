package com.ym.materials.pool2.redis.jedis.exceptions;

import com.ym.materials.pool2.redis.jedis.HostAndPort;
import com.ym.materials.pool2.redis.jedis.exceptions.JedisException;

/**
 * Created by ym on 2018/6/13.
 */
public class JedisAskDataException extends JedisRedirectionException {

    public JedisAskDataException(String message, HostAndPort targetNode, int slot) {
        super(message, targetNode, slot);
    }

    public JedisAskDataException(Throwable cause, HostAndPort targetNode, int slot) {
        super(cause, targetNode, slot);
    }

    public JedisAskDataException(String message, Throwable cause, HostAndPort targetNode, int slot) {
        super(message, cause, targetNode, slot);
    }
}
