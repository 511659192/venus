package com.ym.materials.pool2.redis.jedis;

import com.ym.materials.pool2.redis.jedis.Protocol.Keyword;
import com.ym.materials.pool2.util.SafeEncoder;
import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;

import static com.ym.materials.pool2.redis.jedis.Protocol.Command.*;

/**
 * Created by ym on 2018/6/12.
 */
public class BinaryClient extends Connection {

    private String password;

    private long db;
    private boolean isInMulti;

    public BinaryClient(String host, int port, boolean ssl, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier) {
        super(host, port, ssl, sslSocketFactory, sslParameters, hostnameVerifier);
    }

    public void connect() {
        if (!isConnected()) {
            super.connect();
            if (StringUtils.isNotBlank(password)) {
                auth(password);
                getStatusCodeReply();
            }
            if (db > 0) {
                select(Long.valueOf(db).intValue());
                getStatusCodeReply();
            }
        }
    }

    public void select(final int index) {
        sendCommand(SELECT, SafeEncoder.encode(String.valueOf(index)));
    }

    public void auth(String password) {
        sendCommand(AUTH, password);
    }

    public boolean isInMulti() {
        return isInMulti;
    }

    public void setDb(int db) {
        this.db = db;
    }

    public long getDb() {
        return db;
    }

    public void setDb(long db) {
        this.db = db;
    }

    public void setClientName(String clientName) {
        sendCommand(CLIENT, Keyword.SETNAME.raw, SafeEncoder.encode(clientName));
    }

    public void close() {
        db = 0;
        super.close();
    }

    public void quit() {
        db = 0;
        sendCommand(QUIT);
    }

    public void ping() {
        sendCommand(PING);
    }
}
