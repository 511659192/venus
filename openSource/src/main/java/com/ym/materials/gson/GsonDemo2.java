package com.ym.materials.gson;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import sun.misc.Unsafe;

import java.io.StringReader;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class GsonDemo2 {
    static class Person {
        private String name;
        private Person child;
        private List<Integer> ids;
        private BigDecimal num;

        public BigDecimal getNum() {
            return num;
        }

        public void setNum(BigDecimal num) {
            this.num = num;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Person getChild() {
            return child;
        }

        public void setChild(Person child) {
            this.child = child;
        }

        public List<Integer> getIds() {
            return ids;
        }

        public void setIds(List<Integer> ids) {
            this.ids = ids;
        }
    }

    public static void main(String[] args) {
        Gson gson = new Gson();
        Person person = new Person();
        person.setName("01");
        System.out.println(Long.toBinaryString(Long.MIN_VALUE));
        System.out.println(Long.toBinaryString(Long.MIN_VALUE + 1));
        person.setNum(new BigDecimal(-Long.MIN_VALUE));
        System.out.println(Integer.toBinaryString(15));
        System.out.println(Integer.toBinaryString(12));
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
//        person.setIds(list);
        String aa = gson.toJson(person);
        System.out.println(aa);
        gson.fromJson(aa, Person.class);
    }
}
