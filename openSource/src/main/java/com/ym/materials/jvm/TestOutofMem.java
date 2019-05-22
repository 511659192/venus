// Copyright (C) 2019 Meituan
// All rights reserved
package com.ym.materials.jvm;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yangmeng
 * @version 1.0
 * @created 2019-03-07 21:10
 **/
public class TestOutofMem {
    public static void main(String[] args) {
        List<User> persons = new ArrayList<User>();
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            persons.add(new User("liuhai", "male", 25));
        }
        System.out.println(persons);
    }
}