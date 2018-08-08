package com.ym.materials.guava.cache;

/**
 * Created by ym on 2018/8/5.
 */
public enum  RemovalCause {

    COLLECTED {
        @Override
        boolean wasEvicted() {
            return true;
        }
    },
    EXPIRED {
        @Override
        boolean wasEvicted() {
            return true;
        }
    },
    REPLACED {
        @Override
        boolean wasEvicted() {
            return false;
        }
    };

    abstract boolean wasEvicted();
}
