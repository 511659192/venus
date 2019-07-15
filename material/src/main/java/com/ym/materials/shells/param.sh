#!/usr/bin/env bash

function func() {
    echo "Language: $1"
    echo "Url: $2"
    echo "Process ID: $$"
    echo "File Name: $0"
    echo "First Parameter : $1"
    echo "Second Parameter : $2"
    echo "All parameters 1: $@"
    echo "All parameters 2: $*"
    echo "Total: $#"
    echo "After: ${@:2:2}"
    echo "Before: ${@:2:2}"
    echo "the last param is ${!#}"
    echo "Before: ${@:1:$#-1}"
    echo "Last:${@:$#:1}"
}

func C++ http://wow.duowan.com 1213112 112 adab