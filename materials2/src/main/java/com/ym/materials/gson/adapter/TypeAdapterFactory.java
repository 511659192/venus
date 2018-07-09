package com.ym.materials.gson.adapter;

import com.ym.materials.gson.Gson;
import com.ym.materials.gson.type.TypeToken;

/**
 * Created by ym on 2018/7/8.
 */
public interface TypeAdapterFactory {

    <T> boolean accept(TypeToken<T> typeToken);

    <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken);
}
