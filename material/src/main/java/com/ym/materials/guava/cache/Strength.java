package com.ym.materials.guava.cache;

import com.google.common.base.Equivalence;

/**
 * Created by ym on 2018/8/5.
 */
public enum Strength {
    STRONG {
        @Override
        <K, V> LocalCache.ValueReference<K, V> referenceValue(LocalCache.Segment<K, V> segment, ReferenceEntry<K, V> entry, V value) {
            return new LocalCache.StrongValueReference<K, V>(value);
        }

        @Override
        Equivalence<Object> defaultEquivalence() {
            return Equivalence.equals();
        }
    },
    SOFT {
        @Override
        <K, V> LocalCache.ValueReference<K, V> referenceValue(LocalCache.Segment<K, V> segment, ReferenceEntry<K, V> entry, V value) {
            return new LocalCache.SoftValueReference<K, V>(segment.valueReferenceQueue, value, entry);
        }

        @Override
        Equivalence<Object> defaultEquivalence() {
            return Equivalence.identity();
        }
    },
    WEAK {
        @Override
        <K, V> LocalCache.ValueReference<K, V> referenceValue(LocalCache.Segment<K, V> segment, ReferenceEntry<K, V> entry, V value) {
            return new LocalCache.WeakValueReference<K, V>(segment.valueReferenceQueue, value, entry);
        }

        @Override
        Equivalence<Object> defaultEquivalence() {
            return Equivalence.identity();
        }
    }
    ;


    abstract <K, V> LocalCache.ValueReference<K, V> referenceValue(
            LocalCache.Segment<K, V> segment, ReferenceEntry<K, V> entry, V value);
    abstract Equivalence<Object> defaultEquivalence();
}
