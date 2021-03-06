package com.ym.materials.pool2.util;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * Created by ym on 2018/6/12.
 */
public final class SafeEncoder {

    public static byte[] encode(String string) {
        Preconditions.checkArgument(StringUtils.isNotBlank(string));
        return string.getBytes();
    }

    public static String encode(final byte[] data) {
        return new String(data, Charsets.UTF_8);
    }
}
