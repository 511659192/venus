// Copyright (C) 2019 Meituan
// All rights reserved
package eurekademo;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yangmeng
 * @version 1.0
 * @created 2019-03-05 16:51
 **/
public class test2 {


    public static void main(String[] args) throws IOException {

        File file = new File("/Users/yangmeng/Documents/二期鉴权");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        Set<String> controllers = Sets.newHashSet();
        while (StringUtils.isNotBlank(line = reader.readLine())) {
            controllers.add(line.substring(0, line.indexOf(".")));
        }

        for (String controller : controllers) {
            System.out.print(controller + " ");
        }
    }
}