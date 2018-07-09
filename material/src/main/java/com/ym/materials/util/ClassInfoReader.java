package com.ym.materials.util;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class ClassInfoReader {

    public static void main(String[] args) throws Exception {
        File file = new File("/Users/cdyangmeng/Documents/class");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder comment = new StringBuilder();
        StringBuilder type = new StringBuilder();
        StringBuilder name = new StringBuilder();

        for (String line; (line = reader.readLine()) != null;) {
            if (StringUtils.isBlank(line)) {
                continue;
            }

            line = line.trim();
            if ((line.startsWith("/") && !line.startsWith("//")) || line.contains("@see") || line.equals("*/") || line.equals("*")) {
                continue;
            }

            if (line.startsWith("*") || line.startsWith("//")) {
                line = line.substring(2);
            }

//            if (line.startsWith("private")) {
//                builder.append("\n");
//                continue;
//            }

            if (line.startsWith("private")) {
                comment.append("\n");
                int i1 = line.indexOf(" ");
                int i2 = line.lastIndexOf(" ");
                int len = line.length();
                name.append(line.substring(i2 + 1, len - 1)).append("\n");
                type.append(line.substring(i1 + 1, i2)).append("\n");
            } else {
                comment.append(line);
            }
        }
        System.out.println(name);
        System.out.println("--------------");
        System.out.println(type);
        System.out.println("--------------");
        System.out.println(comment);

    }
}
