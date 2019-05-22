// Copyright (C) 2019 Meituan
// All rights reserved
package com.ym.materials.jvm;

import java.util.concurrent.locks.LockSupport;

/**
 * @author yangmeng
 * @version 1.0
 * @created 2019-03-03 23:54
 **/
public class TestThreadOverview {

    private String str1 = "str1";
    private String str2 = "str2";

    public static void main(String[] args) {
        TestThreadOverview test = new TestThreadOverview();

        String local_str = "local_str";

        LockSupport.park();
    }
}