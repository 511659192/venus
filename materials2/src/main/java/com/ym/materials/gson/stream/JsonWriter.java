package com.ym.materials.gson.stream;

import com.ym.materials.gson.internal.Preconditions;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;

import static com.ym.materials.gson.stream.JsonScope.*;

/**
 * 状态机
 * beginObject: EMPTY_DOCUMENT
 * push: NONEMPTY_DOCUMENT -> EMPTY_OBJECT
 * beforeName: DANGLING_NAME
 * beforeValue:  NONEMPTY_OBJECT
 */
public class JsonWriter implements Closeable, Flushable {

    private static final String[] REPLACEMENT_CHARS;
    private static final String[] HTML_SAFE_REPLACEMENT_CHARS;

    static {
        REPLACEMENT_CHARS = new String[128];
        for (int i = 0; i <= 0x1f; i++) {
            REPLACEMENT_CHARS[i] = String.format("\\u%04x", (int) i);
        }
        REPLACEMENT_CHARS['"'] = "\\\"";
        REPLACEMENT_CHARS['\\'] = "\\\\";
        REPLACEMENT_CHARS['\t'] = "\\t";
        REPLACEMENT_CHARS['\b'] = "\\b";
        REPLACEMENT_CHARS['\n'] = "\\n";
        REPLACEMENT_CHARS['\r'] = "\\r";
        REPLACEMENT_CHARS['\f'] = "\\f";
        HTML_SAFE_REPLACEMENT_CHARS = REPLACEMENT_CHARS.clone();
        HTML_SAFE_REPLACEMENT_CHARS['<'] = "\\u003c";
        HTML_SAFE_REPLACEMENT_CHARS['>'] = "\\u003e";
        HTML_SAFE_REPLACEMENT_CHARS['&'] = "\\u0026";
        HTML_SAFE_REPLACEMENT_CHARS['='] = "\\u003d";
        HTML_SAFE_REPLACEMENT_CHARS['\''] = "\\u0027";
    }

    private final Writer writer;
    private boolean serializeNulls;
    private String fieldName;
    private int[] stack = new int[32];
    private int stackSize = 0;
    private boolean htmlSafe;
    private boolean lenient;
    private String separator = ":";

    {
       push(EMPTY_DOCUMENT);
    }

    public JsonWriter(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public void flush() throws IOException {
    }

    public void setSerializeNulls(boolean serializeNulls) {
        this.serializeNulls = serializeNulls;
    }

    private void push(int scope) {
        expandIfNeccisary();
        stack[stackSize++] = scope;
    }

    private void top(int newScope) {
        stack[stackSize - 1] = newScope;
    }

    private void expandIfNeccisary() {
        if (stackSize == stack.length) {
            int[] newStack = new int[stack.length << 1];
            System.arraycopy(stack, 0, newStack, 0, stack.length);
            stack = newStack;
        }
    }

    public void beginObject() throws IOException {
        writeFieldName(); // 内部对象需要先写入fieldName
        open(EMPTY_OBJECT, '{'); // 开始当前对象
    }

    private void open(int scope, char bracket) throws IOException {
        beforeValue();
        push(scope);
        writer.append(bracket);
    }

    private void beforeValue() throws IOException {
        switch (peek()) {
            case EMPTY_DOCUMENT:
                top(NONEMPTY_DOCUMENT);
                break;
            case DANGLING_NAME: // 如果是名称处理 先写入分隔符
                writer.append(separator);
                top(NONEMPTY_OBJECT);
                break;
            default:
                throw new RuntimeException("not accept");
        }
    }

    private void writeFieldName() throws IOException {
        if (fieldName != null) {
            beforeName();
            writeString(fieldName);
            fieldName = null;
        }
    }

    private void beforeName() throws IOException {
        int scope = peek();
        if (scope == NONEMPTY_OBJECT) {
            writer.append(","); // 支持多个名称写入
        } else if (scope != EMPTY_OBJECT) {
            throw new IllegalStateException();
        }
        top(DANGLING_NAME);
    }

    private int peek() {
        return stack[stackSize - 1];
    }

    public void writeString(String text) throws IOException {
        String[] replacements = htmlSafe ? HTML_SAFE_REPLACEMENT_CHARS : REPLACEMENT_CHARS;
        quote('\"');
        int last = 0;
        int length = text.length();
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            String replacement;
            if (c < 128) {
                replacement = replacements[c];
                if (replacement == null) {
                    continue;
                }
            } else if (c == '\u2018') {
                replacement = "\\u2018";
            } else if (c == '\u2019') {
                replacement = "\\u2019";
            } else {
                continue;
            }

            if (last < i) { // 写入替换下标前的值
                writer.write(text, last, i - last);
            }
            writer.write(replacement); // 写入替换后的值
            last = i + 1;
        }

        if (last < length) { // 剩余的值写入
            writer.write(text, last, length - last);
        }
        quote('\"');
    }

    private void quote(char quote) throws IOException {
        writer.append(quote);
    }

    public void endObject() throws IOException {
        close(EMPTY_OBJECT, NONEMPTY_OBJECT, '}');
    }

    private void close(int emptyScope, int nonemptyScope, char bracket) throws IOException {
        int scope = peek();
        if (scope != emptyScope && scope != nonemptyScope) {
            throw new IllegalStateException();
        }

        if (fieldName != null) {
            throw new IllegalStateException();
        }

        stackSize--;
        writer.append(bracket);
    }

    public void writeName(String fieldName) {
        Preconditions.checkNotNull(fieldName);
        if (this.fieldName != null) { // 一次仅仅能写入一个字段名称
            throw new IllegalStateException();
        }
        if (stackSize == 0) {
            throw new IllegalStateException("jsonwriter is closed");
        }
        this.fieldName = fieldName; // 暂存 writeValue时 真正写入
    }

    public void writeValue(String value) throws IOException {
        writeFieldName();
        beforeValue();
        writeString(value);
    }

    public void nullValue() throws IOException {
        if (fieldName != null) {
            if (serializeNulls) {
                writeFieldName();
            } else {
                fieldName = null;
                return;
            }
        }

        beforeValue();
        writeNull();
    }

    private void writeNull() throws IOException {
        writer.write("null");
    }

}
