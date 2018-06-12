package com.ym.materials.pool2.jedis;

import com.ym.materials.pool2.jedis.Protocol.Command;
import com.ym.materials.pool2.jedis.exceptions.JedisConnectionException;
import com.ym.materials.pool2.util.SafeEncoder;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by ym on 2018/6/12.
 */
public class Connection {
    private String host;
    private int port;
    private boolean ssl;
    private SSLSocketFactory sslSocketFactory;
    private SSLParameters sslParameters;
    private HostnameVerifier hostnameVerifier;
    private int connectTimeout;
    private int soTimeout;

    private Socket socket;
    private RedisOutputStream outputStream;
    private RedisInputStream inputStream;
    private boolean broken = false;

    public Connection(String host, int port, boolean ssl, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier) {
        this.host = host;
        this.port = port;
        this.ssl = ssl;
        this.sslSocketFactory = sslSocketFactory;
        this.sslParameters = sslParameters;
        this.hostnameVerifier = hostnameVerifier;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    public boolean isConnected() {
        return socket != null && socket.isBound() && !socket.isClosed() && socket.isConnected() && !socket.isInputShutdown() && !socket.isOutputShutdown();
    }

    public void connect() {
        if (isConnected()) {
            return;
        }
        try {
            socket = new Socket();
            socket.setReuseAddress(true);
            socket.setKeepAlive(true);
            socket.setTcpNoDelay(true);
            socket.setSoLinger(true, 0);
            socket.connect(new InetSocketAddress(host, port));
            socket.setSoTimeout(soTimeout);

            if (ssl) {
                if (null == sslSocketFactory) {
                    sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                }

                socket = (SSLSocket) sslSocketFactory.createSocket(socket, host, port, true);
                if (null != sslParameters) {
                    ((SSLSocket) socket).setSSLParameters(sslParameters);
                }
                if (null != hostnameVerifier && (!hostnameVerifier.verify(host, ((SSLSocket) socket).getSession()))) {
                    throw new JedisConnectionException(String.format("connection to %s failed ssl/tls hostname verification", host));
                }
            }

            outputStream = new RedisOutputStream(socket.getOutputStream());
            inputStream = new RedisInputStream(socket.getInputStream());
        } catch (IOException e) {
            broken = true;
            throw new JedisConnectionException(e);
        }
    }

    protected Connection sendCommand(Command auth, String... args) {
        byte[][] bargs = new byte[args.length][];
        for (int i = 0; i < args.length; i++) {
            bargs[i] = SafeEncoder.encode(args[i]);
        }
        return sendCommand(auth, bargs);
    }

    protected Connection sendCommand(final Command cmd, final byte[]... args) {
        try {
            connect();
            Protocol.sendCommand(outputStream, cmd, args);
        } catch (JedisConnectionException e) {

        }
        return null;
    }
}
