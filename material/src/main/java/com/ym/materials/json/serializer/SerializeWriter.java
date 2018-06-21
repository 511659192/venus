package com.ym.materials.json.serializer;

import com.alibaba.fastjson.JSONException;
import com.ym.materials.json.util.IOUtils;

import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.util.IllegalFormatFlagsException;

import static com.ym.materials.json.serializer.SerializerFeature.*;
import static com.ym.materials.json.util.IOUtils.*;

public class SerializeWriter extends Writer {

    private final static ThreadLocal<char[]> bufLocal = new ThreadLocal<char[]>();

    protected char buf[];

    private Writer writer;
    private int features;

    private int count;
    private boolean quoteFieldNames;
    private boolean useSingleQuotes;
    private boolean sortField;
    private boolean disableCircularReferenceDetect;
    private boolean beanToArray;
    private boolean writeNonStringValueAsString;
    private boolean notWriteDefaultValue;
    private boolean writeEnumUsingName;
    private boolean writeEnumUsingToString;
    private boolean writeDirect;
    private char keySeperator;
    private boolean browserSecure;
    private long sepcialBits;

    private final static char singleQuete = '\'';
    private final static char doubleQuete = '"';
    private final static char colon = ':';
    private final static char backSlash = '\\';
    private final static char forwardSlash = '/';

    protected int maxBufSize = -1;

    public SerializeWriter(Writer writer, int defaultFeatures, SerializerFeature... features) {
        this.writer = writer;
        buf = bufLocal.get();
        if (writer != null) {
            bufLocal.set(null);
        } else {
            buf = new char[2048];
        }

        int featuresValue = defaultFeatures;
        for (SerializerFeature feature : features) {
            featuresValue |= feature.getMask();
        }
        this.features = featuresValue;
    }

    @Override
    public void write(char[] cbuf, int off, int len) {

    }

    @Override
    public void flush() {
        if (writer == null) {
            return;
        }

        try {
            writer.write(buf, 0, count);
            writer.flush();
        } catch (IOException e) {
            throw new JSONException(e.getMessage(), e);
        }
        count = 0;

    }

    @Override
    public void close() {
        if (writer != null && count > 0) {
            flush();
        }

        if (buf.length <= 1024 * 128) {
            bufLocal.set(buf);
        }
        this.buf = null;
    }

    public void config(SerializerFeature feature, boolean state) {
        if (!state) {
            features |= feature.getMask();
            // 由于枚举序列化特性WriteEnumUsingToString和WriteEnumUsingName不能共存，需要检查
            if (feature == WriteEnumUsingToString) {
                features &= ~WriteEnumUsingName.getMask();
            } else if (feature == WriteEnumUsingName) {
                features &= ~WriteEnumUsingToString.getMask();
            }
        } else {
            features &= ~feature.getMask();
        }

        computeFeatures();
    }

    public void write(int c) {
        int newCount = count + 1;
        if (newCount > buf.length) {
            if (writer != null) {
                expandCapacity(newCount);
            } else {
                flush(); // 什么时间添加的呢
                newCount = 1;
            }
        }
        buf[count] = (char) c;
        count = newCount;
    }

    private void expandCapacity(int minimumCapacity) {
        if (maxBufSize != -1 && minimumCapacity >= maxBufSize) {
            throw new JSONException("serialize exceeded MAX_OUTPUT_LENGTH=" + maxBufSize + ", minimumCapacity=" + minimumCapacity);
        }

        int newCapacity = buf.length + (buf.length >> 1) + 1; // 1.5倍 倍增
        if (newCapacity < minimumCapacity) {
            newCapacity = minimumCapacity;
        }

        char[] newValue = new char[newCapacity];
        System.arraycopy(buf, 0, newValue, 0, count);
        buf = newValue;
    }

    final static int nonDirectFeatures = 0 //
            | UseSingleQuotes.mask //
            | BrowserCompatible.mask //
            | PrettyFormat.mask //
            | WriteEnumUsingToString.mask
            | WriteNonStringValueAsString.mask
            | WriteSlashAsSpecial.mask
            | IgnoreErrorGetter.mask
            | WriteClassName.mask
            | NotWriteDefaultValue.mask
            ;

    private void computeFeatures() {
        quoteFieldNames = (this.features & QuoteFieldNames.mask) != 0;
        useSingleQuotes = (this.features & UseSingleQuotes.mask) != 0;
        sortField = (this.features & SortField.mask) != 0;
        disableCircularReferenceDetect = (this.features & DisableCircularReferenceDetect.mask) != 0;
        beanToArray = (this.features & BeanToArray.mask) != 0;
        writeNonStringValueAsString = (this.features & WriteNonStringValueAsString.mask) != 0;
        notWriteDefaultValue = (this.features & NotWriteDefaultValue.mask) != 0;
        writeEnumUsingName = (this.features & WriteEnumUsingName.mask) != 0;
        writeEnumUsingToString = (this.features & WriteEnumUsingToString.mask) != 0;
        writeDirect = quoteFieldNames && (this.features & nonDirectFeatures) == 0 && (beanToArray || writeEnumUsingName);
        keySeperator = useSingleQuotes ? '\'' : '"';
        browserSecure = (this.features & BrowserSecure.mask) != 0;
        // 10011111111111111111111111111111111
        // 100000000000010011111111111111111111111111111111
        // 101000000000000000000110000010011111111111111111111111111111111
        final long S0 = 0x4FFFFFFFFL, S1 = 0x8004FFFFFFFFL, S2 = 0x50000304ffffffffL;
        sepcialBits = browserSecure ? S2 : (this.features & WriteSlashAsSpecial.mask) != 0 ? S1 : S0;
    }

    public static void main(String[] args) {
        final long S0 = 0x4FFFFFFFFL, S1 = 0x8004FFFFFFFFL, S2 = 0x50000304ffffffffL;
        System.out.println(S0);
        System.out.println(S1);
        System.out.println(S2);
        System.out.println(Long.toBinaryString(S0));
        System.out.println(Long.toBinaryString(S1));
        System.out.println(Long.toBinaryString(S2));
    }

    public void writeFieldName(String key) {
        writeFieldName(key, false);
    }

    private void writeFieldName(String key, boolean checkSpecial) {
        if (key == null) {
            write("null:");
            return;
        }

        if (useSingleQuotes) {
            if (quoteFieldNames) {
                writeStringWithSingleQuote(key);
                write(':');
            } else {
                writeKeyWithSingleQuoteIfHasSepcial(key);
            }
        } else {
            if (quoteFieldNames) {
//                writeStringWithDoubleQuote(key, ':');
            } else {

            }
        }
    }

    private void writeKeyWithSingleQuoteIfHasSepcial(String text) {
        byte[] specicalFlags_singleQuotes = IOUtils.specicalFlags_singleQuotes;
        int len = text.length();
        // count = 3 len = 3 newCount = 7
        int newCount = count + len + 1;
        if (newCount > buf.length) {
            if (writer == null) {
                expandCapacity(newCount);
            } else {
                if (len == 0) {
                    writeSignleQuete();
                    writeSignleQuete();
                    writeColon();
                    return;
                }

                boolean hasSpecial = false;
                for (int i = 0; i < len; i++) {
                    char ch = text.charAt(i);
                    if (ch < specicalFlags_singleQuotes.length && specicalFlags_singleQuotes[ch] != 0) {
                        hasSpecial = true;
                        break;
                    }
                }

                if (hasSpecial) {
                    writeSignleQuete();
                }

                for (int i = 0; i < len; i++) {
                    char ch = text.charAt(i);
                    if (ch < specicalFlags_singleQuotes.length && specicalFlags_singleQuotes[ch] != 0) {
                        writeBackSlash();
                        write(replaceChars[(int) ch]);
                    } else {
                        write(ch);
                    }
                }

                if (hasSpecial) {
                    writeSignleQuete();
                }
                writeColon();
                return;
            }
        }

        if (len == 0) {
            newCount = count + 3;
            expandCapacityIfNecessary(newCount);
            buf[count++] = singleQuete;
            buf[count++] = singleQuete;
            buf[count++] = colon;
            return;
        }

        int start = count; // start = 3
        int end = start + len;  // end = 6
        text.getChars(0, len, buf, start);
        count = newCount; // count = 7
        boolean hasSpecial = false;
        for (int i = start; i < end; i++) {
            char ch = buf[i];
            if (ch < specicalFlags_singleQuotes.length && specicalFlags_singleQuotes[ch] != 0) {
                if (!hasSpecial) {
                    newCount += 3; // newCount = count = 10
                    expandCapacity(newCount);
                    count = newCount;

                    // i = start = 3 end = 6
                    // 0,1,2,3,4,5
                    System.arraycopy(buf, i + 1, buf, i + 3, end - i - 1);
                    // System.arraycopy(buf, 4, buf, 6, 2);
                    // 0,1,2,3, , ,4,5
                    System.arraycopy(buf, 0, buf, 1, i);
                    //  ,0,1,2, , ,4,5
                    buf[start] = singleQuete;
                    //  ,0,1,', , ,4,5
                    buf[++i] = backSlash;
                    //  ,0,1,',/, ,4,5
                    buf[++i] = replaceChars[(int) ch];
                    //  ,0,1,',/,c,4,5
                    end += 2;
                    buf[count - 2] = singleQuete;
                    hasSpecial = true;
                } else {
                    newCount++;
                    expandCapacityIfNecessary(newCount);
                    count = newCount;
                    System.arraycopy(buf, i + 1, buf, i + 2, end - i);
                    buf[i] = backSlash;
                    buf[++i] = replaceChars[(int) ch];
                    end++;
                }
            }
        }
        buf[newCount - 1] = colon;
    }

    private void expandCapacityIfNecessary(int newCount) {
        if (newCount > buf.length) {
            expandCapacity(newCount);
        }
    }

    private void writeColon() {
        write(':');
    }

    private void writeBackSlash() {
        write('\\');
    }

    private void writeSignleQuete() {
        write('\'');
    }

    private void writeStringWithSingleQuote(String text) {
        if (text == null) {
            int newCount = count + 4;
            expandCapacityIfNecessary(newCount);
            "null".getChars(0, 4, buf, count);
            count = newCount;
            return;
        }

        int len = text.length();
        int newCount = count + len + 2;
        if (newCount > buf.length) {
            if (writer == null) {
                expandCapacity(newCount);
            } else {
                write('\'');
                for (int i = 0; i < text.length(); i++) {
                    char ch = text.charAt(i);
                    if (ch <= 13 || ch == '\\' || ch == '\'' || (ch == '/' && isEnabled(WriteSlashAsSpecial))) {
                        write('\\');
                        write(replaceChars[(int) ch]);
                    } else {
                        write(ch);
                    }
                }
                write('\'');
                return;
            }
        }

        int start = count + 1;
        int end = start + len;
        buf[count] = '\'';
        text.getChars(0, len, buf, start);
        count = newCount;

        // TODO: 为什么不使用递归呢
        int specialCount = 0;
        int lastSpecialIndex = -1;
        char lastSpecial = '\0';
        for (int i = start; i < end; i++) {
            char ch = buf[i];
            if (ch <= 13 || ch == '\\' || ch == '\'' || (ch == '/' && isEnabled(WriteSlashAsSpecial))) {
                specialCount++;
                lastSpecialIndex = i;
                lastSpecial = ch;
            }
        }

        newCount += specialCount;
        if (newCount > buf.length) {
            expandCapacity(newCount);
        }

        if (specialCount == 1) {
            System.arraycopy(buf, lastSpecialIndex + 1, buf, lastSpecialIndex + 2, end - lastSpecialIndex - 1);
            buf[lastSpecialIndex] = '\\';
            buf[++lastSpecialIndex] = replaceChars[(int) lastSpecial];
        } else if (specialCount > 1) {
            System.arraycopy(buf, lastSpecialIndex + 1, buf, lastSpecialIndex + 2, end - lastSpecialIndex - 1);
            buf[lastSpecialIndex] = '\\';
            buf[++lastSpecialIndex] = replaceChars[(int) lastSpecial];
            end++;
            for (int i = lastSpecialIndex - 2; i >= start ; i--) {
                char ch = buf[i];
                if (ch <= 13 || ch == '\\' || ch == '\'' || (ch == '/' && isEnabled(WriteSlashAsSpecial))) {
                    System.arraycopy(buf, i + 1, buf, i + 2, end - i - 1);
                    buf[i] = '\\';
                    buf[i + 1] = replaceChars[(int) ch];
                    end++;
                }
            }
        }
        buf[count - 1] = '\'';
    }

    private boolean isEnabled(SerializerFeature feature) {
        return (this.features & feature.getMask()) != 0;
    }

    public void write(String text) {
        if (text == null) {
            writeNull();
            return;
        }
        write(text, 0, text.length());
    }

    public void writeNull() {
        write("null");
    }

    public void writeNull(SerializerFeature feature) {
        writeNull(0, feature.mask);
    }

    private void writeNull(int beanFeatures, int feature) {
        if ((beanFeatures & feature) == 0 && (this.features & feature) == 0) {
            writeNull();
            return;
        }

        if (feature == WriteNullListAsEmpty.mask) {
            write("[]");
        } else if (feature == WriteNullStringAsEmpty.mask) {
            write("");
        } else if (feature == WriteNullBooleanAsFalse.mask) {
            write("false");
        } else if (feature == WriteNullNumberAsZero.mask) {
            write('0');
        } else {
            writeNull();
        }
    }

    public void write(String text, int off, int len) {
        int newCount = count + len;
        if (newCount > buf.length) {
            if (writer == null) {
                expandCapacity(newCount);
            } else {
                do {
                    int rest = buf.length - count; // 剩余容量
                    text.getChars(off, off + rest, buf, count); // 填充
                    count = buf.length;
                    flush();
                    len -= rest; // 剩余字节数
                    off += rest; // 开始下标值
                } while (len > buf.length);
            }
        }
        text.getChars(off, off + len, buf, count);
        count = newCount;
    }

    public void writeString(String text) {
        if (useSingleQuotes) {
            writeStringWithSingleQuote(text);
        } else {
            writeStringWithDoubleQuote(text, (char) 0);
        }
    }

    private void writeStringWithDoubleQuote(String text, char seperator) {
        if (text == null) {
            writeNull();
            if (seperator != 0) {
                write(seperator);
            }
            return;
        }

        int len = text.length();
        int newCount = count + len + 2;
        if (seperator != 0) {
            newCount++;
        }

        if (newCount > buf.length) {
            if (writer == null) {
                expandCapacity(newCount);
            } else {
                writeDoubleQuete();
                for (int i = 0; i < text.length(); i++) {
                    char ch = text.charAt(i);
                    if (isEnabled(BrowserSecure)) {
                        if (ch == '(' || ch == ')' || ch == '<' || ch == '>') {
                            writeUnicode(ch);
                            continue;
                        }
                    }

                    if (isEnabled(BrowserCompatible)) {
                        if (ch == '\b' || ch == '\f' || ch == '\n' || ch == '\r' || ch == '\t' || ch == '"' || ch == '/' || ch == '\\') {
                            writeReplaceChar(ch);
                            continue;
                        }
                        if (ch < 32) {
                            writeAscii(ch);
                            continue;
                        }

                        if (ch >= 127) {
                            writeUnicode(ch);
                            continue;
                        }
                    } else {
                        if (ch < specicalFlags_doubleQuotes.length && specicalFlags_doubleQuotes[ch] != 0 || (ch == forwardSlash && isEnabled(WriteSlashAsSpecial))) {
                            if (specicalFlags_doubleQuotes[ch] == 4) {
                                writeUnicode(ch);
                            } else {
                                writeReplaceChar(ch);
                            }
                            continue;
                        }
                    }
                    write(ch);
                }
                writeDoubleQuete();
                writeSeperatorIfNecessary(seperator);
            }
            return;
        }

        int start = count + 1;
        int end = start + len;
        buf[count] = doubleQuete;
        text.getChars(0, len, buf, start);
        count = newCount;
        if (isEnabled(BrowserCompatible)) {
            int lastSpecialIndex = -1;
            for (int i = start; i < end; i++) {
                char ch = buf[i];
                if (ch == doubleQuete || ch == backSlash || ch == forwardSlash || ch == '\b' || ch == '\f' || ch == '\n' || ch == '\r' || ch == '\t') {
                    lastSpecialIndex = i;
                    newCount++;
                    continue;
                }

                if (ch < 32 || ch >= 127) {
                    lastSpecialIndex = i;
                    newCount += 5;
                    continue;
                }
            }
            expandCapacity(newCount);
            count = newCount;
            for (int i = lastSpecialIndex; i >= start ; i--) {
                char ch = buf[i];
                if (ch == '\b' || ch == '\f' || ch == '\n' || ch == '\r' || ch == '\t') {
                    System.arraycopy(buf, i + 1, buf, i + 2, end - i - 1);
                    writeReplaceChar(ch);
                    end++;
                    continue;
                }

                if (ch == doubleQuete || ch == backSlash || ch == forwardSlash) {
                    System.arraycopy(buf, i + 1, buf, i + 2, end - i - 1);
                    buf[i] = backSlash;
                    buf[i + 1] = ch;
                    end++;
                    continue;
                }

                if (ch < 32) {
                    System.arraycopy(buf, i + 1, buf, i + 6, end - i - 1);
                    writeAscii(ch);
                    end += 5;
                    continue;
                }

                if (ch >= 127) {
                    System.arraycopy(buf, i + 1, buf, i + 6, end - i - 1);
                    writeUnicode(ch);
                    end += 5;
                }
            }

            if (seperator != 0) {
                buf[count - 2] = doubleQuete;
                buf[count - 1] = seperator;
            } else {
                buf[count - 1] = doubleQuete;
            }
            return;
        }

        int specialCount = 0;
        int lastSpecialIndex = -1;
        int firstSpecialIndex = -1;
        char lastSpecial = '\0';

        for (int i = start; i < end; i++) {
            char ch = buf[i];
            if (ch >= ']') {
                if (ch >= 0x7F && (ch == '\u2028' || ch == '\u2029' || ch < 0xA0)) {
                    firstSpecialIndex = firstSpecialIndex == -1 ? i : firstSpecialIndex;
                    specialCount++;
                    lastSpecialIndex = i;
                    lastSpecial = ch;
                    newCount += 4;
                }
                continue;
            }

            boolean special = (ch < 64 && (sepcialBits & (1L << ch)) != 0) || ch == backSlash;
            if (special) {
                specialCount++;
                lastSpecialIndex = i;
                lastSpecial = ch;
                if (ch == '(' || ch == ')' || ch == '<' || ch == '>' || (ch < specicalFlags_doubleQuotes.length && specicalFlags_doubleQuotes[ch] == 4)) {
                    newCount += 4;
                }
                firstSpecialIndex = firstSpecialIndex == -1 ? i : firstSpecialIndex;
            }
        }

        if (specialCount > 0) {
            newCount += specialCount;
            expandCapacityIfNecessary(newCount);
            count = newCount;
            if (specialCount == 1) {
                if (lastSpecial == '\u2028') {
                    int srcPos = lastSpecialIndex + 1;
                    int destPos = lastSpecialIndex + 6;
                    int lengthOfCopy = end - lastSpecialIndex - 1;
                    System.arraycopy(buf, srcPos, buf, destPos, lengthOfCopy);
                    buf[lastSpecialIndex++] = backSlash;
                    buf[lastSpecialIndex++] = 'u';
                    buf[lastSpecialIndex++] = '2';
                    buf[lastSpecialIndex++] = '0';
                    buf[lastSpecialIndex++] = '2';
                    buf[lastSpecialIndex] = '8';
                } else if (lastSpecial == '\u2029') {
                    int srcPos = lastSpecialIndex + 1;
                    int destPos = lastSpecialIndex + 6;
                    int lengthOfCopy = end - lastSpecialIndex - 1;
                    System.arraycopy(buf, srcPos, buf, destPos, lengthOfCopy);
                    buf[lastSpecialIndex++] = backSlash;
                    buf[lastSpecialIndex++] = 'u';
                    buf[lastSpecialIndex++] = '2';
                    buf[lastSpecialIndex++] = '0';
                    buf[lastSpecialIndex++] = '2';
                    buf[lastSpecialIndex] = '9';
                } else if (lastSpecial == '(' || lastSpecial == ')' || lastSpecial == '<' || lastSpecial == '>') {
                    int srcPos = lastSpecialIndex + 1;
                    int destPos = lastSpecialIndex + 6;
                    int lengthOfCopy = end - lastSpecialIndex - 1;
                    System.arraycopy(buf, srcPos, buf, destPos, lengthOfCopy);
                    writeUnicode(lastSpecial);
                } else {
                    char ch = lastSpecial;
                    if (ch < specicalFlags_doubleQuotes.length && specicalFlags_doubleQuotes[ch] == 4) {
                        int srcPos = lastSpecialIndex + 1;
                        int destPos = lastSpecialIndex + 6;
                        int lengthOfCopy = end - lastSpecialIndex - 1;
                        System.arraycopy(buf, srcPos, buf, destPos, lengthOfCopy);
                        writeUnicode(ch);
                    } else {
                        int srcPos = lastSpecialIndex + 1;
                        int destPos = lastSpecialIndex + 2;
                        int lengthOfCopy = end - lastSpecialIndex - 1;
                        System.arraycopy(buf, srcPos, buf, destPos, lengthOfCopy);
                        writeReplaceChar(lastSpecial);
                    }
                }
            }
        } else {
            int textIndex = firstSpecialIndex - start;
            int bufIndex = firstSpecialIndex;
            for (int i = textIndex; i < text.length(); i++) {
                char ch = text.charAt(i);
                if (browserSecure && (ch == '(' || ch == ')' || ch == '<' || ch == '>')) {
                    writeUnicode(ch);
                    end += 5;
                } else if (ch < specicalFlags_doubleQuotes.length && specicalFlags_doubleQuotes[ch] != 0 || (ch == forwardSlash && isEnabled(WriteSlashAsSpecial))) {
                    if (specicalFlags_doubleQuotes[ch] == 4) {
                        writeUnicode(ch);
                        end += 5;
                    } else {
                        writeReplaceChar(ch);
                        end++;
                    }
                } else {
                    if (ch == '\u2028' || ch == '\u2029') {
                        buf[bufIndex++] = '\\';
                        buf[bufIndex++] = 'u';
                        buf[bufIndex++] = DIGITS[(ch >>> 12) & 15];
                        buf[bufIndex++] = DIGITS[(ch >>> 8) & 15];
                        buf[bufIndex++] = DIGITS[(ch >>> 4) & 15];
                        buf[bufIndex++] = DIGITS[ch & 15];
                        end += 5;
                    } else {
                        buf[bufIndex++] = ch;
                    }
                }
            }
        }

        if (seperator != 0) {
            buf[count - 2] = doubleQuete;
            buf[count - 1] = seperator;
        } else {
            buf[count - 1] = doubleQuete;
        }
    }

    private void writeAscii(char ch) {
        write('\\');
        write('u');
        write('0');
        write('0');
        write(ASCII_CHARS[ch * 2]);
        write(ASCII_CHARS[ch * 2 + 1]);
    }

    private void writeSeperatorIfNecessary(int seperator) {
        if (seperator != 0)
            write(seperator);
        
    }

    private void writeReplaceChar(int ch) {
        writeBackSlash();
        write(replaceChars[ch]);
    }

    private void writeUnicode(char ch) {
        write('\\');
        write('u');
        write(DIGITS[(ch >>> 12) & 15]);
        write(DIGITS[(ch >>> 8) & 15]);
        write(DIGITS[(ch >>> 4) & 15]);
        write(DIGITS[ch & 15]);
    }

    private void writeDoubleQuete() {
        write(doubleQuete);
    }
}
