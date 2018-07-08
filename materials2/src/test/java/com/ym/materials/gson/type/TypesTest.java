package com.ym.materials.gson.type;

import com.google.gson.internal.$Gson$Types;
import com.google.gson.reflect.*;
import com.google.gson.reflect.TypeToken;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by ym on 2018/7/8.
 */
public class TypesTest<E> {


    private List<Set<E>> list;

    @Test
    public void resolveTypeVariable() throws Exception {
        Field declaredField = TypesTest.class.getDeclaredField("list");
        TypeToken<TypesTest<String>> typeToken = new TypeToken<TypesTest<String>>() {
        };
        Type resolve = $Gson$Types.resolve(typeToken.getType(), typeToken.getRawType(), declaredField.getGenericType());
        TypeToken typeToken1 = TypeToken.get(resolve);
        $Gson$Types.getCollectionElementType(typeToken1.getType(), typeToken1.getRawType());
    }


}