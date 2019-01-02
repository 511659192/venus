package com.ym.materials.thrift.codegen.invoke;

import com.ym.materials.thrift.codegen.HelloServiceImpl;
import com.ym.materials.thrift.codegen.gened.HelloService;
import com.ym.materials.thrift.codegen.gened.User;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TFastFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingSocket;
import org.apache.thrift.transport.TTransportException;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * Created by ym on 2019/1/1.
 */
public class NonblockingInvoker {

    @Test
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

    @Test
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
        asyncClient.sayHello(user, new AsynInvokerCallback(countDownLatch));
        countDownLatch.await();
    }

    static class AsynInvokerCallback implements AsyncMethodCallback<HelloService.AsyncClient.sayHello_call> {

        private CountDownLatch countDownLatch;

        public AsynInvokerCallback(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void onComplete(HelloService.AsyncClient.sayHello_call resp) {
            try {
                String result = resp.getResult();
                System.out.println(result);
            } catch (TException e) {
                e.printStackTrace();
            } finally {
                countDownLatch.countDown();
            }
        }

        @Override
        public void onError(Exception e) {
            countDownLatch.countDown();
        }
    }
}
