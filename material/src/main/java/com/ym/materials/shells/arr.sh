#!/bin/bash
nums=(29 100 13 8 91 44)
echo ${nums[@]}  #输出所有数组元素
nums[10]=66  #给第10个元素赋值（此时会增加数组长度）
echo ${nums[*]}  #输出所有数组元素
echo ${nums[4]}  #输出第4个元素
echo ${nums[0]}  #输出第4个元素

echo ${#nums[*]}  #输出所有数组元素
echo ${#nums[4]}  #输出第4个元素

#删除数组元素
unset nums[1]
echo ${nums[*]}


array1=(23 56)
array2=(99 "http://c.biancheng.net/shell/")
array_new=(${array1[@]} ${array2[*]})
echo ${array_new[@]}  #也可以写作 ${array_new[*]}