package com.ym.materials.gson;

/**
 * Created by ym on 2018/7/3.
 */
public class JsonSyntaxException extends JsonParseException {
    public JsonSyntaxException(String message) {
        super(message);
    }

    public JsonSyntaxException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonSyntaxException(Throwable cause) {
        super(cause);
    }
}
