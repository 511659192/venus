package com.ym.materials.thrift.invoke;

import com.ym.materials.thrift.HelloServiceImpl;
import com.ym.materials.thrift.gened.HelloService;
import com.ym.materials.thrift.gened.User;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.*;

import java.util.concurrent.CountDownLatch;

/**
 * Created by ym on 2019/1/1.
 */
public class NonblockingInvoker {

    public void startService() throws TTransportException {
        int port = 8091;
        TProcessor tpProcessor = new HelloService.Processor<>(new HelloServiceImpl());
        TNonblockingServerSocket serverTransport = new TNonblockingServerSocket(port);
        TCompactProtocol.Factory protocol = new TCompactProtocol.Factory();
        TFastFramedTransport.Factory transport = new TFastFramedTransport.Factory();

        TNonblockingServer.Args args = new TNonblockingServer.Args(serverTransport);
        args.processor(tpProcessor);
        args.protocolFactory(protocol);
        args.transportFactory(transport);
        TServer server = new TNonblockingServer(args);
        server.serve();
    }

    public void startClient() throws Exception {
        String ip = "127.0.0.1";
        int port = 8091;
        int timeout = 1000;
        TAsyncClientManager clientManager = new TAsyncClientManager();
        TNonblockingSocket transport = new TNonblockingSocket(ip, port, timeout);
        TProtocolFactory tprotocol = new TCompactProtocol.Factory();
        HelloService.AsyncClient asyncClient = new HelloService.AsyncClient(tprotocol, clientManager, transport);

        User user = new User();
        user.setName("yangmeng");
        user.setEmail("email@meituan.com");

        CountDownLatch countDownLatch = new CountDownLatch(1);

        asyncClient.sayHello(user, new AsyncMethodCallback() {
            @Override
            public void onComplete(Object o) {
                countDownLatch.countDown();
                System.out.println(o);
            }

            @Override
            public void onError(Exception e) {

            }
        });
        countDownLatch.await();
    }
}
