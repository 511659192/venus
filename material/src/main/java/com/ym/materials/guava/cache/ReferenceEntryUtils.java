package com.ym.materials.guava.cache;

/**
 * Created by ym on 2018/8/5.
 */
public class ReferenceEntryUtils {

    static <K, V> void connectAccessOrder(ReferenceEntry<K, V> previous, ReferenceEntry<K, V> next) {
        previous.setNextInAccessQueue(next);
        next.setPreviousInAccessQueue(previous);
    }

    static <K, V> void nullifyAccessOrder(ReferenceEntry<K, V> nulled) {
        ReferenceEntry<K, V> nullEntry = (ReferenceEntry<K, V>) ReferenceEntry.NullEntry.INTANCE;
        nulled.setNextInAccessQueue(nullEntry);
        nulled.setPreviousInAccessQueue(nullEntry);
    }


}
