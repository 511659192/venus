import org.springframework.aop.support.AopUtils;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by ym on 2018/8/23.
 */
public class UserMethod {

    interface SuperClass<T> {
        void method(T t);

        void method2(T t);
    }

    static abstract class SubClass1 implements SuperClass<String> {

        @Override
        public void method(String s) {
            System.out.println(s);
        }
    }

    static class SubClass2 extends SubClass1 {
        @Override
        public void method2(String s) {
            System.out.println("method2");
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> clazz = SubClass1.class;
        Method method = SuperClass.class.getMethod("method", Object.class);
        Method mostSpecificMethod = ClassUtils.getMostSpecificMethod(method, clazz);
        Method mostSpecificMethod1 = AopUtils.getMostSpecificMethod(method, clazz);
        System.out.println("111");
    }
}
