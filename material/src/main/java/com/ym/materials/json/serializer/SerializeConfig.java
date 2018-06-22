package com.ym.materials.json.serializer;

import com.ym.materials.json.JSON;
import com.ym.materials.json.serializer.codec.StringCodec;
import com.ym.materials.json.util.IdentityHashMap;

import java.lang.reflect.Type;

public class SerializeConfig {

    public final static SerializeConfig globalInstance  = new SerializeConfig();
    private final IdentityHashMap<Type, ObjectSerializer> serializers;
    private final boolean fieldBase;

    public SerializeConfig() {
        this(IdentityHashMap.DEFAULT_SIZE);
    }

    public SerializeConfig(int tableSize) {
        this(tableSize, false);
    }

    public SerializeConfig(int tableSize, boolean fieldBase) {
        this.fieldBase = fieldBase;
        serializers = new IdentityHashMap<Type, ObjectSerializer>(tableSize);
        initSerializers();
    }

    private void initSerializers() {
        put(String.class, StringCodec.instance);
    }

    public Object put(Type type, ObjectSerializer serializer) {
        return this.serializers.put(type, serializer);
    }

    public ObjectSerializer getObjectWriter(Class<?> clazz) {
        return getObjectWriter(clazz, true);
    }

    private ObjectSerializer getObjectWriter(Class<?> clazz, boolean create) {
        ObjectSerializer writer = serializers.get(clazz);
        if (writer != null) {
            return writer;
        }
        if (create) {
            writer = createJavaBeanSerializer(clazz);
            put(clazz, writer);
        }
        return writer;
    }

    private ObjectSerializer createJavaBeanSerializer(Class<?> clazz) {

        return null;
    }

}
