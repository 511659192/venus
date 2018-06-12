package com.ym.materials.pool2.jedis;

import com.google.common.base.Preconditions;

import java.io.OutputStream;

/**
 * Created by ym on 2018/6/12.
 */
public class RedisOutputStream extends FilterOutputStream {

    protected final byte[] buf;

    public RedisOutputStream(OutputStream outputStream) {
        this(outputStream, 8192);
    }

    public RedisOutputStream(OutputStream outputStream, int size) {
        super(outputStream);
        Preconditions.checkArgument(size > 0);
        buf = new byte[size];
    }
}
