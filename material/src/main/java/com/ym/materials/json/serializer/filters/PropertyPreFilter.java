package com.ym.materials.json.serializer.filters;

import com.ym.materials.json.serializer.JSONSerializer;
import com.ym.materials.json.serializer.SerializeFilter;

public interface PropertyPreFilter extends SerializeFilter {

    boolean apply(JSONSerializer serializer, Object object, String name);
}
