package com.ym.materials.json.serializer.filters;

import com.ym.materials.json.serializer.JSONSerializer;
import com.ym.materials.json.serializer.SerializeFilter;

public abstract class BeforeFilter implements SerializeFilter {

    private final static ThreadLocal<JSONSerializer> serializerLocal = new ThreadLocal<>();
    private final static ThreadLocal<Character> seperatorLocal = new ThreadLocal<>();
    private final static Character COMMA = Character.valueOf(',');

    final char writeBefore(JSONSerializer serializer, Object object, char seperator) {
        serializerLocal.set(serializer);
        seperatorLocal.set(seperator);
        writeBefore(object);
        serializerLocal.set(null);
        return seperatorLocal.get();
    }

    protected final void writeKeyValue(String key, Object value) {
        JSONSerializer serializer = serializerLocal.get();
        Character seperator = seperatorLocal.get();
        serializer.writeKeyValue(seperator, key, value);
        if (seperator != ',') {
            seperatorLocal.set(COMMA);
        }
    }

    protected abstract void writeBefore(Object object);
}
