package com.ym.materials.thrift.invoke;

import com.ym.materials.thrift.HelloServiceImpl;
import com.ym.materials.thrift.gened.HelloService;
import com.ym.materials.thrift.gened.User;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

/**
 * Created by ym on 2019/1/1.
 */
public class SimpleInvoke {

    public void startService() throws TTransportException {
        TProcessor tpProcessor = new HelloService.Processor<>(new HelloServiceImpl());
        int port = 8091;
        TServerSocket serverTransport = new TServerSocket(port);
        TBinaryProtocol.Factory protocol = new TBinaryProtocol.Factory();
        TServer.Args args = new TServer.Args(serverTransport);
        args.processor(tpProcessor);
        args.protocolFactory(protocol);
        TServer server = new TSimpleServer(args);
        server.serve();
    }

    public void startClient() throws TException {
        String ip = "127.0.0.1";
        int port = 8091;
        int timeout = 1000;
        TTransport transport = new TSocket(ip, port, timeout);
        TProtocol protocol = new TBinaryProtocol(transport);
        HelloService.Client client = new HelloService.Client(protocol);
        transport.open();
        User user = new User();
        user.setName("yangmeng");
        user.setEmail("email@meituan.com");
        String content = client.sayHello(user);
        System.out.println(content);
    }
}
