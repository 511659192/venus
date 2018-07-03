package com.ym.materials.gson;

/**
 * Created by ym on 2018/7/2.
 */
public abstract class JsonElement {
    public boolean isJsonNull() {
        return this instanceof JsonNull;
    }

    public boolean isJsonPrimitive() {
        return this instanceof JsonPrimitive;
    }

    public JsonPrimitive getAsJsonPrimitive() {
        if (isJsonPrimitive()) {
            return (JsonPrimitive) this;
        }

        throw new IllegalStateException("not a json primitive " + this);
    }

    public boolean isJsonArray() {
        return this instanceof JsonArray;
    }

    public JsonArray getAsJsonArray() {
        if (isJsonArray()) {
            return (JsonArray) this;
        }
        throw new IllegalStateException("not a json array " + this);
    }

    public boolean isJsonObject() {
        return this instanceof JsonObject;
    }

    public JsonObject getAsJsonObject() {
        if (isJsonObject()) {
            return (JsonObject) this;
        }
        throw new IllegalStateException("not a json object " + this);
    }
}
