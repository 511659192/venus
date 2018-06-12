package com.ym.materials.pool2.jedis;

import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;

import static com.ym.materials.pool2.jedis.Protocol.Command.AUTH;

/**
 * Created by ym on 2018/6/12.
 */
public class BinaryClient extends Connection {

    private String password;

    private long db;

    public BinaryClient(String host, int port, boolean ssl, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier) {
        super(host, port, ssl, sslSocketFactory, sslParameters, hostnameVerifier);
    }

    public void connect() {
        if (!isConnected()) {
            super.connect();
            if (StringUtils.isNotBlank(password)) {
                auth(password);
            }
        }
    }

    private void auth(String password) {
        sendCommand(AUTH, password);
    }
}
