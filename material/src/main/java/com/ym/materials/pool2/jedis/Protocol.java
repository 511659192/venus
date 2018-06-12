package com.ym.materials.pool2.jedis;

import com.ym.materials.pool2.util.SafeEncoder;

/**
 * Created by ym on 2018/6/11.
 */
public final class Protocol {

    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 6379;
    public static final int DEFAULT_TIMEOUT = 2000;
    public static final int DEFAULT_DATABASE = 0;

    public static void sendCommand(RedisOutputStream outputStream, Command cmd, byte[][] args) {

    }

    public static enum Command {
        PING, SET, GET, AUTH;

        public final byte[] raw;

        Command() {
            raw = SafeEncoder.encode(this.name());
        }

    }
}
