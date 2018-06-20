package com.ym.materials.pool2.util;

import com.ym.materials.pool2.redis.jedis.exceptions.JedisConnectionException;
import com.ym.materials.pool2.redis.jedis.exceptions.JedisException;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by ym on 2018/6/11.
 */
public abstract class Pool<T> implements Closeable {

    protected GenericObjectPool<T> internalPool;

    public Pool() {
    }

    public Pool(GenericObjectPoolConfig poolConfig, PooledObjectFactory<T> factory) {
        init(poolConfig, factory);
    }

    private void init(GenericObjectPoolConfig poolConfig, PooledObjectFactory<T> factory) {
        if (internalPool != null) {
            try {
                closeInternalPool();
            } catch (Exception ignore) {

            }
        }
        internalPool = new GenericObjectPool<T>(factory, poolConfig);
    }

    @Override
    public void close() throws IOException {
        destroy();
    }

    private void destroy() {
        closeInternalPool();
    }

    public boolean isClosed() {
        return internalPool.isClosed();
    }

    private void closeInternalPool() {
        try {
            internalPool.close();
        } catch (Exception e) {
            throw new JedisException("can not destory the pool", e);
        }
    }

    public T getResource() {
        try {
            return internalPool.borrowObject();
        } catch (Exception e) {
            throw new JedisConnectionException("can not get a resouce from pool", e);
        }
    }

    public int getNumActive() {
        if (poolInactive()) {
            return -1;
        }
        return internalPool.getNumActive();
    }

    public int getNumIdle() {
        if (poolInactive()) {
            return -1;
        }
        return internalPool.getNumIdle();
    }

    public int getNumWaiters() {
        if (poolInactive()) {
            return -1;
        }
        return internalPool.getNumWaiters();
    }

    public long getMeanBorrowWaitTimeMillis() {
        if (poolInactive()) {
            return -1L;
        }
        return internalPool.getMeanBorrowWaitTimeMillis();
    }

    public long getMaxBorrowWaitTimeMillis() {
        if (poolInactive()) {
            return -1;
        }
        return internalPool.getMaxBorrowWaitTimeMillis();
    }

    public void addObjects(int count) {
        try {
            for (int i = 0; i < count; i++) {
                internalPool.addObject();
            }
        } catch (Exception e) {
            throw new JedisException("error trying to add idle objects", e);
        }
    }

    private boolean poolInactive() {
        return internalPool == null || internalPool.isClosed();
    }

    @Deprecated
    public void returnBrokenResource(final T resource) {
        if (resource != null) {
            returnBrokenResourceObject(resource);
        }
    }

    protected void returnBrokenResourceObject(final T resource) {
        try {
            internalPool.invalidateObject(resource);
        } catch (Exception e) {
            throw new JedisException("Could not return the resource to the pool", e);
        }
    }

    @Deprecated
    public void returnResource(final T resource) {
        if (resource != null) {
            returnResourceObject(resource);
        }
    }

    @Deprecated
    public void returnResourceObject(final T resource) {
        if (resource == null) {
            return;
        }
        try {
            internalPool.returnObject(resource);
        } catch (Exception e) {
            throw new JedisException("Could not return the resource to the pool", e);
        }
    }
}
