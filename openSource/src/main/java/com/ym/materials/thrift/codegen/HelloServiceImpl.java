package com.ym.materials.thrift.codegen;

import com.ym.materials.thrift.codegen.gened.HelloService;
import com.ym.materials.thrift.codegen.gened.User;

/**
 * Created by ym on 2019/1/1.
 */
public class HelloServiceImpl implements HelloService.Iface {
    @Override
    public String sayHello(User user) throws org.apache.thrift.TException {
        return "hello " + user.getName() + " " + user.getEmail();
    }
}
