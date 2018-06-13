package com.ym.materials.pool2.util;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by ym on 2018/6/13.
 */
public class IOUtils {

    private IOUtils() {
    }

    public static void closeQuietly(Socket socket) {
        if (socket == null) {
            return;
        }
        try {
            socket.close();
        } catch (IOException ignore) {

        }
    }
}
