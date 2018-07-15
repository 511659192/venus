package com.ym.materials.guava.comon;

import com.google.common.collect.AbstractIterator;

import java.util.Iterator;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by ym on 2018/7/15.
 */
public class Splitter {

    private final CharMatcher matcher; // 分割符匹配器
    private final CharMatcher trimmer; // 字符排除其
    private final boolean omitEmptyStrings; // 是否排除空字符串
    private final Strategy strategy = new DefaultStrategy(); // 遍历策略

    private Splitter(CharMatcher matcher) {
        this(matcher, CharMatcher.None.instance, false);
    }

    private Splitter(CharMatcher matcher, CharMatcher trimmer, boolean omitEmptyStrings) {
        this.matcher = matcher;
        this.trimmer = trimmer;
        this.omitEmptyStrings = omitEmptyStrings;
    }

    public static Splitter on(String separator) {
        checkArgument(separator.length() >= 1);
        if (separator.length() == 1) {
            return on(CharMatcher.Is.create(separator.charAt(0)));
        }
        return null;
    }

    public static Splitter on(Character separator) {
        checkNotNull(separator);
        return on(CharMatcher.Is.create(separator));
    }

    public static Splitter on(CharMatcher matcher) {
        return new Splitter(matcher);
    }

    private Iterator<String> split(CharSequence toSplit) {
        return strategy.iterator(this, toSplit);
    }

    public Splitter omitEmptyStrings() {
        return new Splitter(matcher, trimmer, true);
    }

    private Splitter trimResult() {
        return new Splitter(matcher, CharMatcher.Whitespace.instance, omitEmptyStrings);
    }

    private interface Strategy {
        Iterator<String> iterator(Splitter splitter, CharSequence toSplit);
    }

    private class DefaultStrategy implements Strategy {

        @Override
        public Iterator<String> iterator(Splitter splitter, CharSequence toSplit) {
            return new SplittingIterator(splitter, toSplit) { // 遍历器
                @Override
                int separatorStart(int start) {
                    return matcher.indexIn(toSplit, start);
                }

                @Override
                int separatorEnd(int separatorPosition) {
                    return separatorPosition + 1;
                }
            };
        }
    }

    private abstract static class SplittingIterator extends AbstractIterator<String> {

        final CharMatcher matcher;
        final CharMatcher trimmer;
        final CharSequence toSplit;
        final boolean omitEmptyStrings;

        public SplittingIterator(Splitter splitter, CharSequence toSplit) {
            this.matcher = splitter.matcher;
            this.omitEmptyStrings = splitter.omitEmptyStrings;
            this.trimmer = splitter.trimmer;
            this.toSplit = toSplit;
        }

        private int offset; // 偏移量

        @Override
        protected String computeNext() {
            while (offset != -1) {
                int start = offset;
                int end;
                int pos = separatorStart(start);
                if (pos == -1) {
                    end = toSplit.length();
                    offset = -1;
                } else {
                    offset = separatorEnd(end = pos);
                }

                while (start < end && trimmer.matches(toSplit.charAt(start))) {
                    start++;
                }

                while (start < end && trimmer.matches(toSplit.charAt(end - 1))) {
                    end--;
                }

                if (omitEmptyStrings && start == end) {
                    continue;
                }
                return toSplit.subSequence(start, end).toString();
            }
            return endOfData();
        }

        abstract int separatorStart(int start);

        abstract int separatorEnd(int separatorPosition);
    }

    public static void main(String[] args) {
        Iterator<String> split = Splitter.on(",").trimResult().omitEmptyStrings().split("1,2,3,,4, ,1");
        while (split.hasNext()) {
            String next = split.next();
            System.out.println(next);
        }
    }
}
