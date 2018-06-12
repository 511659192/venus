package com.ym.materials.pool2.jedis;

import com.google.common.base.Preconditions;

import java.io.InputStream;

/**
 * Created by ym on 2018/6/12.
 */
public class RedisInputStream extends FilterInputStream {

    protected final byte[] buf;

    public RedisInputStream(InputStream inputStream) {
        this(inputStream, 8192);
    }

    public RedisInputStream(InputStream inputStream, int size) {
        super(inputStream);
        Preconditions.checkArgument(size > 0);
        buf = new byte[size];
    }
}
