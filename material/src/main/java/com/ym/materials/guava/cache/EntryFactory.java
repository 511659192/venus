package com.ym.materials.guava.cache;

import com.sun.istack.internal.Nullable;
import com.ym.materials.guava.cache.LocalCache.Segment;
import com.ym.materials.guava.cache.ReferenceEntry.StrongAccessWriteEntry;
import com.ym.materials.guava.cache.ReferenceEntry.StrongWriteEntry;

import static com.ym.materials.guava.cache.ReferenceEntryUtils.connectAccessOrder;
import static com.ym.materials.guava.cache.ReferenceEntryUtils.nullifyAccessOrder;

/**
 * Created by ym on 2018/8/5.
 */
public enum  EntryFactory {

    STRONG {
        @Override
        <K, V> ReferenceEntry<K, V> newEntry(Segment<K, V> segment, K key, int hash, ReferenceEntry<K, V> next) {
            return new ReferenceEntry.StrongEntry<K, V>(key, hash, next);
        }
    },
    STRONG_ACCESS {
        @Override
        <K, V> ReferenceEntry<K, V> newEntry(Segment<K, V> segment, K key, int hash, ReferenceEntry<K, V> next) {
            return new ReferenceEntry.StrongAccessEntry<>(key, hash, next);
        }

        @Override
        <K, V> ReferenceEntry<K, V> copyEntry(Segment<K, V> segment, ReferenceEntry<K, V> original, ReferenceEntry<K, V> newNext) {
            ReferenceEntry<K, V> newEntry = super.copyEntry(segment, original, newNext);
            copyAccessEntry(original, newEntry);
            return newEntry;
        }
    },
    STRONG_WRITE {
        @Override
        <K, V> ReferenceEntry<K, V> newEntry(
                Segment<K, V> segment, K key, int hash, @Nullable ReferenceEntry<K, V> next) {
            return new StrongWriteEntry<>(key, hash, next);
        }

        @Override
        <K, V> ReferenceEntry<K, V> copyEntry(
                Segment<K, V> segment, ReferenceEntry<K, V> original, ReferenceEntry<K, V> newNext) {
            ReferenceEntry<K, V> newEntry = super.copyEntry(segment, original, newNext);
            copyWriteEntry(original, newEntry);
            return newEntry;
        }
    },
    STRONG_ACCESS_WRITE {
        @Override
        <K, V> ReferenceEntry<K, V> newEntry(
                Segment<K, V> segment, K key, int hash, @Nullable ReferenceEntry<K, V> next) {
            return new StrongAccessWriteEntry<>(key, hash, next);
        }

        @Override
        <K, V> ReferenceEntry<K, V> copyEntry(
                Segment<K, V> segment, ReferenceEntry<K, V> original, ReferenceEntry<K, V> newNext) {
            ReferenceEntry<K, V> newEntry = super.copyEntry(segment, original, newNext);
            copyAccessEntry(original, newEntry);
            copyWriteEntry(original, newEntry);
            return newEntry;
        }
    }

    ;

    static final int ACCESS_MASK = 1;
    static final int WRITE_MASK = 1 << 1;

    static final EntryFactory[] factories = {
            STRONG,
            STRONG_ACCESS,
            STRONG_WRITE,
            STRONG_ACCESS_WRITE
    };

    static EntryFactory getFactory(Strength keyStrength, boolean usesAccessQueue, boolean usesWriteQueue) {
        int index = (usesAccessQueue ? ACCESS_MASK : 0) | (usesWriteQueue ? WRITE_MASK : 0);
        return factories[index];
    }

    <K, V> ReferenceEntry<K, V> copyEntry(
            Segment<K, V> segment, ReferenceEntry<K, V> original, ReferenceEntry<K, V> newNext) {
        return newEntry(segment, original.getKey(), original.getHash(), newNext);
    }

    <K, V> void copyAccessEntry(ReferenceEntry<K, V> original, ReferenceEntry<K, V> newEntry) {
        newEntry.setAccessTime(original.getAccessTime());
        connectAccessOrder(original.getPreviousInAccessQueue(), newEntry);
        connectAccessOrder(newEntry, original.getNextInAccessQueue());
        nullifyAccessOrder(original);
    }

    <K, V> void copyWriteEntry(ReferenceEntry<K, V> original, ReferenceEntry<K, V> newEntry) {
        newEntry.setWriteTime(original.getWriteTime());
        connectAccessOrder(original.getPreviousInWriteQueue(), newEntry);
        connectAccessOrder(newEntry, original.getNextInWriteQueue());
        nullifyAccessOrder(original);
    }

    abstract <K, V> ReferenceEntry<K, V> newEntry(Segment<K, V> segment, K key, int hash, @Nullable ReferenceEntry<K, V> next);
}
