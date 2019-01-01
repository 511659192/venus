package com.ym.materials.sort;

import java.util.Arrays;

/**
 * Created by ym on 2018/11/6.
 */
public class Sort {
    public static void main(String args[]) {
//        int[] a = new int[]{16, 25, 34, 27, 30, 5, 7, 4, 41, 55};
        int[] a = {1, 5, 6, 8, 7, 2, 3, 4, 9};
        Sort.heapSort(a);
        System.out.println(Arrays.toString(a));
    }

    static int parent(int i) {
        return (i - 1) / 2;
    }

    static int left(int i) {
        return 2 * i + 1;
    }

    static int right(int i) {
        return 2 * i + 2;
    }

    static void maxHeapfy(int[] a, int i, int heapSize) {
        //数组a，第i个结点，heapSize是数组种实际要排序的元素的长度
        int left = left(i);
        //有的时候能够递归到叶子结点，叶子结点无后继，下面两个if都注意到了这一点
        int right = right(i);
        int largest = i;
        if (left < heapSize && a[left] > a[largest]) {
            largest = left;
        }
        if (right < heapSize && a[right] > a[largest]) {
            largest = right;
        }
        if (largest != i) {
            //把最大值给父结点
            a[largest] = a[largest] ^ a[i];
            a[i] = a[largest] ^ a[i];
            a[largest] = a[largest] ^ a[i];
            maxHeapfy(a, largest, heapSize);
            //发生交换之后还要保证大根堆性质
        }
    }

    static void buildMaxHeap(int[] a, int heapSize) {
        for (int i = (heapSize - 2) / 2; i >= 0; i--) {
            maxHeapfy(a, i, heapSize);
        }
    }

    static void heapSort(int[] a) {
        int len = a.length;
        buildMaxHeap(a, len); //初始建堆
        a[len - 1] = a[0] ^ a[len - 1]; //交换
        a[0] = a[0] ^ a[a.length - 1];
        a[len - 1] = a[0] ^ a[len - 1];
        for (int i = 1; i < len - 1; i++) {
            //初始建堆之后还要排a.length-2次
            maxHeapfy(a, 0, len - i);
            a[len - 1 - i] = a[0] ^ a[len - 1 - i];
            //交换
            a[0] = a[0] ^ a[len - 1 - i];
            a[len - 1 - i] = a[0] ^ a[len - 1 - i];
        }
    }
}