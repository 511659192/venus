#!/bin/bash

export feature=features/v1.6
alias work='op(){gitm $* co $feature;};op'
alias dev='op(){gitm $* co develop;};op'
alias qa='op(){gitm $* co qa;};op'
alias master='op(){gitm $* co master;};op'
alias pull='op(){gitm $* pull;};op'
alias push='op(){cnt=$#;cnt=$(($cnt-1));echo $cnt;gitm ${@:1:$cnt} add .;gitm ${@:1:$cnt} ci -m ${!#};gitm ${@:1:$cnt} push};op'
alias mergeToDevelop='op(){echo "-------------------checkout-------------------";gitm $* co develop;echo "-------------------merge-------------------";gitm $* merge $feature};op'
alias mergeToQa='op(){echo "-------------------checkout-------------------";gitm $* co qa;echo "-------------------merge-------------------";gitm $* merge $feature};op'


PROJECT_PATH=/Users/yangmeng/work/ps
#项目地址
SUB_PROJECT_PATHS=(banma_service_mall_common banma_service_mall_admin_client banma_service_mall_server banma_service_mall_admin_server banma_admin_mall)
#项目别名
SUB_PROJECT_ALIAS=(common masjar mos mas mall)
#命令参数
commandParams=$*
#是否指定工程
for inputParam in $*;
do
    for _alias in ${!SUB_PROJECT_ALIAS[@]};
    do
        [[ $inputParam == ${SUB_PROJECT_ALIAS[$_alias]} ]] && {
            assignedProject+=${SUB_PROJECT_PATHS[$_alias]}" "
            shift
            break
        }
    done
done

#指定工程 重设设置项目地址
[[ -n $assignedProject ]] && {
    SUB_PROJECT_PATHS=($assignedProject)
}

for subPath in ${SUB_PROJECT_PATHS[*]}
do
    cd ${PROJECT_PATH}"/"${subPath}
    echo ">>>$subPath<<<"
    git $*
done
