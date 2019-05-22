// Copyright (C) 2019 Meituan
// All rights reserved
package eurekademo.controller;

/**
 * @author yangmeng
 * @version 1.0
 * @created 2019-05-22 20:20
 **/
public class InvokerController {

    public void test() {
        MethodController methodDemo = new MethodController();
        String aaaa = methodDemo.method("aaaa");
        System.out.println(aaaa);
    }
}