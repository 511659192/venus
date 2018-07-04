package com.ym.materials.gson.stream;

import com.sun.javafx.css.CssError;
import com.ym.materials.json.JSON;
import com.ym.materials.json.serializer.JSONSerializer;
import org.junit.Assert;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;

/**
 * Created by ym on 2018/7/2.
 */
public class JsonReader {

    private static final int PEEKED_NONE = 0; // 未设置
    private static final int PEEKED_BEGIN_OBJECT = 1; // 对象开始
    private static final int PEEKED_END_OBJECT = 2; // 对象结束
    private static final int PEEKED_BEGIN_ARRAY = 3; // 数组开始
    private static final int PEEKED_END_ARRAY = 4; // 数组结束
    private static final int PEEKED_TRUE = 5; // true
    private static final int PEEKED_FALSE = 6; // false
    private static final int PEEKED_NULL = 7; // null
    private static final int PEEKED_SINGLE_QUOTED = 8; // 单引号
    private static final int PEEKED_DOUBLE_QUOTED = 9; // 双引号
    private static final int PEEKED_UNQUOTED = 10; //

    private static final int PEEKED_BUFFERED = 11;
    private static final int PEEKED_SINGLE_QUOTED_NAME = 12;
    private static final int PEEKED_DOUBLE_QUOTED_NAME = 13;
    private static final int PEEKED_UNQUOTED_NAME = 14;

    private static final int PEEKED_LONG = 15;
    private static final int PEEKED_NUMBER = 16;
    private static final int PEEKED_EOF = 17;

    private static final int NUMBER_CHAR_NONE = 0;
    private static final int NUMBER_CHAR_SIGN = 1;
    private static final int NUMBER_CHAR_DIGIT = 2;
    private static final int NUMBER_CHAR_DECIMAL = 3;
    private static final int NUMBER_CHAR_FRACTION_DIGIT = 4;
    private static final int NUMBER_CHAR_EXP_E = 5;
    private static final int NUMBER_CHAR_EXP_SIGN = 6;
    private static final int NUMBER_CHAR_EXP_DIGIT = 7;

    private final char[] buffer = new char[1024];
    private int pos = 0;
    private int limit = 0;

    private int lineNumber = 0;
    private int lineStart = 0;

    int peeked = PEEKED_NONE;

    private int[] stack = new int[32];
    private int stackSize = 0;

    {
        stack[stackSize++] = JsonScope.EMPTY_DOCUMENT;
    }

    private Reader in;

    private boolean lenient = false;
    private String[] pathNames = new String[32];
    private int[] pathIndices = new int[32];

    public JsonReader(Reader in) {
        Assert.assertNotNull(in);
        this.in = in;
    }

    public void skipValue() {

    }

    public JsonToken peek() {
        return null;
    }

    public String nextString() throws IOException {
        int p = peeked;
        if (p == PEEKED_NULL) {
            p = doPeek();
        }


        return null;
    }

    private int doPeek() throws IOException {
        int peekStack = stack[stackSize - 1];
        if (peekStack == JsonScope.EMPTY_ARRAY) {
            stack[stackSize - 1] = JsonScope.NONEMPTY_ARRAY;
        } else if (peekStack == JsonScope.NONEMPTY_ARRAY) {
            int c = nextNonWhitespace(true);
            switch (c) {
                case ']':
                    return peeked = PEEKED_END_ARRAY;
                case ';':
                    checkLenient();
                case ',':
                    break;
                default:
                    throw new RuntimeException("unterminated array");
            }
        } else if (peekStack == JsonScope.EMPTY_OBJECT || peekStack == JsonScope.NONEMPTY_OBJECT) {
            stack[stackSize - 1] = JsonScope.DANGLING_NAME;
            if (peekStack == JsonScope.NONEMPTY_OBJECT) {
                int c = nextNonWhitespace(true);
                switch (c) {
                    case '}':
                        return peeked = PEEKED_END_OBJECT;
                    case ';':
                        checkLenient();
                    case ',':
                        break;
                    default:
                        throw new RuntimeException("unterminated object");
                }
            }
            int c = nextNonWhitespace(true);
            switch (c) {
                case ':':
                    return peeked = PEEKED_DOUBLE_QUOTED_NAME;
                case '\'':
                    return peeked = PEEKED_SINGLE_QUOTED_NAME;
                case '}':
                    if (peekStack != JsonScope.NONEMPTY_OBJECT) {
                        return peeked = PEEKED_END_OBJECT;
                    } else {
                        throw new RuntimeException("expected name");
                    }
                default:
                    checkLenient();
                    pos--;
                    if (isLiteral((char) c)) {
                        return peeked = PEEKED_UNQUOTED_NAME;
                    } else {
                        throw new RuntimeException("expected name");
                    }
            }
        }

        return 0;
    }

    private boolean isLiteral(char c) {
        return false;
    }

    private int nextNonWhitespace(boolean throwOnEof) throws IOException {
        char[] buffer = this.buffer;
        int p = this.pos;
        int l = this.limit;
        for (;;) {
            if (p == l) {
                pos = p;
                if (!fillBuffer(1)) {
                    break;
                }
                p = pos;
                l = limit;
            }

            int c = buffer[p++];
            if (c == '\n') {
                lineNumber++;
                lineStart = p;
                continue;
            } else if (c == ' ' || c == '\r' || c == '\t') {
                continue;
            }

            if (c == '/') {
                pos = p;
                if (p == l) {
                    pos--;
                    boolean charsLoaded = fillBuffer(2);
                    pos++;
                    if (!charsLoaded) {
                        return c;
                    }
                }
                checkLenient();
                char peek = buffer[pos];
                switch (peek) {
                    case '*':
                        pos++;
                        if (!skipTo("*/")) {
                            throw new RuntimeException("unterminated comment");
                        }
                        p = pos + 2;
                        l = limit;
                        continue;
                    case '/':
                        pos++;
                        skipToEndOfLine();
                        p = pos;
                        l = limit;
                        continue;
                    default:
                            return c;
                }
            } else if (c == '#') {
                pos = p;
                checkLenient();
                skipToEndOfLine();
                p = pos;
                l = limit;
            } else {
                pos = p;
                return c;
            }
        }
        if (throwOnEof) {
            throw new EOFException("end of input " + locationString());
        } else {
            return -1;
        }
    }

    String locationString() {
        int line = lineNumber + 1;
        int column = pos - lineStart + 1;
        return " at line " + line + " column " + column + " path " + getPath();
    }

    public String getPath() {
        StringBuilder result = new StringBuilder().append('$');
        for (int i = 0, size = stackSize; i < size; i++) {
            switch (stack[i]) {
                case JsonScope.EMPTY_ARRAY:
                case JsonScope.NONEMPTY_ARRAY:
                    result.append('[').append(pathIndices[i]).append(']');
                    break;

                case JsonScope.EMPTY_OBJECT:
                case JsonScope.DANGLING_NAME:
                case JsonScope.NONEMPTY_OBJECT:
                    result.append('.');
                    if (pathNames[i] != null) {
                        result.append(pathNames[i]);
                    }
                    break;

                case JsonScope.NONEMPTY_DOCUMENT:
                case JsonScope.EMPTY_DOCUMENT:
                case JsonScope.CLOSED:
                    break;
            }
        }
        return result.toString();
    }

    private void skipToEndOfLine() {
    }

    private boolean skipTo(String s) {
        return false;
    }

    private void checkLenient() throws IOException {
        if (!lenient) {
            throw new RuntimeException("Use JsonReader.setLenient(true) to accept malformed JSON");
        }
    }

    private boolean fillBuffer(int i) {
        return false;
    }

    public boolean nextBoolean() {
        return false;
    }

    public void nextNull() {

    }

    public void beginArray() {

    }

    public boolean hasNext() {
        return false;
    }

    public void endArray() {

    }

    public void beginObject() {

    }

    public String nextName() {
        return null;
    }

    public void endObject() {

    }

    public double nextDouble() {
        return 0;
    }

    public int nextInt() {
        return 0;
    }

    public long nextLong() {
        return 0;
    }
}
