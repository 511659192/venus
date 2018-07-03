package com.ym.materials.gson;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ym on 2018/7/2.
 */
public class JsonArray extends JsonElement implements Iterable<JsonElement> {

    private final List<JsonElement> elements;

    public JsonArray() {
        this.elements = new ArrayList<>();
    }

    public JsonArray(int capacity) {
        this.elements = new ArrayList<>(capacity);
    }

    @Override
    public Iterator<JsonElement> iterator() {
        return null;
    }

    public void add(JsonElement element) {
        if (element == null) {
            element = JsonNull.INSTANCE;
        }
        elements.add(element);
    }
}
