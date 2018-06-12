package com.ym.materials.pool2.jedis;

import com.ym.materials.pool2.util.JedisURIHelper;
import com.ym.materials.pool2.util.Pool;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.net.URI;

import static com.ym.materials.pool2.jedis.Protocol.*;

/**
 * Created by ym on 2018/6/11.
 */
public class JedisPool extends Pool<Jedis> {

    public JedisPool(String host) {
        URI uri = URI.create(host);
        if (StringUtils.isBlank(uri.getScheme()) || StringUtils.isBlank(uri.getHost()) || uri.getPort() == -1) {
            JedisFactory jedisFactory = new JedisFactory(host, DEFAULT_PORT, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT, null, DEFAULT_DATABASE, null, false, null, null, null);
            this.internalPool = new GenericObjectPool<Jedis>(jedisFactory, new GenericObjectPoolConfig());
        } else {
            String host1 = uri.getHost();
            int port = uri.getPort();
            String password = JedisURIHelper.getPassword(uri);
            int database = JedisURIHelper.getDBIndex(uri);
            boolean ssl = uri.getScheme().equals("rediss");
            JedisFactory jedisFactory = new JedisFactory(host1, port, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT, password, database, null, ssl, null, null, null);
            this.internalPool = new GenericObjectPool<Jedis>(jedisFactory, new GenericObjectPoolConfig());
        }
    }

    public JedisPool(final String host, final int port) {
        super(new GenericObjectPoolConfig(), new JedisFactory(host, port, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT, null,
                DEFAULT_DATABASE, null, false, null, null, null));
    }

    public JedisPool(final GenericObjectPoolConfig poolConfig, final String host, final int port) {
        super(poolConfig, new JedisFactory(host, port, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT, null,
                DEFAULT_DATABASE, null, false, null, null, null));
    }

    public Jedis getResource() {
        Jedis jedis = super.getResource();
        jedis.setDataSource(this);
        return jedis;
    }
}
