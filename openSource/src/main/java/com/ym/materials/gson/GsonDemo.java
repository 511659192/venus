package com.ym.materials.gson;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
        User<ClassRoom> u = gson.fromJson(strJson, new TypeToken<User<ClassRoom>>(){}.getType()); // david->19:[small:1]
//        User<ClassRoom> u = gson.fromJson(strJson, User.class); // david->19:{roomName=small, number=1.0}
        System.out.println(u);
        System.out.println(gson.toJson(u));
    }
}
