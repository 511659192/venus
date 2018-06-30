package com.ym.materials.gson;

import com.google.gson.Gson;

public class GsonDemo {
    public static class ClassRoom{
        public String roomName;
        public long number;
        public String toString() {
            return "["+roomName+":"+number+"]";
        }
    }

    public static class User<T>{
        private T room;
        public String name;
        public int age;

        @Override
        public String toString() {
            return name+"->"+age+":"+room;
        }
    }

    public static void main(String[] args) {
        Gson gson = new Gson();
        String strJson = "{name:'david',age:19,room:{roomName:'small',number:1}}";
        User u = gson.fromJson(strJson, User.class);
        System.out.println(u);
    }
}
