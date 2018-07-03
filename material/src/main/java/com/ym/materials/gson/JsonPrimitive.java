package com.ym.materials.gson;

import com.ym.materials.gson.internal.LazilyParsedNumber;
import org.apache.commons.collections4.SetValuedMap;
import org.junit.Assert;

/**
 * Created by ym on 2018/7/2.
 */
public class JsonPrimitive extends JsonElement {

    private static final Class<?>[] PRIMITIVE_TYPES = {int.class, long.class, short.class,
            float.class, double.class, byte.class, boolean.class, char.class, Integer.class, Long.class,
            Short.class, Float.class, Double.class, Byte.class, Boolean.class, Character.class};

    private Object value;

    public JsonPrimitive(Number number) {
        setValue(number);
    }

    public JsonPrimitive(String string) {
        setValue(string);
    }

    public JsonPrimitive(Boolean bool) {
        setValue(bool);
    }

    public void setValue(Object primitive) {
        if (primitive instanceof Character) {
            char c = ((Character) primitive).charValue();
            this.value = String.valueOf(c);
        } else {
            Assert.assertTrue(primitive instanceof Number || isPrimitiveOrString(primitive));
            this.value = primitive;
        }
    }

    private static boolean isPrimitiveOrString(Object target) {
        if (target instanceof String) {
            return true;
        }
        Class<?> classOfPrimitive = target.getClass();

        for (Class<?> standardPrimitive : PRIMITIVE_TYPES) {
            if (standardPrimitive.isAssignableFrom(classOfPrimitive)) {
                return true;
            }
        }
        return false;
    }

    public boolean isNumber() {
        return value instanceof Number;
    }

    public Number getAsNumber() {
        return value instanceof String ? new LazilyParsedNumber((String) value) : (Number) value;
    }

    public boolean isBoolean() {
        return value instanceof Boolean;
    }

    Boolean getAsBooleanWrapper() {
        return (Boolean) value;
    }

    public boolean getAsBoolean() {
        if (isBoolean()) {
            return (boolean) this.value;
        }
        return Boolean.parseBoolean(getAsString());
    }

    public String getAsString() {
        if (isNumber()) {
            return getAsNumber().toString();
        }

        if (isBoolean()) {
            return getAsBooleanWrapper().toString();
        }
        return (String) value;
    }
}
