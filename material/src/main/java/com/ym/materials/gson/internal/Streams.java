package com.ym.materials.gson.internal;

import java.io.IOException;
import java.io.Writer;

/**
 * Created by ym on 2018/7/7.
 */
public class Streams {

    private Streams () {
    }

    public static Writer writerForAppendable(Appendable appendable) {
        return appendable instanceof Writer? (Writer) appendable : new AppendableWriter(appendable);
    }

    private static final class AppendableWriter extends Writer {
        private final Appendable appendable;
        private final CurrentWrite currentWrite = new CurrentWrite();
        public AppendableWriter(Appendable appendable) {
            this.appendable = appendable;
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            currentWrite.chars = cbuf;
            appendable.append(currentWrite, off, off + len);
        }

        @Override
        public void write(int c) throws IOException {
            appendable.append(((char) c));
        }

        @Override
        public void flush() throws IOException {

        }

        @Override
        public void close() throws IOException {

        }

        static class CurrentWrite implements CharSequence {
            char[] chars;
            public int length() {
                return chars.length;
            }
            public char charAt(int i) {
                return chars[i];
            }
            public CharSequence subSequence(int start, int end) {
                return new String(chars, start, end - start);
            }
        }
    }


}
