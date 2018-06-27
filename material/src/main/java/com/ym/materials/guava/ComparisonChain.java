package com.ym.materials.guava;

import java.util.Comparator;

/**
 * Created by ym on 2018/6/27.
 */
public abstract class ComparisonChain {

    private ComparisonChain() {
    }

    public ComparisonChain start() {
        return ACTIVE;
    }

    private final static ComparisonChain LESS = new InactiveComparisonChain(-1);
    private final static ComparisonChain GREATER = new InactiveComparisonChain(1);

    private final static ComparisonChain ACTIVE = new ComparisonChain() {
        @Override
        public <T> ComparisonChain compare(T left, T right, Comparator<T> comparator) {
            return classify(comparator.compare(left, right));
        }

        @Override
        public ComparisonChain compare(int left, int right) {
            return classify(Ints.compare(left, right));
        }

        private ComparisonChain classify(int result) {
            // 如果 不相等 则返回链表中的LESS 或者 GRATHER LESS GRATHER 不再进行比对 返回自身
            // 这样多次调用 真的好吗 多了很多个栈帧
            return (result < 0) ? LESS : (result > 0) ? GREATER : ACTIVE;
        }

        @Override
        public int result() {
            return 0;
        }
    };

    private final static class InactiveComparisonChain extends ComparisonChain {

        final int result;

        public InactiveComparisonChain(int result) {
            this.result = result;
        }

        @Override
        public <T> ComparisonChain compare(T left, T right, Comparator<T> comparator) {
            return this;
        }

        @Override
        public ComparisonChain compare(int left, int right) {
            return this;
        }

        @Override
        public int result() {
            return result;
        }
    }

    public abstract <T> ComparisonChain compare(T left, T right, Comparator<T> comparator);

    public abstract ComparisonChain compare(int left, int right);

    public abstract int result();

    private final static class Ints {

        public static int compare(int left, int right) {
            return left < right ? -1 : left > right ? 1 : 0;
        }
    }
}
