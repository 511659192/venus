package com.ym.materials.pool2.redis.jedis;

/**
 * Created by ym on 2018/6/11.
 */
public class HostAndPort {

    private String host;
    private int port;

    public HostAndPort(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public static String[] extractParts(String from){
        int idx = from.lastIndexOf(":");
        String host = idx != -1 ? from.substring(0, idx)  : from;
        String port = idx != -1 ? from.substring(idx + 1) : "";
        return new String[] { host, port };
    }
}
