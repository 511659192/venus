// Copyright (C) 2019 Meituan
// All rights reserved

import junit.framework.TestSuite;
import org.junit.Test;

/**
 * @author yangmeng
 * @version 1.0
 * @created 2019-05-12 23:08
 **/
public class TestSuitDemo {

    @Test
    public void test() {
        TestSuite testSuite = new TestSuite();
        testSuite.addTest(new TestDemo("test"));
        testSuite.addTest(new TestDemo("test"));
        testSuite.tests();

    }

    public static void main(String[] args) {
    }
}