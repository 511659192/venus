package eurekademo.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * Created by ym on 2018/8/22.
 */
@Component
@Aspect
public class AspectDemo {

    @Pointcut(value = "execution(public * eurekademo.controller.CacheService.*(..))")
    public void pointCut(){};

    @Around(value = "pointCut()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("------------------- before");
        Object proceed = pjp.proceed();
        System.out.println("--------------------- after");
        return proceed;
    }
}
