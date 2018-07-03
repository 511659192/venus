package com.ym.materials.gson;

import com.ym.materials.gson.reflect.TypeToken;

/**
 * Created by ym on 2018/7/2.
 */
public interface TypeAdapterFactory {
    <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken);
}
