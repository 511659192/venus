#!/usr/bin/env bash

function push() {
    declare -i cnt=$#
    cnt=$(($#-1))
    echo $cnt

#    /Users/yangmeng/work/ps/git_mall.sh ${@:1:$#-1} add .
#    /Users/yangmeng/work/ps/git_mall.sh ${@:1:$#-1} ci -m ${!#}
#    /Users/yangmeng/work/ps/git_mall.sh ${@:1:$#-1} push
}

push "${@}"