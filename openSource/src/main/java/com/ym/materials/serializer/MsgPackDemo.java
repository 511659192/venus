package com.ym.materials.serializer;

import org.msgpack.MessagePack;
import org.msgpack.template.ListTemplate;
import org.msgpack.template.Template;
import org.msgpack.template.Templates;
import org.msgpack.type.Value;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ym on 2018/10/29.
 */
public class MsgPackDemo {

    private static Person person = new Person("aaaa", 18);
    private static Person person2 = new Person("bbbb", 20);
    private static Person person3 = new Person("ccc", 22);
    public static void main(String[] args) throws IOException {

        String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        List<Person> list = new ArrayList<>();
        list.add(person);
        list.add(person2);
        list.add(person3);

        Result<List<Person>> result = new Result<>(true, list);

        FileOutputStream fos = new FileOutputStream(path + "/" + MsgPackDemo.class.getPackage().toString().replaceAll(".", "/") + "/" + MsgPackDemo.class.getName());
        MessagePack messagePack = new MessagePack();
        byte[] write = messagePack.write(result);
        fos.write(write);
        fos.close();
//        Template<? extends List<? extends Person>> listTemplate = Templates.tList(messagePack.lookup(person.getClass()));
//        List<? extends Person> read = messagePack.read(write, listTemplate);
        System.out.println("啊11");
    }
}
