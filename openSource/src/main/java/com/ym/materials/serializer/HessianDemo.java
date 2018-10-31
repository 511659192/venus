package com.ym.materials.serializer;

import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.HessianOutput;

import java.io.*;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ym on 2018/10/29.
 */
public class HessianDemo {

    private static Person person = new Person("aaaa", 18);
    private static Person person2 = new Person("bbbb", 20);
    private static Person person3 = new Person("ccc", 22);
    public static void main(String[] args) throws IOException {

        String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        List<Person> list = new ArrayList<>();
        list.add(person);
        list.add(person2);
        list.add(person3);

        FileOutputStream fos = new FileOutputStream(path + "/" + HessianDemo.class.getPackage().toString().replaceAll(".", "/") + "/" + HessianDemo.class.getName());
        Hessian2Output ho = new Hessian2Output(fos);
        ho.writeObject(list);
        ho.close();
    }
}
