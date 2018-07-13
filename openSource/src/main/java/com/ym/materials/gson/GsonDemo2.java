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

    public static void main(String[] args) {
        Person person = new Person();
        person.setName("person");
        Info info = new Info();
        info.setAddress("address");
//        person.setInfo(info);
        Gson gson = new Gson();
        String s = gson.toJson(person);
        System.out.println(s);
    }

    static class Person {
        String name;
        Info info;

        public Info getInfo() {
            return info;
        }

        public void setInfo(Info info) {
            this.info = info;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    ", info=" + info +
                    '}';
        }
    }

    static class Info {
        String address;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        @Override
        public String toString() {
            return "Info{" +
                    "address='" + address + '\'' +
                    '}';
        }
    }
}
