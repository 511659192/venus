package com.ym.materials.gson.stream;

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
    private static final char[] NON_EXECUTE_PREFIX = ")]}'\n".toCharArray();
    private static final long MIN_INCOMPLETE_INTEGER = Long.MIN_VALUE / 10;
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
    private long peekedLong;
    private int peekedNumberLength;
    private String peekedString;

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

    public JsonToken peek() throws IOException {
        int p = peeked;
        if (p == PEEKED_NONE) {
            p = doPeek();
        }

        switch (p) {
            case PEEKED_BEGIN_OBJECT:
                return JsonToken.BEFIN_OBJECT;
            case PEEKED_END_OBJECT:
                return JsonToken.END_OBJECT;
            case PEEKED_BEGIN_ARRAY:
                return JsonToken.BEGIN_ARRAY;
            case PEEKED_END_ARRAY:
                return JsonToken.END_ARRAY;
            case PEEKED_DOUBLE_QUOTED_NAME:
            case PEEKED_SINGLE_QUOTED_NAME:
            case PEEKED_UNQUOTED_NAME:
                return JsonToken.NAME;
            case PEEKED_TRUE:
            case PEEKED_FALSE:
                return JsonToken.BOOLEAN;
            case PEEKED_NULL:
                return JsonToken.NULL;
            case PEEKED_SINGLE_QUOTED:
            case PEEKED_DOUBLE_QUOTED:
            case PEEKED_UNQUOTED:
            case PEEKED_BUFFERED:
                return JsonToken.STRING;
            case PEEKED_LONG:
            case PEEKED_NUMBER:
                return JsonToken.NUMBER;
            case PEEKED_EOF:
                return JsonToken.END_DOCUMENT;
            default:
                throw new AssertionError();
        }
    }

    public String nextString() throws IOException {
        int p = peeked;
        if (p == PEEKED_NULL) {
            p = doPeek();
        }

        String result = null;
        if (p == PEEKED_UNQUOTED) {
            result = nextUnquotedValue();
        } else if (p == PEEKED_SINGLE_QUOTED) {
            result = nextQuotedVaule('\'');
        } else if (p == PEEKED_DOUBLE_QUOTED) {
            result = nextQuotedVaule('\"');
        } else if (p == PEEKED_BUFFERED) {
            result = peekedString;
            peekedString = null;
        } else if (p == PEEKED_LONG) {
            result = Long.toString(peekedLong);
        } else if (p == PEEKED_NUMBER) {
            result = new String(buffer, pos, peekedNumberLength);
            pos += peekedNumberLength;
        } else {
            throw new IllegalStateException("expected a string but was " + peek());
        }

        peeked = PEEKED_NONE;
        pathIndices[stackSize - 1]++;
        return result;
    }

    private String nextUnquotedValue() throws IOException {
        StringBuilder builder = null;
        int i = 0;
        findNonLiteralCharacter:
        for (; ; ) {
            for (; pos + i < limit; i++) {
                switch (buffer[pos + i]) {
                    case '/':
                    case '\\':
                    case ';':
                    case '#':
                    case '=':
                        checkLenient();
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
                        break findNonLiteralCharacter;
                }
            }

            if (i < buffer.length) {
                if (fillBuffer(i + 1)) {
                    continue;
                } else {
                    break;
                }
            }

            if (builder == null) {
                builder = new StringBuilder(Math.max(i, 16));
            }
            builder.append(buffer, pos, i);
            pos += i;
            i = 0;
            if (!fillBuffer(1)) {
                break;
            }
        }
        String result = (null == builder) ? new String(buffer, pos, i) : builder.append(buffer, pos, i).toString();
        pos += i;
        return result;
    }

    private int doPeek() throws IOException {
        int peekStack = stack[stackSize - 1];
        if (peekStack == JsonScope.EMPTY_ARRAY) { // 空数组
            stack[stackSize - 1] = JsonScope.NONEMPTY_ARRAY;
        } else if (peekStack == JsonScope.NONEMPTY_ARRAY) { // 非空数组
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
                case '"':
                    return peeked = PEEKED_DOUBLE_QUOTED_NAME;
                case '\'':
                    return peeked = PEEKED_SINGLE_QUOTED_NAME;
                case '}':
                    return peeked = PEEKED_END_OBJECT;
                default:
                    checkLenient();
                    pos--;
                    if (isLiteral((char) c)) {
                        return peeked = PEEKED_UNQUOTED_NAME; // 未被包裹的普通字符串
                    } else {
                        throw new RuntimeException("expected name");
                    }
            }
        } else if (peekStack == JsonScope.DANGLING_NAME) {
            stack[stackSize - 1] = JsonScope.NONEMPTY_OBJECT;
            int c = nextNonWhitespace(true);
            switch (c) {
                case ':':
                    break;
                case '=':
                    checkLenient();
                    if ((pos < limit || fillBuffer(1)) && buffer[pos] == '>') {
                        pos++;
                    }
                    break;
                default:
                    throw new RuntimeException("expected :");
            }
        } else if (peekStack == JsonScope.EMPTY_DOCUMENT) {
            if (lenient) {
                consumeNonExecutePrefix();
            }
            stack[stackSize - 1] = JsonScope.NONEMPTY_DOCUMENT;
        } else if (peekStack == JsonScope.NONEMPTY_DOCUMENT) {
            int c = nextNonWhitespace(false);
            if (c == -1) {
                return peeked = PEEKED_EOF;
            } else {
                checkLenient();
                pos--;
            }
        } else if (peekStack == JsonScope.CLOSED) {
            throw new IllegalStateException("jsonreader is closed");
        }

        int c = nextNonWhitespace(true);
        switch (c) {
            case ']':
                if (peekStack == JsonScope.EMPTY_ARRAY) {
                    return peeked = PEEKED_END_ARRAY;
                }
            case ';':
            case ',':
                if (peekStack == JsonScope.EMPTY_ARRAY || peekStack == JsonScope.NONEMPTY_ARRAY) {
                    checkLenient();
                    pos--;
                    return peeked = PEEKED_NULL;
                } else {
                    throw new RuntimeException("unexpected value");
                }
            case '\'':
                checkLenient();
                return peeked = PEEKED_SINGLE_QUOTED;
            case '"':
                return peeked = PEEKED_DOUBLE_QUOTED;
            case '[':
                return peeked = PEEKED_BEGIN_ARRAY;
            case '{':
                return peeked = PEEKED_BEGIN_OBJECT;
            default:
                pos--;
        }

        int result = peekKeyWord();
        if (result != PEEKED_NONE) {
            return result;
        }

        result = peekNumber();
        if (result != PEEKED_NONE) {
            return result;
        }

        if (!isLiteral(buffer[pos])) {
            throw new RuntimeException("unexpected value");
        }

        checkLenient();
        return peeked = PEEKED_UNQUOTED;
    }

    private int peekNumber() throws IOException {
        char[] buffer = this.buffer;
        int p = pos;
        int l = limit;
        long value = 0;
        boolean negative = false;
        boolean fitsInLong = true;
        int last = NUMBER_CHAR_NONE;
        int i = 0;
        charactersOfNumber:
        for (; true; i++) {
            if (p + i == l) {
                if (i == buffer.length) {
                    return PEEKED_NONE;
                }
                if (!fillBuffer(i + 1)) {
                    break;
                }
            }
            char c = buffer[p + i];
            switch (c) {
                case '-':
                    if (last == NUMBER_CHAR_NONE) {
                        negative = true;
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
                            break charactersOfNumber;
                        }
                        return PEEKED_NONE;
                    }
                    if (last == NUMBER_CHAR_SIGN || last == NUMBER_CHAR_NONE) {
                        value = -(c - '0');
                        last = NUMBER_CHAR_DIGIT;
                    } else if (last == NUMBER_CHAR_DIGIT) {
                        if (value == 0) {
                            return PEEKED_NONE;
                        }
                        long newValue = value * 10 - (c - '0');
                        fitsInLong &= value > MIN_INCOMPLETE_INTEGER || (value == MIN_INCOMPLETE_INTEGER && newValue < value);
                        value = newValue;
                    } else if (last == NUMBER_CHAR_DECIMAL) {
                        last = NUMBER_CHAR_FRACTION_DIGIT;
                    } else if (last == NUMBER_CHAR_EXP_E || last == NUMBER_CHAR_EXP_SIGN) {
                        last = NUMBER_CHAR_EXP_SIGN;
                    }
            }
        }

        if (last == NUMBER_CHAR_DIGIT && fitsInLong && (value != Long.MIN_VALUE || negative) && (value != 0 || !negative)) {
            peekedLong = negative ? value : -value;
            pos += i;
            return peeked = PEEKED_LONG;
        } else if (last == NUMBER_CHAR_DIGIT || last == NUMBER_CHAR_FRACTION_DIGIT || last == NUMBER_CHAR_EXP_SIGN) {
            peekedNumberLength = i;
            return peeked = PEEKED_NUMBER;
        } else {
            return PEEKED_NONE;
        }
    }

    private int peekKeyWord() throws IOException {
        char c = buffer[pos];
        String keyword;
        String keywordUpper;
        int peeking;
        if (c == 't' || c == 'T') {
            keyword = "true";
            keywordUpper = "TRUE";
            peeking = PEEKED_TRUE;
        } else if (c == 'f' || c == 'F') {
            keyword = "false";
            keywordUpper = "FALSE";
            peeking = PEEKED_FALSE;
        } else if (c == 'n' || c == 'N') {
            keyword = "null";
            keywordUpper = "NULL";
            peeking = PEEKED_NULL;
        } else {
            return PEEKED_NONE;
        }

        int length = keyword.length();
        for (int i = 0; i < length; i++) {
            if (pos + i >= limit && !fillBuffer(i + 1)) {
                return PEEKED_NONE;
            }
            c = buffer[pos + i];
            if (c != keyword.charAt(i) && c != keywordUpper.charAt(i)) {
                return PEEKED_NONE;
            }
        }

        if ((pos + length < limit || fillBuffer(length + 1)) && isLiteral(buffer[pos + length])) {
            return PEEKED_NONE;
        }

        pos += length;
        return peeked = peeking;
    }

    private void consumeNonExecutePrefix() throws IOException {
        nextNonWhitespace(true);
        pos--;

        if (pos + NON_EXECUTE_PREFIX.length > limit && !fillBuffer(NON_EXECUTE_PREFIX.length)) {
            return;
        }

        for (int i = 0; i < NON_EXECUTE_PREFIX.length; i++) {
            if (buffer[pos + i] != NON_EXECUTE_PREFIX[i]) {
                return;
            }
        }
        pos += NON_EXECUTE_PREFIX.length;
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

    private int nextNonWhitespace(boolean throwOnEof) throws IOException {
        char[] buffer = this.buffer;
        int p = this.pos;
        int l = this.limit;
        for (; ; ) {
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

    /**
     * 从reader中读取数据
     *
     * @param minimum
     * @return
     * @throws IOException
     */
    private boolean fillBuffer(int minimum) throws IOException {
        char[] buffer = this.buffer;
        lineStart -= pos;
        if (limit != pos) {
            limit -= pos; // 处理前面为空的情况
            System.arraycopy(buffer, pos, buffer, 0, limit);
        } else {
            limit = 0; // 起始点
        }

        pos = 0;
        int total;
        while ((total = in.read(buffer, limit, buffer.length - limit)) != -1) {
            limit += total;
            if (lineNumber == 0 && lineStart == 0 && limit > 0 && buffer[0] == '\ufeff') {
                pos++;
                lineStart++;
                minimum++;
            }

            if (limit >= minimum) {
                return true;
            }
        }
        return false;
    }

    public boolean nextBoolean() {
        return false;
    }

    public void nextNull() throws IOException {
        int p = peeked;
        if (p == PEEKED_NONE) {
            p = doPeek();
        }
        if (p == PEEKED_NULL) {
            peeked = PEEKED_NONE;
            pathIndices[stackSize - 1]++;
        } else {
            throw new IllegalStateException("expect null but was " + peek());
        }
    }

    public void beginArray() throws IOException {
        int p = peeked;
        if (p == PEEKED_NONE) {
            p = doPeek();
        }
        if (p == PEEKED_BEGIN_ARRAY) {
            push(JsonScope.EMPTY_ARRAY);
            pathIndices[stackSize - 1] = 0;
            peeked = PEEKED_NONE;
        } else {
            throw new IllegalStateException("expected begin array but was " + peek());
        }
    }

    public boolean hasNext() throws IOException {
        int p = peeked;
        if (p == PEEKED_NONE) {
            p = doPeek();
        }
        return p != PEEKED_END_OBJECT && p != PEEKED_END_ARRAY;
    }

    public void endArray() throws IOException {
        int p = peeked;
        if (p == PEEKED_NONE) {
            p = doPeek();
        }
        if (p == PEEKED_END_ARRAY) {
            stackSize--;
            pathIndices[stackSize - 1]++;
            peeked = PEEKED_NONE;
        } else {
            throw new IllegalStateException("expect end array but was " + peek());
        }
    }

    public void beginObject() throws IOException {
        int p = peeked;
        if (p == PEEKED_NONE) {
            p = doPeek();
        }

        if (p == PEEKED_BEGIN_OBJECT) {
            push(JsonScope.EMPTY_OBJECT);
            peeked = PEEKED_NONE;
        } else {
            throw new IllegalStateException("expected begin object but was" + peek());
        }
    }

    private void push(int newTop) {
        if (stackSize == stack.length) {
            int[] newStack = new int[stackSize << 1];
            int[] newPathIndices = new int[stackSize << 1];
            String[] newPathNames = new String[stackSize << 1];
            System.arraycopy(stack, 0, newStack, 0, stackSize);
            System.arraycopy(pathIndices, 0, newPathIndices, 0, stackSize);
            System.arraycopy(pathNames, 0, newPathNames, 0, stackSize);
            stack = newStack;
            pathIndices = newPathIndices;
            pathNames = newPathNames;
        }
        stack[stackSize++] = newTop;
    }

    public String nextName() throws IOException {
        int p = peeked;
        if (p == PEEKED_NONE) {
            p = doPeek();
        }

        String result;
        if (p == PEEKED_UNQUOTED_NAME) {
            result = nextUnquotedValue();
        } else if (p == PEEKED_SINGLE_QUOTED_NAME) {
            result = nextQuotedVaule('\'');
        } else if (p == PEEKED_DOUBLE_QUOTED_NAME) {
            result = nextQuotedVaule('\"');
        } else {
            throw new IllegalStateException("expected a name but was " + peek());
        }

        peeked = PEEKED_NONE;
        pathNames[stackSize - 1] = result;
        return result;
    }

    private String nextQuotedVaule(char quote) throws IOException {
        char[] buffer = this.buffer;
        StringBuilder builder = null;
        for (;;) {
            int p = pos;
            int l = limit;
            int start = p;
            while (p < l) {
                int c = buffer[p++];
                if (c == quote) {
                    pos = p;
                    int len = p - start - 1;
                    if (builder == null) {
                        return new String(buffer, start, len);
                    } else {
                        builder.append(buffer, start, len);
                        return builder.toString();
                    }
                } else if (c == '\\') {
                    pos = p;
                    int len = p - start - 1;
                    if (builder == null) {
                        int estimatedLength = (len + 1) * 2;
                        builder = new StringBuilder(Math.max(estimatedLength, 16));
                    }
                    builder.append(buffer, start, len);
                    builder.append(readEscapeCharacter());
                    p = pos;
                    l = limit;
                    start = p;
                } else if (c == '\n') {
                    lineNumber++;
                    lineStart = p;
                }
            }

            if (builder == null) {
                int estimatedLength = (p - start) * 2;
                builder = new StringBuilder(Math.max(estimatedLength, 16));
            }
            builder.append(buffer, start, p - start);
            pos = p;
            if (!fillBuffer(1)) {
                throw new RuntimeException("unterminated string");
            }
        }
    }

    private char readEscapeCharacter() throws IOException {
        if (pos == limit && !fillBuffer(1)) {
            throw new RuntimeException("unterminated escape sequence");
        }

        char escaped = buffer[pos++];
        switch (escaped) {
            case 'u':
                if (pos + 4 > limit && !fillBuffer(4)) {
                    throw new RuntimeException("unterminated escape sequence");
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
                        throw new NumberFormatException("\\u" + new String(buffer, pos, 4));
                    }
                }
                pos += 4;
                return result;
            case 't':
                return '\t';
            case 'b':
                return '\b';
            case 'n':
                return '\b';
            case 'r':
                return '\r';
            case 'f':
                return '\f';
            case '\n':
                lineNumber++;
                lineStart = pos;
            case '\'':
            case '\"':
            case '\\':
            case '/':
                return escaped;
            default:
                throw new RuntimeException("invalid escape sequence");
        }
    }

    public void endObject() throws IOException {
        int p = peeked;
        if (p == PEEKED_NONE) {
            p = doPeek();
        }

        if (p == PEEKED_END_OBJECT) {
            stackSize--;
            pathNames[stackSize] = null;
            pathIndices[stackSize - 1]++;
            peeked = PEEKED_NONE;
        } else {
            throw new IllegalStateException("expected end object but was " + peek());
        }
    }

    public double nextDouble() {
        return 0;
    }

    public int nextInt() throws IOException {
        int p = peeked;
        if (p == PEEKED_NONE) {
            p = doPeek();
        }
        int result;
        if (p == PEEKED_LONG) {
            result = (int) peekedLong;
            if (peekedLong != result) {
                throw new NumberFormatException("expect an int but was " + peekedLong);
            }
            peeked = PEEKED_NONE;
            pathIndices[stackSize - 1]++;
            return result;
        }

        if (p == PEEKED_NUMBER) {
            peekedString = new String(buffer, pos, peekedNumberLength);
            pos += peekedNumberLength;
        } else if (p == PEEKED_SINGLE_QUOTED || p == PEEKED_DOUBLE_QUOTED || p == PEEKED_UNQUOTED) {
            if (p == PEEKED_UNQUOTED) {
                peekedString = nextUnquotedValue();
            } else {
                peekedString = nextQuotedVaule(p == PEEKED_SINGLE_QUOTED ? '\'' : '\"');
            }

            try {
                result = Integer.parseInt(peekedString);
                peeked = PEEKED_NONE;
                pathIndices[stackSize - 1]++;
                return result;
            } catch (NumberFormatException ignored) {

            }
        } else {
            throw new IllegalStateException("Expected an int but was " + peek() + locationString());
        }

        peeked = PEEKED_BUFFERED;
        double asDouble = Double.parseDouble(peekedString);
        result = (int) asDouble;
        if (result != asDouble) { // Make sure no precision was lost casting to 'int'.
            throw new NumberFormatException("Expected an int but was " + peekedString + locationString());
        }

        peekedString = null;
        peeked = PEEKED_NONE;
        pathIndices[stackSize - 1]++;
        return result;
    }

    public long nextLong() {
        return 0;
    }

    public void setLenient(boolean lenient) {
        this.lenient = lenient;
    }

    public boolean isLenient() {
        return lenient;
    }
}
