package com.ym.materials.proxy;

import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Date;

public class TestInvoker {

    private static long l     = 333333L;
    private static int  times = 100000000;

    public interface BinaryInvoker<T, A> {

        void invoke(T data, A arg);
    }

    public interface UnitaryInvoker<T> {

        void invoke(T data);
    }

    public interface ReturnedUnitaryInvoker<T, R> {

        R invoke(T data);
    }

    static UnitaryInvoker<Date> dateGetter = new UnitaryInvoker<Date>() {
        @Override
        public void invoke(Date data) {
            data.getTime();
        }

    };

    static ReturnedUnitaryInvoker<Date, Long> returnedDateGetter = new ReturnedUnitaryInvoker<Date, Long>() {
        @Override
        public Long invoke(Date data) {
            return data.getTime(); // 一次封箱
        }

    };

    static BinaryInvoker<Date, Long> dateSetter  = new BinaryInvoker<Date, Long>() {
        @Override
        public void invoke(Date data, Long arg) {
            data.setTime(arg);
        }
    };

    static long date_get(Date date) {
        return date.getTime();
    }

    static void date_set(Date date, long time) {
        date.setTime(time);
    }

    @Test
    public void testMethod() {
        Date date = new Date(l);
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < times; i++) {
            date_get(date);
            date_set(date, 333333333L);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("方法调用耗时：" + (t1 - t0) + "ms");
    }

    @Test
    public void testInterface() {
        Date date = new Date(l);
        Long time = new Long(333333333L);
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < times; i++) {
            dateGetter.invoke(date);
            //returnedDateGetter.invoke(date); //1328ms 因为封箱，所以慢
            dateSetter.invoke(date, time); // 22ms
            // dateSetter.invoke(date, 333333333L); //1206ms 因为封箱，所以慢
        }
        long t1 = System.currentTimeMillis();
        System.out.println("接口调用耗时：" + (t1 - t0) + "ms");
    }

    @Test
    public void testInterface2() {
        Date date = new Date(l);
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < times; i++) {
            returnedDateGetter.invoke(date); //1328ms 因为封箱，所以慢
            dateSetter.invoke(date, 333333333L); //1206ms 因为封箱，所以慢
        }
        long t1 = System.currentTimeMillis();
        System.out.println("接口调用耗时：" + (t1 - t0) + "ms");
    }

    @Test
    public void test() {
        Date date = new Date(l);
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < times; i++) {
            date.getTime();
            date.setTime(333333333L);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("直接调用耗时：" + (t1 - t0) + "ms");
    }

    @Test
    public void testInvoker() throws Exception {

        Date date = new Date();
        Method getMethod = Date.class.getMethod("getTime");
        getMethod.setAccessible(true);
        Method setMethod = Date.class.getMethod("setTime", Long.TYPE);
        setMethod.setAccessible(true);
        Invokers.Invoker get = Invokers.newInvoker(getMethod);
        Invokers.Invoker set = Invokers.newInvoker(setMethod);
        Object[] argGet = new Object[] {};
        Object[] argSet = new Object[] { 333333L };
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < times; i++) {
            // get.invoke(date, new Object[] {});
            // set.invoke(date, new Object[] { 333333L });
            get.invoke(date, argGet);
            set.invoke(date, argSet);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("Invoker调用耗时：" + (t1 - t0) + "ms");
    }

    @Test
    public void testJDK() throws Exception {

        Date date = new Date();
        Method getMethod = Date.class.getMethod("getTime");
        getMethod.setAccessible(true);
        Method setMethod = Date.class.getMethod("setTime", Long.TYPE);
        setMethod.setAccessible(true);
        Object[] argGet = new Object[] {};
        Object[] argSet = new Object[] { 333333L };
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < times; i++) {
            // getMethod.invoke(date, new Object[] {});
            // setMethod.invoke(date, new Object[] { 333333L });
            getMethod.invoke(date, argGet);
            setMethod.invoke(date, argSet);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("JDK反射调用耗时：" + (t1 - t0) + "ms");
    }
}
