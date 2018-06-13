package com.ym.materials.pool2.jedis;

import com.ym.materials.pool2.jedis.exceptions.JedisException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

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
            if (StringUtils.isNotBlank(password)) {
                jedis.auth(password);
            }
            if (database != 0) {
                jedis.select(database);
            }
            if (StringUtils.isNotBlank(clientName)) {
                jedis.setClientName(clientName);
            }
        } catch (JedisException e) {
            jedis.close();
            throw e;
        }
        return new DefaultPooledObject<Jedis>(jedis);
    }

    @Override
    public void destroyObject(PooledObject<Jedis> p) throws Exception {
        BinaryJedis jedis = p.getObject();
        if (jedis.isConnected()) {
            try {
                jedis.quit();
            } catch (Exception ignore) {

            } finally {
                jedis.disConnect();
            }
        }
    }

    @Override
    public boolean validateObject(PooledObject<Jedis> p) {
        BinaryJedis jedis = p.getObject();
        try {
            HostAndPort hostAndPort = this.hostAndPort.get();
            String host = jedis.getClient().getHost();
            int port = jedis.getClient().getPort();
            return StringUtils.equals(hostAndPort.getHost(), host) &&
                    hostAndPort.getPort() ==  port &&
                    jedis.isConnected() &&
                    jedis.ping().equals("PONG");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void activateObject(PooledObject<Jedis> p) throws Exception {
        BinaryJedis jedis = p.getObject();
        if (jedis.getDb() != database) {
            jedis.select(database);
        }
    }

    @Override
    public void passivateObject(PooledObject<Jedis> p) throws Exception {
        // TODO maybe should select db 0? Not sure right now.
    }
}
