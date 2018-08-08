package com.ym.materials.guava.cache;

import com.google.common.base.Equivalence;
import com.ym.materials.guava.cache.LocalCache.Segment;
import com.ym.materials.guava.cache.ValueReference.SoftValueReference;
import com.ym.materials.guava.cache.ValueReference.StrongValueReference;
import com.ym.materials.guava.cache.ValueReference.WeakValueReference;

/**
 * Created by ym on 2018/8/5.
 */
public enum Strength {
    STRONG {
        @Override
        <K, V> ValueReference<K, V> referenceValue(Segment<K, V> segment, ReferenceEntry<K, V> entry, V value) {
            return new StrongValueReference<K, V>(value);
        }

        @Override
        Equivalence<Object> defaultEquivalence() {
            return Equivalence.equals();
        }
    },
    SOFT {
        @Override
        <K, V> ValueReference<K, V> referenceValue(Segment<K, V> segment, ReferenceEntry<K, V> entry, V value) {
            return new SoftValueReference<K, V>(segment.valueReferenceQueue, value, entry);
        }

        @Override
        Equivalence<Object> defaultEquivalence() {
            return Equivalence.identity();
        }
    },
    WEAK {
        @Override
        <K, V> ValueReference<K, V> referenceValue(Segment<K, V> segment, ReferenceEntry<K, V> entry, V value) {
            return new WeakValueReference<K, V>(segment.valueReferenceQueue, value, entry);
        }

        @Override
        Equivalence<Object> defaultEquivalence() {
            return Equivalence.identity();
        }
    }
    ;


    abstract <K, V> ValueReference<K, V> referenceValue(
            Segment<K, V> segment, ReferenceEntry<K, V> entry, V value);
    abstract Equivalence<Object> defaultEquivalence();
}
