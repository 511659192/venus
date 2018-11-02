package com.ym.materials.serializer;

import org.msgpack.annotation.Message;

import java.io.Serializable;

/**
 * Created by ym on 2018/10/29.
 */
@Message
public class Person implements Serializable{

    private String name;
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Person() {
    }

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
