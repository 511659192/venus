package com.ym.materials.gson;

/**
 * Created by ym on 2018/7/3.
 */
public class JsonIOException extends JsonParseException {
    public JsonIOException(String message) {
        super(message);
    }

    public JsonIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonIOException(Throwable cause) {
        super(cause);
    }
}
