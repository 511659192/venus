package com.ym.materials;

public class Test {

    private String name;

    public void test1() {
        System.out.println("test1");
        test2();
    }

    protected void test2() {
        System.out.println("test2");
    }

    public static void main(String[] args) {
        new Test().new Inner().test0();
    }

    private class Inner {
        public void test0() {
            test1();
        }

        protected void test2() {
            System.out.println("test2");
        }
    }

}
