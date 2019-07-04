// Copyright (C) 2019 Meituan
// All rights reserved

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yangmeng
 * @version 1.0
 *
 * @created 2019-05-24 16:25
 **/
public class TestDemo111 {

    public static void main(String[] args) {
        String aa = "金沙大道平安象湖风情小区25栋一单元\n302室";
        System.out.println(aa);

        Pattern pattern = Pattern.compile("[\\s\\S]*");
        Matcher matcher = pattern.matcher(aa);
        System.out.println(matcher.matches());
    }
}