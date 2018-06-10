package com.ym.materials.proxy;

import net.sf.cglib.core.AbstractClassGenerator;
import net.sf.cglib.core.GeneratorStrategy;
import net.sf.cglib.core.NamingPolicy;
import net.sf.cglib.core.Signature;
import net.sf.cglib.reflect.FastClass;


public class MethodProxy {

    private Signature methodSignature;
    private CreateInfo createInfo;
    private final Object initLock = new Object();
    private volatile FastClassInfo fastClassInfo;

    public static MethodProxy create(Class<?> clazz, String desc, String name) {
        MethodProxy methodProxy = new MethodProxy();
        methodProxy.methodSignature = new Signature(name, desc);
        methodProxy.createInfo = new CreateInfo(clazz);
        return methodProxy;
    }

    public Object invoke(Object target, Object[] args) {
        try {
            init();
            FastClassInfo fastClassInfo = this.fastClassInfo;
            return fastClassInfo.fastClass.invoke(fastClassInfo.index, target, args);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void init() {
        if (fastClassInfo != null) {
            return;
        }

        synchronized (initLock) {
            if (fastClassInfo != null) {
                return;
            }

            CreateInfo createInfo = this.createInfo;
            FastClassInfo fastClassInfo = new FastClassInfo();
            fastClassInfo.fastClass = helper(createInfo, createInfo.clazz);
            fastClassInfo.index = fastClassInfo.fastClass.getIndex(methodSignature);
            this.fastClassInfo = fastClassInfo;
            createInfo = null;
        }
    }

    private static FastClass helper(CreateInfo ci, Class type) {
        FastClass.Generator g = new FastClass.Generator();
        g.setType(type);
        g.setClassLoader(ci.clazz.getClassLoader());
        g.setNamingPolicy(ci.namingPolicy);
        g.setStrategy(ci.generatorStrategy);
        g.setAttemptLoad(ci.attemptLoad);
        return g.create();
    }

    private static class FastClassInfo
    {
        FastClass fastClass;
        int index;
    }


    private static class CreateInfo {
        Class<?> clazz;
        NamingPolicy namingPolicy;
        GeneratorStrategy generatorStrategy;
        boolean attemptLoad;

        public CreateInfo(Class<?> clazz) {
            this.clazz = clazz;
            AbstractClassGenerator fromEnhancer = AbstractClassGenerator.getCurrent();
            if (fromEnhancer != null) {
                namingPolicy = fromEnhancer.getNamingPolicy();
                generatorStrategy = fromEnhancer.getStrategy();
                attemptLoad = fromEnhancer.getAttemptLoad();
            }
        }
    }
}
