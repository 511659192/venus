package com.ym.materials.unsafe;

public interface Array<T> {

    /**
     * 根据下标获取指定的元素
     * @param index
     * @return
     */
    public T getObject(int index);
    /**
     * 根据下标修改指定的元素
     * @param index
     * @param element
     * @return
     */
    public boolean setObject(int index, T element);
    /**
     * 添加元素
     * @param element
     * @return
     */
    public boolean addObject(T element);
    /**
     * 删除元素
     * @param element
     * @return
     */
    public T remove(T element);
    /**
     * 根据下标删除元素
     * @param index
     * @return
     */
    public boolean remove(int index);

}
