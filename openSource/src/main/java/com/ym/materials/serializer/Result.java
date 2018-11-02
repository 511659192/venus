package com.ym.materials.serializer;

import org.msgpack.annotation.Message;

import java.io.Serializable;

@Message
public class Result<T> implements Serializable {

    private boolean success;

    private T model;

    public Result() {
    }

    public Result(boolean success, T model) {
        this.success = success;
        this.model = model;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getModel() {
        return model;
    }

    public void setModel(T model) {
        this.model = model;
    }
}
