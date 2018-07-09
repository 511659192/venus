package com.ym.materials.gson.internal.bind;

import com.ym.materials.gson.Gson;
import com.ym.materials.gson.adapter.TypeAdapter;
import com.ym.materials.gson.adapter.TypeAdapterFactory;
import com.ym.materials.gson.type.TypeToken;

/**
 * Created by ym on 2018/7/8.
 */
public class PrimitiveTypeAdapterFacroty {

    static <T> TypeAdapterFactory newFactory(final Class type, final TypeAdapter<T> typeAdapter) {
        return new TypeAdapterFactory() {
            @Override
            public <T> boolean accept(TypeToken<T> typeToken) {
                return typeToken.getRawType() == type;
            }

            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
                return (TypeAdapter<T>) typeAdapter;
            }
        };
    }
}
