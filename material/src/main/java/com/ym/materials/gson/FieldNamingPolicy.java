package com.ym.materials.gson;

import java.lang.reflect.Field;

/**
 * Created by ym on 2018/7/2.
 */
public enum FieldNamingPolicy implements FieldNamingStrategy {

    INENTITY() {
        @Override
        public String translateNmae(Field f) {
            return f.getName();
        }
    }
    ;
}
