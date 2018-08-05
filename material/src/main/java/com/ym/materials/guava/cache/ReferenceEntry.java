package com.ym.materials.guava.cache;

/**
 * Created by ym on 2018/8/4.
 */
interface ReferenceEntry<K, V> {


    ValueReference<K, V> getValueReference();
    void setValueReference(ValueReference<K, V> valueReference);
    ReferenceEntry<K, V> getNext();
    int getHash();
    K getKey();

    long getAccessTime();
    void setAccessTime(long time);

    long getWriteTime();
    void setWriteTime(long time);

    ReferenceEntry<K, V> getNextInWriteQueue();
    void setNextInWriteQueue(ReferenceEntry<K, V> next);
    ReferenceEntry<K, V> getPreviousInWriteQueue();
    void setPreviousInWriteQueue(ReferenceEntry<K, V> previous);

    ReferenceEntry<K, V> getNextInAccessQueue();
    void setNextInAccessQueue(ReferenceEntry<K, V> next);
    ReferenceEntry<K, V> getPreviousInAccessQueue();
    void setPreviousInAccessQueue(ReferenceEntry<K, V> previous);

    abstract class AbstractReferenceEntry<K, V> implements ReferenceEntry<K, V> {
        @Override
        public ValueReference<K, V> getValueReference() {
            return null;
        }

        @Override
        public void setValueReference(ValueReference<K, V> valueReference) {

        }

        @Override
        public ReferenceEntry<K, V> getNext() {
            return null;
        }

        @Override
        public int getHash() {
            return 0;
        }

        @Override
        public K getKey() {
            return null;
        }

        @Override
        public long getAccessTime() {
            return 0;
        }

        @Override
        public void setAccessTime(long time) {
        }

        @Override
        public long getWriteTime() {
            return 0;
        }

        @Override
        public void setWriteTime(long time) {

        }

        @Override
        public ReferenceEntry<K, V> getNextInWriteQueue() {
            return this;
        }

        @Override
        public void setNextInWriteQueue(ReferenceEntry<K, V> next) {

        }

        @Override
        public ReferenceEntry<K, V> getPreviousInWriteQueue() {
            return this;
        }

        @Override
        public void setPreviousInWriteQueue(ReferenceEntry<K, V> previous) {

        }

        @Override
        public ReferenceEntry<K, V> getNextInAccessQueue() {
            return this;
        }

        @Override
        public void setNextInAccessQueue(ReferenceEntry<K, V> next) {

        }

        @Override
        public ReferenceEntry<K, V> getPreviousInAccessQueue() {
            return this;
        }

        @Override
        public void setPreviousInAccessQueue(ReferenceEntry<K, V> previous) {

        }
    }

    class StrongEntry<K, V> extends AbstractReferenceEntry<K, V> {
        final K key;
        final int hash;
        final ReferenceEntry<K, V> next;
        volatile ValueReference<K, V> valueReference = (ValueReference<K, V>) ValueReference.UNSET;

        public StrongEntry(K key, int hash, ReferenceEntry<K, V> next) {
            this.key = key;
            this.hash = hash;
            this.next = next;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public ValueReference<K, V> getValueReference() {
            return valueReference;
        }

        @Override
        public int getHash() {
            return hash;
        }

        @Override
        public ReferenceEntry<K, V> getNext() {
            return next;
        }

        @Override
        public void setValueReference(ValueReference<K, V> valueReference) {
            this.valueReference = valueReference;
        }
    }

    class StrongAccessEntry<K, V> extends StrongEntry<K, V> {
        volatile long accessTime = Long.MAX_VALUE;
        StrongAccessEntry(K key, int hash, ReferenceEntry<K, V> next) {
            super(key, hash, next);
        }

        @Override
        public long getAccessTime() {
            return accessTime;
        }

        @Override
        public void setAccessTime(long accessTime) {
            this.accessTime = accessTime;
        }

        ReferenceEntry<K, V> nextAccess = (ReferenceEntry<K, V>) NullEntry.INTANCE;

        @Override
        public ReferenceEntry<K, V> getNextInAccessQueue() {
            return nextAccess;
        }

        @Override
        public void setNextInAccessQueue(ReferenceEntry<K, V> next) {
            this.nextAccess = next;
        }

        ReferenceEntry<K, V> previousAccess = (ReferenceEntry<K, V>) NullEntry.INTANCE;

        @Override
        public ReferenceEntry<K, V> getPreviousInAccessQueue() {
            return previousAccess;
        }

        @Override
        public void setPreviousInAccessQueue(ReferenceEntry<K, V> previous) {
            this.previousAccess = previous;
        }
    }

    final class StrongWriteEntry<K, V> extends StrongEntry<K, V> {
        volatile long writeTime = Long.MAX_VALUE;
        StrongWriteEntry(K key, int hash, ReferenceEntry<K, V> next) {
            super(key, hash, next);
        }
        @Override
        public long getWriteTime() {
            return writeTime;
        }

        @Override
        public void setWriteTime(long time) {
            this.writeTime = time;
        }

        // Guarded By Segment.this
        ReferenceEntry<K, V> nextWrite = (ReferenceEntry<K, V>) NullEntry.INTANCE;

        @Override
        public ReferenceEntry<K, V> getNextInWriteQueue() {
            return nextWrite;
        }

        @Override
        public void setNextInWriteQueue(ReferenceEntry<K, V> next) {
            this.nextWrite = next;
        }

        // Guarded By Segment.this
        ReferenceEntry<K, V> previousWrite = (ReferenceEntry<K, V>) NullEntry.INTANCE;

        @Override
        public ReferenceEntry<K, V> getPreviousInWriteQueue() {
            return previousWrite;
        }

        @Override
        public void setPreviousInWriteQueue(ReferenceEntry<K, V> previous) {
            this.previousWrite = previous;
        }
    }

    final class StrongAccessWriteEntry<K, V> extends StrongEntry<K, V> {
        StrongAccessWriteEntry(K key, int hash, ReferenceEntry<K, V> next) {
            super(key, hash, next);
        }

        // The code below is exactly the same for each access entry type.

        volatile long accessTime = Long.MAX_VALUE;

        @Override
        public long getAccessTime() {
            return accessTime;
        }

        @Override
        public void setAccessTime(long time) {
            this.accessTime = time;
        }

        // Guarded By Segment.this
        ReferenceEntry<K, V> nextAccess = (ReferenceEntry<K, V>) NullEntry.INTANCE;

        @Override
        public ReferenceEntry<K, V> getNextInAccessQueue() {
            return nextAccess;
        }

        @Override
        public void setNextInAccessQueue(ReferenceEntry<K, V> next) {
            this.nextAccess = next;
        }

        // Guarded By Segment.this
        ReferenceEntry<K, V> previousAccess = (ReferenceEntry<K, V>) NullEntry.INTANCE;

        @Override
        public ReferenceEntry<K, V> getPreviousInAccessQueue() {
            return previousAccess;
        }

        @Override
        public void setPreviousInAccessQueue(ReferenceEntry<K, V> previous) {
            this.previousAccess = previous;
        }

        // The code below is exactly the same for each write entry type.

        volatile long writeTime = Long.MAX_VALUE;

        @Override
        public long getWriteTime() {
            return writeTime;
        }

        @Override
        public void setWriteTime(long time) {
            this.writeTime = time;
        }

        // Guarded By Segment.this
        ReferenceEntry<K, V> nextWrite = (ReferenceEntry<K, V>) NullEntry.INTANCE;
        @Override
        public ReferenceEntry<K, V> getNextInWriteQueue() {
            return nextWrite;
        }

        @Override
        public void setNextInWriteQueue(ReferenceEntry<K, V> next) {
            this.nextWrite = next;
        }

        // Guarded By Segment.this
        ReferenceEntry<K, V> previousWrite = (ReferenceEntry<K, V>) NullEntry.INTANCE;

        @Override
        public ReferenceEntry<K, V> getPreviousInWriteQueue() {
            return previousWrite;
        }

        @Override
        public void setPreviousInWriteQueue(ReferenceEntry<K, V> previous) {
            this.previousWrite = previous;
        }
    }

    class NullEntry extends AbstractReferenceEntry<Object, Object> {
        static final NullEntry INTANCE = new NullEntry();

        private NullEntry() {

        }
    }
}
