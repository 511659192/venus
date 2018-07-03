package com.ym.materials.gson.internal;

import java.math.BigDecimal;

/**
 * Created by ym on 2018/7/2.
 */
public class LazilyParsedNumber extends Number {

    private final String value;

    public LazilyParsedNumber(String value) {
        this.value = value;
    }

    @Override
    public int intValue() {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            try {
                return (int) Long.parseLong(value);
            } catch (NumberFormatException e1) {
                return new BigDecimal(value).intValue();
            }
        }
    }

    @Override
    public long longValue() {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return new BigDecimal(value).longValue();
        }
    }

    @Override
    public float floatValue() {
        return Float.parseFloat(value);
    }

    @Override
    public double doubleValue() {
        return Double.parseDouble(value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof LazilyParsedNumber) {
            LazilyParsedNumber other = (LazilyParsedNumber) obj;
            return value == other.value || value.equals(other.value);
        }
        return false;
    }
}
