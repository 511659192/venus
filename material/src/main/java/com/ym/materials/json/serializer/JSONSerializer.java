package com.ym.materials.json.serializer;

public class JSONSerializer extends SerializeFilterable {
    private final SerializeWriter out;
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
}
