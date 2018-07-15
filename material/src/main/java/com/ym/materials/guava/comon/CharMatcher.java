package com.ym.materials.guava.comon;

import com.google.common.base.Preconditions;

/**
 * Created by ym on 2018/7/15.
 */
public abstract class CharMatcher {

    public abstract boolean matches(char c);

    public int indexIn(CharSequence sequence, int start) {
        int length = sequence.length();
        Preconditions.checkArgument(start < length);
        for (int i = start; i < length; i++) {
            if (matches(sequence.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    public static final class Whitespace extends CharMatcher{
        static final Whitespace instance = new Whitespace();

        static final char whitespace = ' ';

        @Override
        public boolean matches(char c) {
            return whitespace == c;
        }
    }

    abstract static class FastMatcher extends CharMatcher {

    }

    abstract static class NamedFastMatcher extends FastMatcher {
        private final String desc;
        public NamedFastMatcher(String desc) {
            this.desc = desc;
        }
    }


    public static class Is extends FastMatcher {

        static Is create(char match) {
            return new Is(match);
        }

        private final char match;

        public Is(char match) {
            this.match = match;
        }

        @Override
        public boolean matches(char c) {
            return c == match;
        }
    }

    public static class None extends NamedFastMatcher {
        static final None instance = new None();

        private None() {
            super("charMatcher none");
        }

        @Override
        public boolean matches(char c) {
            return false;
        }
    }
}

