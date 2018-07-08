package com.ym.materials.gson;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import sun.misc.Unsafe;

import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class GsonDemo {
    private final static Unsafe UNSAFE;
    private final static long bufferOffset;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            UNSAFE = (Unsafe) field.get(null);
            bufferOffset = UNSAFE.objectFieldOffset(JsonReader.class.getDeclaredField("buffer")); // 原始数组元素地址偏移量
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public static void main(String[] args) {
        Gson gson = new Gson();
        String strJson = "test1111test2222test3333";
        JsonReader jsonReader = gson.newJsonReader(new StringReader(strJson));
        UNSAFE.putObject(jsonReader, bufferOffset, new char[8]);
        String s = gson.fromJson(jsonReader, String.class);
        System.out.println(s);
    }
}
