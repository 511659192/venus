package com.ym.materials.gson;

/**
 * Created by ym on 2018/7/2.
 */
public enum LongSerializationPolicy {

    DEFAULT() {
        @Override
        public JsonElement serialize(Long value) {
            return new JsonPrimitive(value) {
            };
        }
    };

    public abstract JsonElement serialize(Long value);
}
