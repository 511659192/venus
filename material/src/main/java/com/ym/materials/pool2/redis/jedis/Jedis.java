package com.ym.materials.pool2.redis.jedis;

import com.ym.materials.pool2.util.Pool;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by ym on 2018/6/11.
 */
public class Jedis extends BinaryJedis {
    private Pool<Jedis> dataSource;

    public void setDataSource(Pool<Jedis> dataSource) {
        this.dataSource = dataSource;
    }

    public Jedis(String host, int port, int connectTimeout, int soTimeout, boolean ssl, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier) {
        super(host, port, connectTimeout, soTimeout, ssl, sslSocketFactory, sslParameters, hostnameVerifier);
    }

    public void close() {
        if (dataSource != null) {
            if (client.isBroken()) {
                this.dataSource.returnBrokenResource(this);
            } else {
                this.dataSource.returnResource(this);
            }
        } else {
            client.close();
        }
    }
}
