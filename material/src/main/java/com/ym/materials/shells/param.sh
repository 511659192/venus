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
}

func C++ http://wow.duowan.com