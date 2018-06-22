package com.ym.materials.unsafe;

import java.lang.reflect.Field;
import java.util.Arrays;

import sun.misc.Unsafe;

public class ArrayUnsafe<T> implements Array<T> {

    private static final int DEFAULT_INITIAL_CAPACITY = 10;
    private transient volatile Object[] elements;
    private volatile int size = 0;

    private static final Unsafe UNSAFE;
    private static final long ARRAY_OFFSET;
    private static final int ARRAY_SHIFT;
    private static final int INDEX_SCALE;
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    private static final long SIZE_OFFSET;

    static {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            UNSAFE = (Unsafe)unsafeField.get(null);
            ARRAY_OFFSET = UNSAFE.arrayBaseOffset(Object[].class);
            INDEX_SCALE = UNSAFE.arrayIndexScale(Object[].class);
            ARRAY_SHIFT = 31 - Integer.numberOfLeadingZeros(INDEX_SCALE);
            SIZE_OFFSET = UNSAFE.objectFieldOffset(ArrayUnsafe.class.getDeclaredField("size"));
            System.out.println("ARRAY_OFFSET:" + ARRAY_OFFSET);
            System.out.println("INDEX_SCALE:" + INDEX_SCALE);
            System.out.println(Integer.toBinaryString(INDEX_SCALE));
            System.out.println("ARRAY_SHIFT:" + ARRAY_SHIFT);
            System.out.println("SIZE_OFFSET:" + SIZE_OFFSET);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public ArrayUnsafe() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    public ArrayUnsafe(int initialCapacity) {
        this.elements = new Object[initialCapacity];
    }

    public T getObject(int index) {
        return (T)UNSAFE.getObjectVolatile(elements, getIndexScale(index));
    }

    public boolean setObject(int index, T element) {
        rangeCheck(index);
        T oldElement = getObject(index);
        UNSAFE.putOrderedObject(elements, getIndexScale(index), element);
        if(oldElement != null && element == null) {
            int numMoved = size - index - 1;
            if(numMoved > 0) {
                System.arraycopy(elements, index + 1, elements, index, numMoved);
            }
            UNSAFE.compareAndSwapInt(this, SIZE_OFFSET, size, size - 1);
            UNSAFE.putOrderedObject(elements, getIndexScale(size), null);
        }
        return true;
    }

    private long getIndexScale(long index) {
        return (index << ARRAY_SHIFT) + ARRAY_OFFSET;
    }

    private void ensureCapacityInternal(int minCapacity) {
        if(minCapacity - elements.length > 0)
            grow(minCapacity);
    }

    private void grow(int minCapacity) {
        int oldCapacity = elements.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if(newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if(newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        elements = Arrays.copyOf(elements, newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        if(minCapacity < 0)
            throw new OutOfMemoryError();
        return minCapacity > MAX_ARRAY_SIZE ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
    }

    public boolean addObject(T element) {
        ensureCapacityInternal(size + 1);
        UNSAFE.putOrderedObject(elements, getIndexScale(size), element);
        UNSAFE.compareAndSwapInt(this, SIZE_OFFSET, size, size + 1);
        return true;
    }

    private void rangeCheck(int index) {
        if(index >= size || index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    public T remove(T element) {
        for(int i = 0; i < elements.length; i++) {
            int index = i;
            T e = getObject(index);
            if(e != null) {
                if(e.equals(element)) {
                    setObject(index, null);
                    return e;
                }
            }
        }
        return null;
    }

    public boolean remove(int index) {
        rangeCheck(index);
        T removedElement = getObject(index);
        if(removedElement != null)
            return setObject(index, null);
        return false;
    }

    public static void main(String[] args) {
        Array<String> strArray = new ArrayUnsafe<String>();
        for(int i = 0; i < 10; i++) {
            strArray.addObject(Integer.toBinaryString(i));
        }
        strArray.addObject("hello");
        strArray.addObject("world");
        String s0 = strArray.getObject(0);// get "0"
        System.out.println("s0:" + s0);
        String s2 = strArray.getObject(2);// get "2"
        System.out.println("s2:" + s2);
        strArray.setObject(1, null);
        String s6 = strArray.remove("6"); // remove "6"
        System.out.println("s6:" + s6);
        String sNull = strArray.remove("foo");// null
        boolean result = strArray.remove(9);
    }
}
