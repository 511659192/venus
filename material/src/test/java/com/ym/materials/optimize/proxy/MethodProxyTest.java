package com.ym.materials.optimize.proxy;

import com.google.common.base.Stopwatch;
import net.sf.cglib.core.DebuggingClassWriter;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public class MethodProxyTest {

    @Test
    public void invoke() {
//        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, Thread.currentThread().getContextClassLoader().getResource("").getPath());
        SimpleEntity entity = new SimpleEntity();
        MethodProxy methodProxy = MethodProxy.create(SimpleEntity.class, "(Ljava/lang/String;)V", "setName");
        Object[] objects = {"setName"};
        methodProxy.invoke(entity, objects);
        Stopwatch stopwatch = Stopwatch.createStarted();
        for (int j = 0; j < 10; j++) {
            for (int i = 0; i < 1000; i++) {
                methodProxy.invoke(entity, objects);
            }
        }
        System.out.println(entity.getName());
        stopwatch.stop();
        System.out.println(stopwatch.elapsed(TimeUnit.MICROSECONDS) / 10);

    }

    @Test
    public void invoke2() throws Exception {
        SimpleEntity entity = new SimpleEntity();
        Object[] objects = {"setName"};
        Method method = SimpleEntity.class.getDeclaredMethod("setName", String.class);
        Stopwatch stopwatch = Stopwatch.createStarted();
        for (int j = 0; j < 10; j++) {
            for (int i = 0; i < 100; i++) {
                method.invoke(entity, objects);
            }
        }
        System.out.println(entity.getName());
        stopwatch.stop();
        System.out.println(stopwatch.elapsed(TimeUnit.MICROSECONDS) / 10);

    }


    @Test
    public void invoke3() throws Exception {
        SimpleEntity entity = new SimpleEntity();
        entity.setName("name");
        Stopwatch stopwatch = Stopwatch.createStarted();
        for (int j = 0; j < 10; j++) {
            for (int i = 0; i < 100; i++) {
                entity.getName();
            }
        }
        stopwatch.stop();
        System.out.println(stopwatch.elapsed(TimeUnit.MICROSECONDS) / 10);

    }

}