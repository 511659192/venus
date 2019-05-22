// Copyright (C) 2019 Meituan
// All rights reserved
package com.ym.materials.benchmark.bash;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author yangmeng
 * @version 1.0
 * @created 2019-05-18 00:08
 **/
public class BashDemo {

    public static String exec(String command) throws Exception {
        String returnString = "";
        Process pro;
        Runtime runTime = Runtime.getRuntime();
        if (runTime == null) {
            System.err.println("Create runtime false!");
        }
        try {
            List<String> cmds = new ArrayList<String>();
            cmds.add("sh");
            cmds.add("-c");
            cmds.add(command);

            pro = runTime.exec(cmds.toArray(new String[]{}));
            LockSupport.parkUntil(TimeUnit.SECONDS.toNanos(2));
            BufferedReader input = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            PrintWriter output = new PrintWriter(new OutputStreamWriter(pro.getOutputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                returnString = returnString + line + "\n";
            }
            input.close();
            output.close();
            pro.destroy();
        } catch (IOException ex) {
            Logger.getLogger(BashDemo.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnString;
    }

    @Test
    public void test() throws Exception {
        System.out.println(exec("find . -type f | xargs grep proxy | wc"));
    }
}