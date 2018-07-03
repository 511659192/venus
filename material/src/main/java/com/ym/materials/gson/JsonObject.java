package com.ym.materials.gson;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by ym on 2018/7/2.
 */
public class JsonObject extends JsonElement {

    private final LinkedHashMap<String, JsonElement> members = new LinkedHashMap<>();


    public void add(String property, JsonElement value) {
        if (property == null) {
            value = JsonNull.INSTANCE;
        }
        members.put(property, value);
    }

    public Set<Map.Entry<String, JsonElement>> entrySet() {
        return members.entrySet();
    }
}
