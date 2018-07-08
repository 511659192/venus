package com.ym.materials.gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ym on 2018/7/7.
 */
public class GsonDemo {

    static class Person {
        private String name;
//        private Person child;
//        private String name2;
        private List<Integer> ids;

        public List<Integer> getIds() {
            return ids;
        }

        public void setIds(List<Integer> ids) {
            this.ids = ids;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static void main(String[] args) {
//        String text = "{\"ids\":[1,2]}";
//        Gson com.ym.materials.gson = new Gson();
//        Person s = com.ym.materials.gson.fromJson(text, Person.class);
//        System.out.println(s == null ? "null" : s);

        Gson gson = new Gson();
        Person person = new Person();
//        List<Integer> list = new ArrayList<>();
//        list.add(100);
//        list.add(200);
//        person.setIds(list);
        person.setName("name");
        System.out.println(gson.toJson(person));
    }
}
