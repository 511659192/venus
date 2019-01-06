package com.ym.materials.thrift.annotaion;

import com.facebook.nifty.client.FramedClientConnector;
import com.facebook.swift.service.ThriftClientManager;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;

/**
 * Created by ym on 2019/1/6.
 */
public class ClientMain {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ThriftClientManager clientManager = new ThriftClientManager();
        FramedClientConnector connector = new FramedClientConnector(new InetSocketAddress("localhost", 8899));
        User user = new User();
        user.setEmail("email");
        user.setName("name");
        HelloService helloService = clientManager.createClient(connector, HelloService.class).get();
        String hi = helloService.sayHello(user);
        System.out.println(hi);
    }
}
