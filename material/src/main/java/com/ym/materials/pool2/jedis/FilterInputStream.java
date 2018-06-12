package com.ym.materials.pool2.jedis;

import java.io.InputStream;

/**
 * Created by ym on 2018/6/12.
 */
public class FilterInputStream {
    private InputStream inputStream;

    public FilterInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }
}
