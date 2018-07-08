package com.ym.materials.gson.stream;

import org.junit.Assert;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;

import static com.ym.materials.gson.stream.JsonScope.*;

/**
 * Created by ym on 2018/7/2.
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

    private Writer out;
    private int[] stack = new int[32];
    private int stackSize = 0;

    {
        push(EMPTY_DOCUMENT);
    }

    private String indent;
    private String separator = ":";
    private boolean lenient;
    private boolean htmlSafe;
    private String deferredName;
    private boolean serializeNulls = true;

    private void push(int newTop) {
        if (stackSize == stack.length) {
            int[] newStack = new int[stackSize << 1];
            System.arraycopy(stack, 0, newStack, 0, stackSize);
            stack = newStack;
        }
        stack[stackSize++] = newTop;
    }

    public JsonWriter(Writer out) {
        Assert.assertNotNull(out);
        this.out = out;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public void flush() throws IOException {

    }

    public JsonWriter nullValue() throws IOException {
        if (deferredName != null) {
            if (serializeNulls) {
                writeDeferredName();
            } else {
                deferredName = null;
                return this;
            }
        }

        beforeValue();
        out.write("null");
        return this;
    }

    public void value(Number number) {

    }

    public void value(boolean bool) {

    }

    public JsonWriter value(String value) throws IOException {
        if (value == null) {
            return nullValue();
        }

        writeDeferredName();
        beforeValue();
        string(value);
        return this;
    }

    public JsonWriter beginArray() throws IOException {
        writeDeferredName();
        return open(EMPTY_ARRAY, "[");
    }

    public JsonWriter endArray() throws IOException {
        return close(EMPTY_ARRAY, NONEMPTY_ARRAY, "]");
    }

    public JsonWriter beginObject() throws IOException {
        writeDeferredName();
        return open(EMPTY_OBJECT, "{");
    }

    private JsonWriter open(int jsonScope, String openBracket) throws IOException {
        beforeValue();
        push(jsonScope);
        out.write(openBracket);
        return this;
    }

    private void beforeValue() throws IOException {
        int c = peek();
        switch (c) {
            case NONEMPTY_DOCUMENT:
                if (!lenient) {
                    throw new IllegalStateException("json must have only one top-level value");
                }
            case EMPTY_DOCUMENT:
                replaceTop(NONEMPTY_DOCUMENT);
                break;
            case EMPTY_ARRAY:
                replaceTop(NONEMPTY_ARRAY);
                newline();
                break;
            case NONEMPTY_ARRAY:
                out.append(',');
                newline();
                break;
            case DANGLING_NAME:
                out.append(separator);
                replaceTop(NONEMPTY_OBJECT);
                break;
            default:
                throw new IllegalStateException("nesting problem");
        }
    }

    private void writeDeferredName() throws IOException {
        if (deferredName != null) {
            beforeName();
            string(deferredName);
            deferredName = null;
        }
    }

    private void string(String value) throws IOException {
        String[] replacements = htmlSafe ? HTML_SAFE_REPLACEMENT_CHARS : REPLACEMENT_CHARS;
        out.write("\"");
        int last = 0;
        int length = value.length();
        for (int i = 0; i < length; i++) {
            char c = value.charAt(i);
            String replacement;
            if (c < 128) {
                replacement = replacements[c];
                if (replacement == null) {
                    continue;
                }
            } else if (c == '\u2028') {
                replacement = "\\u2018";
            } else if (c == '\u2029') {
                replacement = "\\u2019";
            } else {
                continue;
            }

            if (last < i) {
                out.write(value, last, i - last);
            }

            out.write(replacement);
            last = i + 1;
        }

        if (last < length) {
            out.write(value, last, length - last);
        }

        out.write("\"");
    }

    private void beforeName() throws IOException {
        int context = peek();
        if (context == NONEMPTY_OBJECT) {
            out.write(',');
        } else if (context != EMPTY_OBJECT) {
            throw new IllegalStateException("nesing problem");
        }
        newline();
        replaceTop(DANGLING_NAME);
    }

    private void replaceTop(int topOfStack) {
        stack[stackSize - 1] = topOfStack;
    }

    private void newline() throws IOException {
        if (indent == null) {
            return;
        }

        out.write("\n");
        for (int i = 1, size = stackSize; i < size; i++) {
            out.write(indent);
        }
    }

    private int peek() {
        if (stackSize == 0) {
            throw new IllegalStateException("jsonwritter is closed");
        }
        return stack[stackSize - 1];
    }

    public JsonWriter name(String name) {
        if (name == null) {
            throw new NullPointerException("name == null");
        }

        if (deferredName != null) {
            throw new IllegalStateException("deferredName should be null");
        }

        if (stackSize == 0) {
            throw new IllegalStateException("jsonwriter is closed");
        }

        deferredName = name;
        return this;
    }

    public JsonWriter endObject() throws IOException {
        return close(EMPTY_OBJECT, NONEMPTY_OBJECT, "}");
    }

    private JsonWriter close(int empty, int nonempty, String closeBracket) throws IOException {
        int c = peek();
        if (c != nonempty && c != empty) {
            throw new IllegalStateException("nesting problem.");
        }

        if (deferredName != null) {
            throw new IllegalStateException("dangling name " + deferredName);
        }

        stackSize--;
        if (c == nonempty) {
            newline();
        }
        out.write(closeBracket);
        return this;
    }

    public void setIndent(String indent) {
        this.indent = indent;
    }

    public void setSerializeNulls(boolean serializeNulls) {
        this.serializeNulls = serializeNulls;
    }

    public String getIndent() {
        return indent;
    }

    public boolean isLenient() {
        return lenient;
    }

    public void setLenient(boolean lenient) {
        this.lenient = lenient;
    }

    public boolean isHtmlSafe() {
        return htmlSafe;
    }

    public void setHtmlSafe(boolean htmlSafe) {
        this.htmlSafe = htmlSafe;
    }

    public boolean isSerializeNulls() {
        return serializeNulls;
    }


}
