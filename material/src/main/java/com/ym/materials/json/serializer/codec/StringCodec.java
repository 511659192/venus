package com.ym.materials.json.serializer.codec;

import com.ym.materials.json.serializer.JSONSerializer;
import com.ym.materials.json.serializer.ObjectSerializer;
import com.ym.materials.json.serializer.SerializeWriter;
import com.ym.materials.json.serializer.SerializerFeature;

import java.io.IOException;
import java.lang.reflect.Type;

public class StringCodec implements ObjectSerializer {

    public final static StringCodec instance = new StringCodec();

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        write(serializer, (String) object);
    }

    private void write(JSONSerializer serializer, String value) {
        SerializeWriter out = serializer.out;
        if (value == null) {
            out.writeNull(SerializerFeature.WriteNullStringAsEmpty);
            return;
        }

        out.writeString(value);
    }
}
