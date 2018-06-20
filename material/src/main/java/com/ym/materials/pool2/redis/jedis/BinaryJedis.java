package com.ym.materials.pool2.redis.jedis;

import com.ym.materials.pool2.redis.jedis.exceptions.JedisDataException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by ym on 2018/6/11.
 */
public class BinaryJedis {

    protected Client client = null;
    protected Pipeline pipeline = null;

    public BinaryJedis(String host, int port, int connectTimeout, int soTimeout, boolean ssl, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier) {
        client = new Client(host, port, ssl, sslSocketFactory, sslParameters, hostnameVerifier);
        client.setConnectTimeout(connectTimeout);
        client.setSoTimeout(soTimeout);
    }

    public void connect() {
        client.connect();
    }

    public String auth(String password) {
        checkIsInMultiOrPipeline();
        client.auth(password);
        return client.getStatusCodeReply();
    }

    private void checkIsInMultiOrPipeline() {
        if (client.isInMulti()) {
            throw new JedisDataException(
                    "Cannot use Jedis when in Multi. Please use Transation or reset jedis state.");
        } else if (pipeline != null && pipeline.hasPipelinedResponse()) {
            throw new JedisDataException(
                    "Cannot use Jedis when in Pipeline. Please use Pipeline or reset jedis state .");
        }
    }

    public String select(int database) {
        checkIsInMultiOrPipeline();
        client.select(database);
        String statusCodeReply = client.getStatusCodeReply();
        client.setDb(database);
        return statusCodeReply;
    }

    public String setClientName(String clientName) {
        checkIsInMultiOrPipeline();
        client.setClientName(clientName);
        return client.getStatusCodeReply();
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    public String quit() {
        checkIsInMultiOrPipeline();
        client.quit();
        return client.getStatusCodeReply();
    }

    public void disConnect() {
        client.disConnect();
    }

    public long getDb() {
        return client.getDb();
    }

    public Client getClient() {
        return client;
    }

    public String ping() {
        checkIsInMultiOrPipeline();
        client.ping();
        return client.getStatusCodeReply();
    }

}
