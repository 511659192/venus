package com.ym.materials.gson.stream;

import org.junit.Assert;

import java.io.Reader;

/**
 * Created by ym on 2018/7/2.
 */
public class JsonReader {
    private Reader in;

    public JsonReader(Reader in) {
        Assert.assertNotNull(in);
        this.in = in;
    }

    public void skipValue() {

    }

    public JsonToken peek() {
        return null;
    }

    public String nextString() {
        return null;
    }

    public boolean nextBoolean() {
        return false;
    }

    public void nextNull() {

    }

    public void beginArray() {

    }

    public boolean hasNext() {
        return false;
    }

    public void endArray() {

    }

    public void beginObject() {

    }

    public String nextName() {
        return null;
    }

    public void endObject() {

    }

    public double nextDouble() {
        return 0;
    }

    public int nextInt() {
        return 0;
    }

    public long nextLong() {
        return 0;
    }
}
