package com.ym.materials.guava;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 * Created by ym on 2018/6/27.
 */
public abstract class Ordering<T> implements Comparator<T> {

    public static <C extends Comparable> Ordering<C> natural() {
        return (Ordering<C>) NaturalOrdering.INSTANCE;
    }

    public static <T> Ordering from(Comparator<T> comparator) {
        return comparator instanceof Ordering ? (Ordering) comparator : new ComparatorOrdering<T>(comparator);
    }

    public <S extends T> Ordering<S> reverse() {
        return new ReverseOrdering<S>(this);
    }

    public <S extends T> Ordering<S> nullsFirst() {
        return new NullsFirstOrdering<S>(this);
    }

    public <S extends T> Ordering<S> nullsLast() {
        return new NullsLastOrdering<S>(this);
    }

    public final static class NaturalOrdering extends Ordering<Comparable> implements Serializable {

        private NaturalOrdering() {
        }

        public final static NaturalOrdering INSTANCE = new NaturalOrdering();

        @Override
        public int compare(Comparable left, Comparable right) {
            assertNotNull(left);
            assertNotNull(right);
            return left.compareTo(right);
        }

        public <S extends Comparable> Ordering<S> reverse() {
            return (Ordering<S>) ReverseNaturalOrdering.INSTANCE;
        }
    }

    public final static class ReverseNaturalOrdering extends Ordering<Comparable> implements Serializable {

        private ReverseNaturalOrdering() {
        }

        public final static ReverseNaturalOrdering INSTANCE = new ReverseNaturalOrdering();

        @Override
        public int compare(Comparable left, Comparable right) {
            assertNotNull(left);
            if (left == right) {
                return 0;
            }
            return right.compareTo(left);
        }

        public <S extends Comparable> Ordering reverse() {
            return NaturalOrdering.INSTANCE;
        }

    }

    private final static class ComparatorOrdering<T> extends Ordering<T> implements Serializable {

        final Comparator<T> comparator;

        public ComparatorOrdering(Comparator<T> comparator) {
            assertNotNull(comparator);
            this.comparator = comparator;
        }

        @Override
        public int compare(T left, T right) {
            return comparator.compare(left, right);
        }

        @Override
        public boolean equals(Object object) {
            if (object == this) {
                return true;
            }
            if (object instanceof ComparatorOrdering) {
                ComparatorOrdering that = (ComparatorOrdering) object;
                return this.comparator.equals(that.comparator);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return comparator.hashCode();
        }
    }


    private final static class ReverseOrdering<T> extends Ordering<T> implements Serializable {

        final Ordering<? super T> ordering;

        public ReverseOrdering(Ordering<? super T> ordering) {
            assertNotNull(ordering);
            this.ordering = ordering;
        }

        @Override
        public int compare(T left, T right) {
            return ordering.compare(right, left);
        }
    }

    private final static class NullsFirstOrdering<T> extends Ordering<T> implements Serializable {

        final Ordering<? super T> ordering;

        public NullsFirstOrdering(Ordering<? super T> ordering) {
            assertNotNull(ordering);
            this.ordering = ordering;
        }

        @Override
        public int compare(T next, T pre) {
            System.out.println("next:" + next + " pre:" + pre);
            if (next == pre) {
                return 0;
            }
            if (next == null) {
                return -1;
            }
            if (pre == null) {
                return 1;
            }
            return ordering.compare(next, pre);
        }
    }

    private final static class NullsLastOrdering<T> extends Ordering<T> implements Serializable {
        final Ordering<? super T> ordering;

        public NullsLastOrdering(Ordering<? super T> ordering) {
            assertNotNull(ordering);
            this.ordering = ordering;
        }

        @Override
        public int compare(T left, T right) {
            if (left == right) {
                return 0;
            }
            if (left == null) {
                return 1;
            }
            if (right == null) {
                return -1;
            }
            return ordering.compare(left, right);
        }
    }

    static final int LEFT_IS_GREATER = 1;
    static final int RIGHT_IS_GREATER = -1;

    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<>();
        list.add(null);
        list.add("jerry");
        list.add("jerry2");
        list.add(null);
        list.add("jerry3");
        list.sort(Ordering.natural().nullsFirst());
        System.out.println(JSON.toJSONString(list));
        System.out.println(Integer.valueOf(1).compareTo(2));
    }
}
