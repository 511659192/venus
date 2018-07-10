package com.ym.materials.gson.stream;

import com.ym.materials.gson.internal.Preconditions;
import jdk.nashorn.internal.ir.IfNode;
import jdk.nashorn.internal.runtime.regexp.joni.exception.SyntaxException;

import java.io.*;
import java.lang.annotation.Target;
import java.util.concurrent.Future;

/**
 * Created by ym on 2018/7/8.
 */
public class JsonReader extends JsonScope implements Closeable {

    private boolean lenient;
    private int peeked = PEEKED_NONE;

    private static final int PEEKED_NONE = 0;
    private static final int PEEKED_BEGIN_OBJECT = 1;
    private static final int PEEKED_END_OBJECT = 2;

    private static final int PEEKED_NULL = 7;
    private static final int PEEKED_DOUBLE_QUOTED = 9;
    private static final int PEEKED_UNQUOTED = 10;

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

    private int peekNumber() {
        return 0;
    }

    private int peekKeyWork() {
        return 0;
    }

    private int nextNonWhitespace(boolean throwOnEof) throws IOException {
        for (;;) {
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
        for (;pos < limit || fillBuffer(1); pos++) {
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
        for (;pos <= limit || fillBuffer(length); pos++) {
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
        for (int total; (total = in.read(buffer, limit, buffer.length - limit)) != 0;) {
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
