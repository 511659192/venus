package com.ym.materials.gson.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by ym on 2018/7/4.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface SerializedName {

    /**
     * @return the desired name of the field when it is serialized or deserialized
     */
    String value();
    /**
     * @return the alternative names of the field when it is deserialized
     */
    String[] alternate() default {};
}
