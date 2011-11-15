#!/bin/bash

PAGE_NBR=5000

if [ -z "$1" ]; then
    COMMAND="go"
else
    COMMAND="$1"
fi



case $COMMAND in
clean)
    echo
    echo "Cleaning generated pages"
    rm -drf ../public/loadtest
    mkdir ../public/loadtest
    ;;
*)
    echo
    echo "Generating $PAGE_NBR pages"
    for i in $(eval echo {1..$PAGE_NBR})
    do
        cp ../public/homepage.html ../public/loadtest/homepage$i.html
        cp ../public/homepage.htm ../public/loadtest/homepage$i.htm
    done
    ;;
esac


