package com.ym.materials.pool2.redis.jedis.exceptions;

import com.ym.materials.pool2.redis.jedis.HostAndPort;

/**
 * Created by ym on 2018/6/13.
 */
public class JedisRedirectionException extends JedisException {
    private HostAndPort targetNode;
    private int slot;

    public JedisRedirectionException(String message, HostAndPort targetNode, int slot) {
        super(message);
        this.targetNode = targetNode;
        this.slot = slot;
    }

    public JedisRedirectionException(Throwable cause, HostAndPort targetNode, int slot) {
        super(cause);
        this.targetNode = targetNode;
        this.slot = slot;
    }

    public JedisRedirectionException(String message, Throwable cause, HostAndPort targetNode, int slot) {
        super(message, cause);
        this.targetNode = targetNode;
        this.slot = slot;
    }

    public HostAndPort getTargetNode() {
        return targetNode;
    }

    public int getSlot() {
        return slot;
    }
}
