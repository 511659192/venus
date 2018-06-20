package com.ym.materials.pool2.redis.jedis.exceptions;

import com.ym.materials.pool2.redis.jedis.HostAndPort;

/**
 * Created by ym on 2018/6/13.
 */
public class JedisMovedDataException extends JedisRedirectionException {

    public JedisMovedDataException(String message, HostAndPort targetNode, int slot) {
        super(message, targetNode, slot);
    }

    public JedisMovedDataException(Throwable cause, HostAndPort targetNode, int slot) {
        super(cause, targetNode, slot);
    }

    public JedisMovedDataException(String message, Throwable cause, HostAndPort targetNode, int slot) {
        super(message, cause, targetNode, slot);
    }
}
