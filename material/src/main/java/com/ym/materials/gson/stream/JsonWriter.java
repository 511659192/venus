package com.ym.materials.gson.stream;

import org.junit.Assert;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;

/**
 * Created by ym on 2018/7/2.
 */
public class JsonWriter implements Closeable, Flushable {
    private Writer out;

    public JsonWriter(Writer out) {
        Assert.assertNotNull(out);
        this.out = out;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public void flush() throws IOException {

    }

    public void nullValue() {

    }

    public void value(Number number) {

    }

    public void value(boolean bool) {

    }

    public void value(String string) {

    }

    public void beginArray() {

    }

    public void endArray() {

    }

    public void beginObject() {

    }

    public void name(String name) {

    }

    public void endObject() {

    }
}
