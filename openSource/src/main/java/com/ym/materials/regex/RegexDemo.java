// Copyright (C) 2019 Meituan
// All rights reserved
package com.ym.materials.regex;

import org.junit.Test;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yangmeng
 * @version 1.0
 * @created 2019-07-09 17:11
 **/
public class RegexDemo {
    public static void main(String[] args) {

        String text = "实际发放优惠券数量:22 优惠金额:100.12";
        String reg = "实际发放优惠券数量:(\\d+) 优惠金额:(.*)";
        Pattern compile = Pattern.compile(reg);
        Matcher matcher = compile.matcher(text);

        if (matcher.find()) {
            String bbbb = matcher.replaceFirst("bbbb");
            System.out.println(bbbb);
            System.out.println(matcher.group(1));
            System.out.println(matcher.group(2));
        }

        String aaa = text.replaceFirst("实际发放优惠券数量:(\\d+) 优惠金额:(.*)", "\1");
        System.out.println(aaa);
    }

    @Test
    public void test() {
        String regex = "\\$\\{([^{}]+?)\\}";
        Pattern pattern = Pattern.compile(regex);
        String input = "${name}-babalala-${age}-${address}";

        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            System.out.println(matcher.group());
            System.out.println("---------------");
            System.out.println(matcher.group(0) + ", pos: " + matcher.start() + "|" + matcher.end());
            System.out.println(matcher.group(1) + ", pos: " + matcher.start(1) + "|" + matcher.end());
        }
    }

    @Test
    public  void testReplace() {
        String tel = "18304072984";
        // 括号表示组，被替换的部分$n表示第n组的内容
        tel = tel.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
        // output: 183****2984
        System.out.print(tel);

        String one = "hello girl hi hot".replaceFirst("(\\w+)\\s+(\\w+)", "$2 - $1");
        String two = "hello girl hi hot".replaceAll("(\\w+)\\s+(\\w+)", "$2 - $1");
        // girl hello hi hot
        System.out.println(one);
        // girl hello hot hi
        System.out.println(two);

        String input = "假如生活欺骗了你，，，相信吧，，，快乐的日子将会来临！！！…………";

        // 重复标点符号替换
        String duplicateSymbolReg = "([。？！?!，]|\\.\\.\\.|……)+";
        input = input.replaceAll(duplicateSymbolReg, "$1");
        System.out.println(input);

        String ip = "192.68.1.254 102.49.23.013 10.10.10.10 2.2.2.2 8.109.90.30";
        ip = ip.replaceAll("(\\d+)", "00$1");
        System.out.println(ip);

        ip = ip.replaceAll("0*(\\d{3})", "$1");
        System.out.println(ip);
        String[] strs = ip.split(" ");

        Arrays.sort(strs);
        for (String str : strs) {
            str = str.replaceAll("0*(\\d+)", "$1");
            System.out.println(str);
        }
    }

    @Test
    public void matches() {
        String regex = "(.)\\1(.)\\2";
        // true
        System.out.println("安安静静".matches(regex));
        // false
        System.out.println("安静安静".matches(regex));

        regex = "(..)\\1";
        // true
        System.out.println("安静安静".matches(regex));
        // false
        System.out.println("安安静静".matches(regex));
    }

    @Test
    public void replaceRef() {
        String str = "我我...我我...我要..要要...要要...找找找一个....女女女女...朋朋朋朋朋朋...友友友友友..友.友...友...友友！！！";

        /*将 . 去掉*/
        str = str.replaceAll("\\.+", "");
        System.out.println(str);

        str = str.replaceAll("(.)\\1+", "$1");
        System.out.println(str);

        String s = "xx12abdd12345".replaceAll("(\\d{2}).+?\\1", "");
        System.out.println(s);
        //结果为 xx345
    }


}