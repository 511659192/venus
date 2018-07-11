package com.ym.materials.gson.stream;

import com.ym.materials.gson.internal.Preconditions;
import jdk.nashorn.internal.ir.IfNode;
import jdk.nashorn.internal.runtime.regexp.joni.exception.SyntaxException;

import javax.swing.*;
import java.io.*;
import java.lang.annotation.Target;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.concurrent.Future;

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
    private final char[] buffer = new char[1024];
    private int pos = 0;
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

    public void peek() throws IOException {
        int p = doPeek();
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

    private int peekNumber() throws IOException {
        int p = pos;
        int l = limit;
        long value = 0;
        boolean negative = false;
        boolean fitsInLong = true;
        int last = NUMBER_CHAR_NONE;

        int i = 0;
        outer:
        for (; true; i++) {
            if (p + i == l) {
                if (i == buffer.length) {
                    return PEEKED_NONE;
                }
                if (!fillBuffer(i + 1)) {
                    break;
                }
                l = limit;
                p = pos;
            }

            char c = buffer[pos + i];
            switch (c) {
                case '-':
                    if (last == NUMBER_CHAR_NONE) {
                        last = NUMBER_CHAR_SIGN;
                        negative = true;
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
                case 'E':
                    if (last == NUMBER_CHAR_DIGIT || last == NUMBER_CHAR_FRACTION_DIGIT) {
                        last = NUMBER_CHAR_EXP_E;
                        continue;
                    }
                    return PEEKED_NONE;
                case '.':
                    if (last == NUMBER_CHAR_DIGIT) {
                        last = NUMBER_CHAR_DECIMAL;
                        continue;
                    }
                    return PEEKED_NONE;
                default:
                    if (c < '0' || c > '9') {
                        if (!isLiteral(c)) {
                            break outer;
                        }
                        return PEEKED_NONE;
                    }

                    if (last == NUMBER_CHAR_NONE || last == NUMBER_CHAR_SIGN) {
                        value = -(c - '0');
                        last = NUMBER_CHAR_DIGIT;
                    } else if (last == NUMBER_CHAR_DIGIT) {
                        if (value == 0) {
                            return PEEKED_NONE;
                        }

                        long newValue = value * 10 - (c - '0');
                        fitsInLong &= value > MIN_INCOMPLETE_INTEGER || (value == MIN_INCOMPLETE_INTEGER && value > newValue);
                        value = newValue;
                    } else if (last == NUMBER_CHAR_DECIMAL) {
                        last = NUMBER_CHAR_FRACTION_DIGIT;
                    } else if (last == NUMBER_CHAR_EXP_E || last == NUMBER_CHAR_EXP_SIGN) {
                        last = NUMBER_CHAR_EXP_DIGIT;
                    }
            }
        }

        if (last == NUMBER_CHAR_DIGIT && fitsInLong && (value != Long.MIN_VALUE || negative) && (value != 0 || !negative)) {
            peekedLong = negative ? value : -value;
            pos += i;
            return peeked = PEEKED_LONG;
        } else if (last == NUMBER_CHAR_DIGIT || last == NUMBER_CHAR_FRACTION_DIGIT || last == NUMBER_CHAR_EXP_DIGIT) {
            peekedNumberLength = i;
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
            System.arraycopy(buffer, 0, buffer, 0, limit);
        } else {
            limit = 0;
        }

        pos = 0;
        for (int total; (total = in.read(buffer, limit, buffer.length - limit)) != 0; ) {
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
        stack[stackSize++] = scope;
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
}
