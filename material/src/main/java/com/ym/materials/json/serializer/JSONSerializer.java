package com.ym.materials.json.serializer;

import com.alibaba.fastjson.JSONException;

import java.io.IOException;

public class JSONSerializer extends SerializeFilterable {
    public final SerializeWriter out;
    private final SerializeConfig config;
    private String dateFormat;
    public JSONSerializer(SerializeWriter out, SerializeConfig config) {
        this.out = out;
        this.config = config;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public void config(SerializerFeature feature, boolean state) {
        out.config(feature, state);
    }

    public void writeKeyValue(Character seperator, String key, Object value) {
        if (seperator != '\0') {
            out.write(seperator);
        }

        out.writeFieldName(key);
        write(value);
    }

    public final void write(Object object) {
        if (object == null) {
            out.writeNull();
            return;
        }

        Class<?> clazz = object.getClass();
        ObjectSerializer writer = getObjectWriter(clazz);

        try {
            writer.write(this, object, null, null, 0);
        } catch (IOException e) {
            throw new JSONException(e.getMessage(), e);
        }
    }

    private ObjectSerializer getObjectWriter(Class<?> clazz) {
        return config.getObjectWriter(clazz);
    }
}
