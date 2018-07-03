package com.ym.materials.gson;

import java.lang.reflect.Field;

/**
 * Created by ym on 2018/7/2.
 */
public interface FieldNamingStrategy {
    String translateNmae(Field f);
}
