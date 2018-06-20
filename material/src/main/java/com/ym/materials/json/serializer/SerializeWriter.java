package com.ym.materials.json.serializer;

import com.alibaba.fastjson.JSONException;

import java.io.IOException;
import java.io.Writer;

public class SerializeWriter extends Writer {

    private final static ThreadLocal<char[]> bufLocal = new ThreadLocal<char[]>();

    protected char                           buf[];

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
            if (feature == SerializerFeature.WriteEnumUsingToString) {
                features &= ~SerializerFeature.WriteEnumUsingName.getMask();
            } else if (feature == SerializerFeature.WriteEnumUsingName) {
                features &= ~SerializerFeature.WriteEnumUsingToString.getMask();
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
            | SerializerFeature.UseSingleQuotes.mask //
            | SerializerFeature.BrowserCompatible.mask //
            | SerializerFeature.PrettyFormat.mask //
            | SerializerFeature.WriteEnumUsingToString.mask
            | SerializerFeature.WriteNonStringValueAsString.mask
            | SerializerFeature.WriteSlashAsSpecial.mask
            | SerializerFeature.IgnoreErrorGetter.mask
            | SerializerFeature.WriteClassName.mask
            | SerializerFeature.NotWriteDefaultValue.mask
            ;

    private void computeFeatures() {
        quoteFieldNames = (this.features & SerializerFeature.QuoteFieldNames.mask) != 0;
        useSingleQuotes = (this.features & SerializerFeature.UseSingleQuotes.mask) != 0;
        sortField = (this.features & SerializerFeature.SortField.mask) != 0;
        disableCircularReferenceDetect = (this.features & SerializerFeature.DisableCircularReferenceDetect.mask) != 0;
        beanToArray = (this.features & SerializerFeature.BeanToArray.mask) != 0;
        writeNonStringValueAsString = (this.features & SerializerFeature.WriteNonStringValueAsString.mask) != 0;
        notWriteDefaultValue = (this.features & SerializerFeature.NotWriteDefaultValue.mask) != 0;
        writeEnumUsingName = (this.features & SerializerFeature.WriteEnumUsingName.mask) != 0;
        writeEnumUsingToString = (this.features & SerializerFeature.WriteEnumUsingToString.mask) != 0;
        writeDirect = quoteFieldNames && (this.features & nonDirectFeatures) == 0 && (beanToArray || writeEnumUsingName);
        keySeperator = useSingleQuotes ? '\'' : '"';
        browserSecure = (this.features & SerializerFeature.BrowserSecure.mask) != 0;
        // 10011111111111111111111111111111111
        // 100000000000010011111111111111111111111111111111
        // 101000000000000000000110000010011111111111111111111111111111111
        final long S0 = 0x4FFFFFFFFL, S1 = 0x8004FFFFFFFFL, S2 = 0x50000304ffffffffL;
        sepcialBits = browserSecure ? S2 : (this.features & SerializerFeature.WriteSlashAsSpecial.mask) != 0 ? S1 : S0;
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
                writeStringWithDoubleQuote(key, ':');
            } else {

            }
        }
    }

    private void writeStringWithSingleQuote(String text) {
        if (text == null) {
            int newCount = count + 4;
            if (newCount > buf.length) {
                expandCapacity(newCount);
            }
            "null".getChars(0, 4, buf, count);
            count = newCount;
            return;
        }

        int len = text.length();
        int newCount = count + len + 2;
        if (newCount > buf.length) {
            if (writer !)
        }
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
}
