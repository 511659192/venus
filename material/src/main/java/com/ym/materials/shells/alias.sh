
export feature=features/v1.6
alias work='op(){gitm $* co $feature;};op'
alias dev='op(){gitm $* co develop;};op'
alias qa='op(){gitm $* co qa;};op'
alias master='op(){gitm $* co master;};op'
alias pull='op(){gitm $* pull;};op'
alias push='op(){cnt=$#;cnt=$(($cnt-1));echo $cnt;gitm ${@:1:$cnt} add .;gitm ${@:1:$cnt} ci -m ${!#};gitm ${@:1:$cnt} push};op'
alias mergeToDevelop='op(){echo "-------------------checkout-------------------";gitm $* co develop;echo "-------------------merge-------------------";gitm $* merge $feature};op'
alias mergeToQa='op(){echo "-------------------checkout-------------------";gitm $* co qa;echo "-------------------merge-------------------";gitm $* merge $feature};op'

