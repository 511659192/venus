package com.ym.materials.thrift.annotaion;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;

/**
 * Created by ym on 2019/1/6.
 */
@ThriftService("helloService")
public interface HelloService {

    @ThriftMethod
    String sayHello(@ThriftField(name = "user") User user);
}
