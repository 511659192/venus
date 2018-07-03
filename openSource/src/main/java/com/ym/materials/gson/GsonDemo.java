package com.ym.materials.gson;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

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

        public User() {
            System.out.println(this);
            System.out.println(this.getClass());
            System.out.println(this.getClass().getGenericSuperclass());
        }

//        @Override
//        public String toString() {
//            return name+"->"+age+":"+room;
//        }
    }



    public static void main(String[] args) {

        new User<ClassRoom>(){};
        new User<ClassRoom>();

        Gson gson = new Gson();
        String strJson = "{name:'david',age:19,room:{roomName:'small',number:1}}";
        User<ClassRoom> u = gson.fromJson(strJson, new TypeToken<User<ClassRoom>>(){}.getType()); // david->19:[small:1]
//        User<ClassRoom> u = gson.fromJson(strJson, User.class); // david->19:{roomName=small, number=1.0}
        System.out.println(gson.toJson(u));
    }
}
