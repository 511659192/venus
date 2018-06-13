package com.ym.materials.pool2.jedis;

import com.ym.materials.pool2.jedis.Protocol.Command;
import com.ym.materials.pool2.jedis.exceptions.JedisConnectionException;
import com.ym.materials.pool2.util.IOUtils;
import com.ym.materials.pool2.util.SafeEncoder;
import org.apache.commons.lang3.StringUtils;

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

    private static final byte[][] EMPTY_ARGS = new byte[0][];

    private String host;
    private int port;
    private boolean ssl;
    private SSLSocketFactory sslSocketFactory;
    private SSLParameters sslParameters;
    private HostnameVerifier hostnameVerifier;
    private int connectTimeout;
    private int soTimeout;

    private int pipelinedCommands = 0;

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

    protected Connection sendCommand(final Command cmd) {
        return sendCommand(cmd, EMPTY_ARGS);
    }

    protected Connection sendCommand(final Command cmd, final byte[]... args) {
        try {
            connect();
            Protocol.sendCommand(outputStream, cmd, args);
            pipelinedCommands++;
            return this;
        } catch (JedisConnectionException e) {
            try {
                String errMsg = Protocol.readErrorLineIfPossible(inputStream);
                if (StringUtils.isNotBlank(errMsg)) {
                    throw new JedisConnectionException(errMsg, e.getCause());
                }
            } catch (Exception igore) {

            }
            broken = true;
            throw e;
        }
    }

    public String getStatusCodeReply() {
        flush();
        pipelinedCommands--;
        final byte[] resp = (byte[]) readProtocolWithCheckingBroken();
        if (resp == null) {
            return null;
        }
        return SafeEncoder.encode(resp);
    }

    private Object readProtocolWithCheckingBroken() {
        try {
            return Protocol.read(inputStream);
        } catch (JedisConnectionException e) {
            broken = true;
            throw e;
        }
    }

    private void flush() {
        try {
            outputStream.flush();
        } catch (IOException e) {
            broken = true;
            throw new JedisConnectionException(e);
        }
    }


    public boolean isBroken() {
        return broken;
    }

    public void close() {
        disConnect();
    }

    public void disConnect() {
        if (!isConnected()) {
            return;
        }
        try {
            outputStream.flush();
            socket.close();
        } catch (IOException e) {
            broken = true;
            throw new JedisConnectionException(e);
        } finally {
            IOUtils.closeQuietly(socket);
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
