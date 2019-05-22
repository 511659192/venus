// Copyright (C) 2019 Meituan
// All rights reserved
package com.ym.materials.jvm;

/**
 * @author yangmeng
 * @version 1.0
 * @created 2019-03-07 21:09
 **/
public class User {

    private String name;
    private String sex;
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public User(String name, String sex, int age) {
        this.name = name;
        this.sex = sex;
        this.age = age;
    }
}