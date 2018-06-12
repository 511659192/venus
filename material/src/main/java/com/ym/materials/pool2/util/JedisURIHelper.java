package com.ym.materials.pool2.util;

import org.apache.commons.lang3.StringUtils;

import java.net.URI;

/**
 * Created by ym on 2018/6/11.
 */
public class JedisURIHelper {

    private static final int DEFAULT_DB = 0;

    public static String getPassword(URI uri) {
        String userInfo = uri.getUserInfo();
        if (StringUtils.isNotBlank(userInfo)) {
            return userInfo.split(":", 2)[1];
        }
        return null;
    }

    public static int getDBIndex(URI uri) {
        String[] split = uri.getPath().split("/", 2);
        if (split.length > 1) {
            String dbIndexStr = split[1];
            if (StringUtils.isBlank(dbIndexStr)) {
                return DEFAULT_DB;
            }

            return Integer.parseInt(dbIndexStr);
        }
        return DEFAULT_DB;
    }
}
