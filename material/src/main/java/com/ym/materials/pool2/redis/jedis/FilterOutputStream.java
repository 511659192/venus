package com.ym.materials.pool2.redis.jedis;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by ym on 2018/6/12.
 */
public class FilterOutputStream extends OutputStream {

    protected OutputStream out;

    public FilterOutputStream() {
    }

    public FilterOutputStream(OutputStream out) {
        this.out = out;
    }

    @Override
    public void write(int b) throws IOException {

    }
}
