package com.ym.materials.json.util;

public class IOUtils {

    // TODO: 这个方式 可以提升性能 值得考虑
    public final static char[] replaceChars = new char[93];
    public final static byte[] specicalFlags_singleQuotes = new byte[161];
    public final static byte[] specicalFlags_doubleQuotes = new byte[161];
    public static final char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public final static char[] ASCII_CHARS = { '0', '0', '0', '1', '0', '2', '0', '3', '0', '4', '0',
            '5', '0', '6', '0', '7', '0', '8', '0', '9', '0', 'A', '0', 'B', '0', 'C', '0', 'D', '0', 'E', '0', 'F',
            '1', '0', '1', '1', '1', '2', '1', '3', '1', '4', '1', '5', '1', '6', '1', '7', '1', '8', '1', '9', '1',
            'A', '1', 'B', '1', 'C', '1', 'D', '1', 'E', '1', 'F', '2', '0', '2', '1', '2', '2', '2', '3', '2', '4',
            '2', '5', '2', '6', '2', '7', '2', '8', '2', '9', '2', 'A', '2', 'B', '2', 'C', '2', 'D', '2', 'E', '2',
            'F',};

    static {
        specicalFlags_doubleQuotes['\0'] = 4;
        specicalFlags_doubleQuotes['\1'] = 4;
        specicalFlags_doubleQuotes['\2'] = 4;
        specicalFlags_doubleQuotes['\3'] = 4;
        specicalFlags_doubleQuotes['\4'] = 4;
        specicalFlags_doubleQuotes['\5'] = 4;
        specicalFlags_doubleQuotes['\6'] = 4;
        specicalFlags_doubleQuotes['\7'] = 4;
        specicalFlags_doubleQuotes['\b'] = 1; // 8
        specicalFlags_doubleQuotes['\t'] = 1; // 9
        specicalFlags_doubleQuotes['\n'] = 1; // 10
        specicalFlags_doubleQuotes['\u000B'] = 4; // 11
        specicalFlags_doubleQuotes['\f'] = 1; // 12
        specicalFlags_doubleQuotes['\r'] = 1; // 13
        specicalFlags_doubleQuotes['\"'] = 1; // 34
        specicalFlags_doubleQuotes['\\'] = 1; // 92

        specicalFlags_singleQuotes['\0'] = 4;
        specicalFlags_singleQuotes['\1'] = 4;
        specicalFlags_singleQuotes['\2'] = 4;
        specicalFlags_singleQuotes['\3'] = 4;
        specicalFlags_singleQuotes['\4'] = 4;
        specicalFlags_singleQuotes['\5'] = 4;
        specicalFlags_singleQuotes['\6'] = 4;
        specicalFlags_singleQuotes['\7'] = 4;
        specicalFlags_singleQuotes['\b'] = 1; // 8
        specicalFlags_singleQuotes['\t'] = 1; // 9
        specicalFlags_singleQuotes['\n'] = 1; // 10
        specicalFlags_singleQuotes['\u000B'] = 4; // 11
        specicalFlags_singleQuotes['\f'] = 1; // 12
        specicalFlags_singleQuotes['\r'] = 1; // 13
        specicalFlags_singleQuotes['\\'] = 1; // 92
        specicalFlags_singleQuotes['\''] = 1; // 39

        for (int i = 14; i <= 31; ++i) {
            specicalFlags_doubleQuotes[i] = 4;
            specicalFlags_singleQuotes[i] = 4;
        }

        for (int i = 127; i < 160; ++i) {
            specicalFlags_doubleQuotes[i] = 4;
            specicalFlags_singleQuotes[i] = 4;
        }

        replaceChars['\0'] = '0';
        replaceChars['\1'] = '1';
        replaceChars['\2'] = '2';
        replaceChars['\3'] = '3';
        replaceChars['\4'] = '4';
        replaceChars['\5'] = '5';
        replaceChars['\6'] = '6';
        replaceChars['\7'] = '7';
        replaceChars['\b'] = 'b'; // 8
        replaceChars['\t'] = 't'; // 9
        replaceChars['\n'] = 'n'; // 10
        replaceChars['\u000B'] = 'v'; // 11
        replaceChars['\f'] = 'f'; // 12
        replaceChars['\r'] = 'r'; // 13
        replaceChars['\"'] = '"'; // 34
        replaceChars['\''] = '\''; // 39
        replaceChars['/'] = '/'; // 47
        replaceChars['\\'] = '\\'; // 92
    }
}
