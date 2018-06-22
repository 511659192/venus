package com.ym.materials.json;

/**
 * Created by ym on 2018/6/21.
 */
public enum PropertyNamingStrategy {

    CamelCase, //
    PascalCase, //
    SnakeCase, //
    KebabCase;

    public String translate(String propertyName) {
        switch (this) {
            case SnakeCase: {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < propertyName.length(); i++) {
                    char ch = propertyName.charAt(i);
                    if (ch >= 'A' && ch <= 'Z') {
                        char used = (char) (ch + 32);
                        if (i > 0) {
                            builder.append("_");
                        }
                        builder.append(used);
                    } else {
                        builder.append(ch);
                    }
                }
                return builder.toString();
            }
            case KebabCase:
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < propertyName.length(); i++) {
                    char ch = propertyName.charAt(i);
                    if (ch >= 'A' && ch <= 'Z') {
                        char used = (char) (ch + 32);
                        if (i > 0) {
                            builder.append("-");
                        }
                        builder.append(used);
                    } else {
                        builder.append(ch);
                    }
                }
                return builder.toString();
            case CamelCase: {
                char ch = propertyName.charAt(0);
                if (ch >= 'A' && ch <= 'Z') {
                    char[] chars = propertyName.toCharArray();
                    chars[0] += 32;
                    return new String(chars);
                }
                return propertyName;
            }

            case PascalCase: {
                char ch = propertyName.charAt(0);
                if (ch >= 'a' && ch <= 'z') {
                    char[] chars = propertyName.toCharArray();
                    chars[0] -= 32;
                    return new String(chars);
                }
                return propertyName;
            }
            default:
                return propertyName;
        }
    }
}
