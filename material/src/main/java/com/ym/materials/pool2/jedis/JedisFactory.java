package com.ym.materials.pool2.jedis;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by ym on 2018/6/11.
 */
public class JedisFactory implements PooledObjectFactory<Jedis> {

    private final AtomicReference<HostAndPort> hostAndPort = new AtomicReference<HostAndPort>();
    private final int connectionTimeout;
    private final int soTimeout;
    private final String password;
    private final int database;
    private final String clientName;
    private final boolean ssl;
    private final SSLSocketFactory sslSocketFactory;
    private final SSLParameters sslParameters;
    private final HostnameVerifier hostnameVerifier;


    public JedisFactory(final String host, final int port, final int connectionTimeout,
                        final int soTimeout, final String password, final int database, final String clientName,
                        final boolean ssl, final SSLSocketFactory sslSocketFactory, final SSLParameters sslParameters,
                        final HostnameVerifier hostnameVerifier) {
        this.hostAndPort.set(new HostAndPort(host, port));
        this.connectionTimeout = connectionTimeout;
        this.soTimeout = soTimeout;
        this.password = password;
        this.database = database;
        this.clientName = clientName;
        this.ssl = ssl;
        this.sslSocketFactory = sslSocketFactory;
        this.sslParameters = sslParameters;
        this.hostnameVerifier = hostnameVerifier;
    }


    @Override
    public PooledObject<Jedis> makeObject() throws Exception {
        HostAndPort hostAndPort = this.hostAndPort.get();
        Jedis jedis = new Jedis(hostAndPort.getHost(), hostAndPort.getPort(), connectionTimeout, soTimeout, ssl, sslSocketFactory, sslParameters, hostnameVerifier);
        try {
            jedis.connect();
        } catch (Exception e) {

        }
        return null;
    }

    @Override
    public void destroyObject(PooledObject<Jedis> p) throws Exception {

    }

    @Override
    public boolean validateObject(PooledObject<Jedis> p) {
        return false;
    }

    @Override
    public void activateObject(PooledObject<Jedis> p) throws Exception {

    }

    @Override
    public void passivateObject(PooledObject<Jedis> p) throws Exception {

    }
}
