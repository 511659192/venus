package com.ym.materials.gson.internal.bind;

import com.ym.materials.gson.Gson;
import com.ym.materials.gson.TypeAdapter;
import com.ym.materials.gson.TypeAdapterFactory;
import com.ym.materials.gson.reflect.TypeToken;

/**
 * Created by ym on 2018/7/2.
 */
public class JsonAdapterAnnotationTypeAdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> tTypeToken) {
        return null;
    }
}
