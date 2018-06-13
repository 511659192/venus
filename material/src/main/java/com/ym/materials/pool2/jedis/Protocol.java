package com.ym.materials.pool2.jedis;

import com.ym.materials.pool2.jedis.exceptions.*;
import com.ym.materials.pool2.util.SafeEncoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by ym on 2018/6/11.
 */
public final class Protocol {

    private static final String ASK_RESPONSE = "ASK";
    private static final String MOVED_RESPONSE = "MOVED";
    private static final String CLUSTERDOWN_RESPONSE = "CLUSTERDOWN";
    private static final String BUSY_RESPONSE = "BUSY";
    private static final String NOSCRIPT_RESPONSE = "NOSCRIPT";

    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 6379;
    public static final int DEFAULT_TIMEOUT = 2000;
    public static final int DEFAULT_DATABASE = 0;

    public static final byte ASTERISK_BYTE = '*';
    public static final byte DOLLAR_BYTE = '$';
    public static final byte MINUS_BYTE = '-';
    public static final byte PLUS_BYTE = '+';
    public static final byte COLON_BYTE = ':';

    public static void sendCommand(RedisOutputStream os, Command cmd, byte[][] args) {
        sendCommand(os, cmd.raw, args);
    }

    private static void sendCommand(final RedisOutputStream os, final byte[] command, final byte[]... args) {
        try {
            os.write(ASTERISK_BYTE);
            os.writeIntCrLf(args.length + 1);
            os.write(DOLLAR_BYTE);
            os.writeIntCrLf(command.length);
            os.write(command);
            os.writeCrLf();

            for (byte[] arg : args) {
                os.write(DOLLAR_BYTE);
                os.writeIntCrLf(arg.length);
                os.write(arg);
                os.writeCrLf();
            }
        } catch (IOException e) {
            throw new JedisConnectionException(e);
        }
    }

    public static String readErrorLineIfPossible(RedisInputStream is) {
        byte b = is.readByte();
        if (b != MINUS_BYTE) {
            return null;
        }
        return is.readLine();
    }

    public static Object read(RedisInputStream inputStream) {
        return process(inputStream);
    }

    private static Object process(RedisInputStream is) {
        final byte b = is.readByte();
        if (b == PLUS_BYTE) {
            return processStatusCodeReply(is);
        } else if (b == DOLLAR_BYTE) {
            return processBulkReply(is);
        } else if (b == ASTERISK_BYTE) {
            return processMultiBulkReply(is);
        } else if (b == COLON_BYTE) {
            return processInteger(is);
        } else if (b == MINUS_BYTE) {
            processError(is);
            return null;
        } else {
            throw new JedisConnectionException("Unknown reply: " + (char) b);
        }
    }

    private static byte[] processStatusCodeReply(final RedisInputStream is) {
        return is.readLineBytes();
    }

    private static byte[] processBulkReply(final RedisInputStream is) {
        final int len = is.readIntCrLf();
        if (len == -1) {
            return null;
        }

        final byte[] read = new byte[len];
        int offset = 0;
        while (offset < len) {
            final int size = is.read(read, offset, (len - offset));
            if (size == -1) throw new JedisConnectionException("It seems like server has closed the connection.");
            offset += size;
        }

        // read 2 more bytes for the command delimiter
        is.readByte();
        is.readByte();
        return read;
    }

    private static List<Object> processMultiBulkReply(final RedisInputStream is) {
        final int num = is.readIntCrLf();
        if (num == -1) {
            return null;
        }
        final List<Object> ret = new ArrayList<Object>(num);
        for (int i = 0; i < num; i++) {
            try {
                ret.add(process(is));
            } catch (JedisDataException e) {
                ret.add(e);
            }
        }
        return ret;
    }

    private static Long processInteger(final RedisInputStream is) {
        return is.readLongCrLf();
    }

    private static void processError(final RedisInputStream is) {
        String message = is.readLine();
        // TODO: I'm not sure if this is the best way to do this.
        // Maybe Read only first 5 bytes instead?
        if (message.startsWith(MOVED_RESPONSE)) {
            String[] movedInfo = parseTargetHostAndSlot(message);
            throw new JedisMovedDataException(message, new HostAndPort(movedInfo[1], Integer.valueOf(movedInfo[2])), Integer.valueOf(movedInfo[0]));
        } else if (message.startsWith(ASK_RESPONSE)) {
            String[] askInfo = parseTargetHostAndSlot(message);
            throw new JedisAskDataException(message, new HostAndPort(askInfo[1], Integer.valueOf(askInfo[2])), Integer.valueOf(askInfo[0]));
        } else if (message.startsWith(CLUSTERDOWN_RESPONSE)) {
            throw new JedisClusterException(message);
        } else if (message.startsWith(BUSY_RESPONSE)) {
            throw new JedisBusyException(message);
        } else if (message.startsWith(NOSCRIPT_RESPONSE) ) {
            throw new JedisNoScriptException(message);
        }
        throw new JedisDataException(message);
    }

    private static String[] parseTargetHostAndSlot(String clusterRedirectResponse) {
        String[] response = new String[3];
        String[] messageInfo = clusterRedirectResponse.split(" ");
        String[] targetHostAndPort = HostAndPort.extractParts(messageInfo[2]);
        response[0] = messageInfo[1];
        response[1] = targetHostAndPort[0];
        response[2] = targetHostAndPort[1];
        return response;
    }


    public static enum Command {
        PING, SET, GET, AUTH, SELECT, CLIENT, QUIT;

        public final byte[] raw;

        Command() {
            raw = SafeEncoder.encode(this.name());
        }

    }

    public static enum Keyword {
        SETNAME, PING, PONG;
        public final byte[] raw;

        Keyword() {
            raw = SafeEncoder.encode(this.name().toLowerCase(Locale.ENGLISH));
        }
    }
}
