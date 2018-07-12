package com.ym.materials.gson.stream;

import com.ym.materials.gson.internal.Preconditions;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.Future;

import static com.ym.materials.gson.stream.JsonToken.*;

/**
 * Created by ym on 2018/7/8.
 */
public class JsonReader extends JsonScope implements Closeable {

    private static final long MIN_INCOMPLETE_INTEGER = Long.MIN_VALUE / 10;

    private boolean lenient;
    private int peeked = PEEKED_NONE;

    private static final int PEEKED_NONE = 0;
    private static final int PEEKED_BEGIN_OBJECT = 1;
    private static final int PEEKED_END_OBJECT = 2;

    private static final int PEEKED_TRUE = 5;
    private static final int PEEKED_FALSE = 6;
    private static final int PEEKED_NULL = 7;
    private static final int PEEKED_DOUBLE_QUOTED = 9;
    private static final int PEEKED_UNQUOTED = 10;

    private static final int PEEKED_BUFFERED = 11;
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

    private final Reader in;
    private char[] buffer = new char[1024];
    private int pos = 0; // 已经使用过的数据 fillBuffer的时候可以清除
    private int limit = 0;
    private int lineNumber = 0;
    private int lineStart = 0;

    private int[] stack = new int[32];
    private int stackSize = 0;
    private String[] pathNames = new String[32];
    private int[] pathIndices = new int[32];

    private long peekedLong;
    private int peekedNumberLength;
    private String peekedString;


    {
        push(EMPTY_DOCUMENT);
    }

    public JsonReader(StringReader in) {
        Preconditions.checkNotNull(in);
        this.in = in;
    }

    @Override
    public void close() throws IOException {

    }

    public boolean isLenient() {
        return lenient;
    }

    public void setLenient(boolean lenient) {
        this.lenient = lenient;
    }

    public JsonToken peek() throws IOException {
        int p = doPeek();
        switch (p) {
            case PEEKED_BEGIN_OBJECT:
                return BEGIN_OBJECT;
            case PEEKED_END_OBJECT:
                return END_OBJECT;
            case PEEKED_DOUBLE_QUOTED:
                return STRING;
            case PEEKED_TRUE:
            case PEEKED_FALSE:
                return BOOLEAN;
            case PEEKED_NULL:
                return NULL;
            case PEEKED_NUMBER:
            case PEEKED_LONG:
                return NUMBER;
            case PEEKED_DOUBLE_QUOTED_NAME:
            case PEEKED_UNQUOTED_NAME:
            case PEEKED_BUFFERED:
                return NAME;
            case PEEKED_EOF:
                return END_DOCUMENT;
            default:
                throw new RuntimeException("not support");
        }
    }

    private int doPeek() throws IOException {
        int p = peeked;
        if (p != PEEKED_NONE) {
            return p;
        }

        int scope = top();
        if (scope == EMPTY_DOCUMENT) {
            if (lenient) {
                nextNonWhitespace(true);
                pos--;
            }
            top(NONEMPTY_DOCUMENT);
        } else if (scope == EMPTY_OBJECT) {
        } else {
        }

        int c = nextNonWhitespace(true);
        switch (c) {
            case '"':
                checkLenient();
                return peeked = PEEKED_DOUBLE_QUOTED;
            case '{':
                return peeked = PEEKED_BEGIN_OBJECT;
            default:
                pos--;
        }

        int result = peekKeyWork();
        if (result != PEEKED_NONE) {
            return result;
        }

        result = peekNumber();
        if (result != PEEKED_NONE) {
            return result;
        }

        if (!isLiteral(buffer[pos])) {
            throw new RuntimeException();
        }

        checkLenient();
        return peeked = PEEKED_UNQUOTED;
    }

    public static void main1(String[] args) {
        long value = MIN_INCOMPLETE_INTEGER - 1;
        while (true) {
            long newValue = value * 10;
            boolean b2 = value < MIN_INCOMPLETE_INTEGER && newValue >= value;
            if (b2) {
                System.out.println(value - MIN_INCOMPLETE_INTEGER);
                System.out.println("value   :" + value);
                System.out.println("newValue:" + newValue);
            }
            value = newValue;
        }
    }


    /**
     * 正整数: NUMBER_CHAR_NONE -> NUMBER_CHAR_DIGIT
     * 负整数：NUMBER_CHAR_NONE -> NUMBER_CHAR_SIGN -> NUMBER_CHAR_DIGIT
     * 正小数：NUMBER_CHAR_NONE -> NUMBER_CHAR_DIGIT -> NUMBER_CHAR_DECIMAL -> NUMBER_CHAR_FRACTION_DIGIT
     * 负小数：NUMBER_CHAR_NONE -> NUMBER_CHAR_SIGN-> NUMBER_CHAR_DIGIT -> NUMBER_CHAR_DECIMAL -> NUMBER_CHAR_FRACTION_DIGIT
     * 科学计数+：NUMBER_CHAR_NONE -> NUMBER_CHAR_DIGIT -> NUMBER_CHAR_DECIMAL -> NUMBER_CHAR_FRACTION_DIGIT
     * NUMBER_CHAR_EXP_E -> NUMBER_CHAR_EXP_SIGN -> NUMBER_CHAR_EXP_DIGIT
     *
     * @return
     * @throws IOException
     */
    private int peekNumber() throws IOException {
        int p = pos;
        int l = limit;

        boolean nevigate = false; // 是否负值
        boolean fitsInLong = true; // 没看懂 数据极端场景
        long value = 0;
        int i = 0; // 下标
        int last = NUMBER_CHAR_NONE;
        outer:
        for (; true; i++) {
            if (p + i == l) {
                if (i == buffer.length) {
                    return PEEKED_NONE;
                }

                if (!fillBuffer(i + 1)) {
                    break;
                }
                p = pos;
                l = limit;
            }

            char c = buffer[pos];
            switch (c) {
                case '-': // 负值 或者 科学计数
                    if (last == NUMBER_CHAR_NONE) {
                        nevigate = true;
                        last = NUMBER_CHAR_SIGN;
                        continue;
                    } else if (last == NUMBER_CHAR_EXP_E) {
                        last = NUMBER_CHAR_EXP_SIGN;
                        continue;
                    }
                    return PEEKED_NONE;
                case '+':
                    if (last == NUMBER_CHAR_EXP_E) {
                        last = NUMBER_CHAR_EXP_SIGN;
                        continue;
                    }
                    return PEEKED_NONE;
                case 'e':
                case 'E': // e 存在两个状态 NUMBER_CHAR_EXP_E NUMBER_CHAR_EXP_SIGN
                    if (last == NUMBER_CHAR_DIGIT || last == NUMBER_CHAR_FRACTION_DIGIT) {
                        last = NUMBER_CHAR_EXP_E;
                        continue;
                    }
                    return PEEKED_NONE;
                case '.': // 小数处理
                    if (last == NUMBER_CHAR_DIGIT) {
                        last = NUMBER_CHAR_DECIMAL;
                        continue;
                    }
                    return PEEKED_NONE;
                default:
                    if (c < '0' || c > '9') { // 字符型 代表数值结束
                        if (isLiteral(c)) {
                            return c;
                        }
                    }
                    if (last == NUMBER_CHAR_NONE || last == NUMBER_CHAR_SIGN) { // 空 或者 负值
                        value = -(c - '0');
                        last = NUMBER_CHAR_DIGIT;
                    } else if (last == NUMBER_CHAR_DIGIT) {
                        if (value == 0) {
                            return PEEKED_NONE;
                        }
                        long newValue = value * 10 - (c - '0'); // char 2 long
                        fitsInLong &= value > MIN_INCOMPLETE_INTEGER || (value == MIN_INCOMPLETE_INTEGER && value > newValue);
                        value = newValue;
                    } else if (last == NUMBER_CHAR_DIGIT) {
                        last = NUMBER_CHAR_FRACTION_DIGIT;
                    } else if (last == NUMBER_CHAR_EXP_E || last == NUMBER_CHAR_EXP_SIGN) {
                        last = NUMBER_CHAR_EXP_DIGIT;
                    }
            }

        }

        // 数值类型
        if (last == NUMBER_CHAR_DIGIT && fitsInLong && (value != Long.MIN_VALUE || nevigate) && (value != 0 || !nevigate)) { // 注意这种写法
            peekedLong = nevigate ? value : -value;
            pos += i;
            return peeked = PEEKED_LONG;
        } else if (last == NUMBER_CHAR_DIGIT || last == NUMBER_CHAR_DECIMAL || last == NUMBER_CHAR_FRACTION_DIGIT) {
            peekedNumberLength = i; // 数值类型长度
            return peeked = PEEKED_NUMBER;
        } else {
            return PEEKED_NONE;
        }
    }

    private int peekKeyWork() throws IOException {
        char c = buffer[pos];
        String keyword;
        String keyworkUpper;
        int peeking;
        if (c == 't' || c == 'T') {
            keyword = "true";
            keyworkUpper = "TRUE";
            peeking = PEEKED_TRUE;
        } else if (c == 'f' || c == 'F') {
            keyword = "false";
            keyworkUpper = "FALSE";
            peeking = PEEKED_FALSE;
        } else if (c == 'n' || c == 'N') {
            keyword = "null";
            keyworkUpper = "NULL";
            peeking = PEEKED_NULL;
        } else {
            return PEEKED_NONE;
        }

        int length = keyword.length();
        for (int i = 1; i < length; i++) {
            if (pos + i >= limit && !fillBuffer(i + 1)) {
                return PEEKED_NONE;
            }
            c = buffer[pos + i];
            if (c != keyword.charAt(i) && c != keyworkUpper.charAt(i)) {
                return PEEKED_NONE;
            }
        }

        if ((pos + length < limit || fillBuffer(length + 1)) && isLiteral(buffer[pos + length])) {
            return PEEKED_NONE;
        }

        pos += length;
        return peeked = peeking;
    }

    private int nextNonWhitespace(boolean throwOnEof) throws IOException {
        for (; ; ) {
            if (pos == limit) {
                boolean filled = fillBuffer(1);
                if (!filled) {
                    break; // 返回-1 或者抛出异常
                }
            }

            char c = buffer[pos++];
            if (c == '\n') {
                lineStart = pos;
                lineNumber++;
                continue;
            } else if (c == ' ' || c == '\r' || c == '\t') {
                continue;
            }

            if (c == '/') {
                if (pos == limit) {
                    pos--;
                    boolean filled = fillBuffer(2);
                    pos++;
                    if (!filled) {
                        return c; // 返回 '/'
                    }
                }

                checkLenient();
                int next = buffer[pos];
                switch (next) {
                    case '*':
                        pos++;
                        boolean skipTo = skipTo("*/");
                        if (!skipTo) {
                            throw new RuntimeException();
                        }
                        pos = pos + 2; // 回退到开始的地方
                        continue;
                    case '/':
                        pos++;
                        skipToEndLine();
                        continue;
                    default:
                        return c;
                }
            } else if (c == '#') {
                checkLenient();
                skipToEndLine();
            } else {
                return c;
            }
        }

        if (throwOnEof) {
            throw new EOFException("can not end now");
        } else {
            return -1;
        }
    }

    private void skipToEndLine() throws IOException {
        for (; pos < limit || fillBuffer(1); pos++) {
            char c = buffer[pos];
            if (c == '\n') {
                lineNumber++;
                lineStart = pos + 1;
                break;
            } else if (c == '\r') {
                break;
            }
        }
    }

    private boolean skipTo(String toFind) throws IOException {
        int length = toFind.length();
        outer:
        for (; pos <= limit || fillBuffer(length); pos++) {
            char c = buffer[pos];
            if (c == '\n') {
                lineNumber++;
                lineStart = pos + 1;
                continue;
            }

            for (int p = 0; p < length; p++) {
                if (buffer[pos + p] != toFind.charAt(p)) {
                    continue outer;
                }
            }
            return true;
        }
        return false;
    }

    private boolean fillBuffer(int min) throws IOException {
        lineStart -= pos;
        if (pos != limit) {
            limit -= pos;
            System.arraycopy(buffer, pos, buffer, 0, limit);
        } else {
            limit = 0;
        }

        pos = 0;
        for (int total; (total = in.read(buffer, limit, buffer.length - limit)) != -1; ) {
            limit += total;
            if (lineNumber == 0 && lineStart == 0 && limit > 0 && buffer[0] == '\ufeff') {
                lineStart++;
                pos++;
                min++;
            }

            if (limit >= min) {
                return true;
            }
        }
        return false;
    }

    private void checkLenient() throws IOException {
        if (!lenient) {
            throw new RuntimeException();
        }
    }

    private void push(int scope) {
        expandIfNeccesafy();
        stack[stackSize++] = scope;
    }

    private void expandIfNeccesafy() {
        if (stackSize == stack.length) {
            int[] newStack = new int[stackSize << 1];
            System.arraycopy(stack, 0, newStack, 0, stackSize);
            stack = newStack;
        }
    }

    private void top(int newTop) {
        stack[stackSize - 1] = newTop;
    }

    private int top() {
        return stack[stackSize - 1];
    }

    private boolean isLiteral(char c) throws IOException {
        switch (c) {
            case '/':
            case '\\':
            case ';':
            case '#':
            case '=':
                checkLenient(); // fall-through
            case '{':
            case '}':
            case '[':
            case ']':
            case ':':
            case ',':
            case ' ':
            case '\t':
            case '\f':
            case '\r':
            case '\n':
                return false;
            default:
                return true;
        }
    }

    public void beginObject() throws IOException {
        int p = doPeek();
        if (p == PEEKED_BEGIN_OBJECT) {
            push(EMPTY_OBJECT);
            peeked = NONEMPTY_OBJECT;
        } else {
            throw new RuntimeException("expect begin object but was " + peek());
        }
    }

    public boolean hashNext() throws IOException {
        int peek = doPeek();
        return peek != PEEKED_END_OBJECT;
    }

    public String nextName() throws IOException {
        int peek = doPeek();
        String result;
        switch (peek) {
            case PEEKED_DOUBLE_QUOTED_NAME:
                result = nextQuotedValue('\"');
                break;
            case PEEKED_UNQUOTED_NAME:
                result = nextUnquotedValue();
                break;
            default:
                throw new RuntimeException("not support");
        }
        peeked = PEEKED_NONE;
        return result;
    }

    public static void main(String[] args) throws IOException {
        String text = "01234567";
        JsonReader jsonReader = new JsonReader(new StringReader(text));
        jsonReader.buffer = new char[8];
        jsonReader.fillBuffer(1);
        jsonReader.pos = 5;
        String s = jsonReader.nextUnquotedValue();
        System.out.println(s);
    }

    private String nextUnquotedValue() throws IOException {
        StringBuilder builder = null;
        int len = 0;
        outer:
        while (true) {
            for (; pos + len < limit; len++) {
                char c = buffer[pos + len];
                if (!isLiteral(c)) { // 见到字符 当前字符串已经结束
                    break outer;
                }
            }

            if (len < buffer.length) {
                if (fillBuffer(len + 1)) { // 为什么填充i+1呢
                    continue;
                } else {
                    break;
                }
            }

            builder = newStringBuilder(builder, len);
            builder.append(buffer, pos, len);
            pos += len;
            len = 0;
            if (!fillBuffer(1)) {
                return builder.toString();
            }
        }
        String result = (builder == null) ? new String(buffer, pos, len) : builder.append(buffer, pos, len).toString();
        pos += len;
        return result;
    }

    private StringBuilder newStringBuilder(StringBuilder builder, int capacity) {
        return builder != null ? builder : new StringBuilder(Math.max(16, capacity));
    }

    private String nextQuotedValue(char quote) throws IOException {
        StringBuilder builder = null;
        while (true) {
            int start = pos;
            while (pos < limit) {
                char c = buffer[pos++];
                if (c == quote) {
                    int length = pos - start - 1;
                    if (builder == null) {
                        return new String(buffer, start, length); // 当前buffer 直接返回
                    } else {
                        builder.append(buffer, start, length); // 多次循环buffer
                        return builder.toString();
                    }
                }

                if (c == '\\') {
                    int length = pos - start - 1; // 排除\\
                    if (builder == null) {
                        builder = new StringBuilder(Math.max((length + 1) << 1, 16));
                    }
                    builder.append(builder, start, length);
                    builder.append(readEscapeCharacter());
                    start = pos;
                } else if (c == '\n') {
                    lineNumber++;
                    lineStart = pos;
                }
            }

            // 超过limit
            int length = pos - start;
            if (builder == null) {
                builder = new StringBuilder(Math.max(length << 1, 16));
            }

            builder.append(buffer, start, length);
            if (!fillBuffer(1)) {
                throw new RuntimeException("unterminated string");
            }
        }
    }

    private char readEscapeCharacter() throws IOException {
        if (pos == limit && !fillBuffer(1)) {
            throw new RuntimeException();
        }

        char escaped = buffer[pos++];
        switch (escaped) {
            case 'u':
                if (pos + 4 > limit && !fillBuffer(4)) {
                    throw new RuntimeException();
                }

                char result = 0;
                for (int i = pos, end = i + 4; i < end; i++) {
                    char c = buffer[i];
                    result <<= 4;
                    if (c >= '0' && c <= '9') {
                        result += (c - '0');
                    } else if (c >= 'a' && c <= 'f') {
                        result += (c - 'a' + 10);
                    } else if (c >= 'A' && c <= 'F') {
                        result += (c - 'A' + 10);
                    } else {
                        throw new RuntimeException("uuid 必须是16进制");
                    }
                }
                pos += 4;
                return result;
            case 't': return '\t';
            case 'b': return '\b';
            case 'n': return '\n';
            case 'r': return '\r';
            case 'f': return '\f';
            case '\n':
                lineNumber++;
                lineStart = pos;
            case '\'':
            case '"':
            case '\\':
            case '/':
                return escaped;
            default:
                // throw error when none of the above cases are matched
                throw new RuntimeException("Invalid escape sequence");

        }
    }
}
