package com.ym.materials.pool2.jedis;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by ym on 2018/6/11.
 */
public class BinaryJedis {

    protected Client client = null;

    public BinaryJedis(String host, int port, int connectTimeout, int soTimeout, boolean ssl, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier) {
        client = new Client(host, port, ssl, sslSocketFactory, sslParameters, hostnameVerifier);
        client.setConnectTimeout(connectTimeout);
        client.setSoTimeout(soTimeout);
    }

    public void connect() {
        client.connect();
    }
}
