package com.ym.materials.jdk;

import org.junit.Test;

/**
 * Created by ym on 2018/6/30.
 */
public class ConstructorDiff {

    public static class ClassRoom{
    }

    public static class User<T>{
        private T room;

        public User() {
            System.out.println(this.getClass());
            System.out.println(this.getClass().getGenericSuperclass());
        }
    }

    public static class $1 extends User<ClassRoom> {
    }

    @Test
    public void testSimpleConstructor() throws Exception {
        new User<ClassRoom>();
    }

    @Test
    public void testConstructorWithBrace() throws Exception {
        new User<ClassRoom>(){};
    }

    @Test
    public void test$1() throws Exception {
        new $1();
    }
}
