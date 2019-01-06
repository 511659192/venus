package com.ym.materials.thrift.annotaion;

import com.facebook.nifty.core.NettyServerConfig;
import com.facebook.nifty.core.ThriftServerDef;
import com.facebook.swift.codec.ThriftCodecManager;
import com.facebook.swift.service.ThriftServer;
import com.facebook.swift.service.ThriftServiceProcessor;
import com.google.common.collect.Lists;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ym on 2019/1/6.
 */
public class ServerMain {

    public static void main(String[] args) {
        ThriftServiceProcessor processor = new ThriftServiceProcessor(new ThriftCodecManager(), Lists.newArrayList(), new HelloServiceImpl());
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        ThriftServerDef serverDef = ThriftServerDef.newBuilder()
                .listen(8899)
                .withProcessor(processor)
                .using(executorService)
                .build();

        ExecutorService boss = Executors.newCachedThreadPool();
        ExecutorService worker = Executors.newCachedThreadPool();
        NettyServerConfig serverConfig = NettyServerConfig.newBuilder()
                .setBossThreadExecutor(boss)
                .setWorkerThreadExecutor(worker)
                .build();

        ThriftServer server = new ThriftServer(serverConfig, serverDef);
        server.start();
    }
}
