#!/bin/bash
#if [ "$1" == 100 ]
#then
#   exit 0  #参数正确，退出状态为0
#else
#   exit 1  #参数错误，退出状态1
#fi


#得到两个数相加的和
function add(){
    return `expr $1 + $2`
}
add 23 50  #调用函数
echo $?  #获取函数返回值