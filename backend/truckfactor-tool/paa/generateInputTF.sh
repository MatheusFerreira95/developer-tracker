#!/bin/bash

path=$1

cd $path

for file in $(git ls-files ./)
do
    echo "FILE: $file"
    git log -n 1 --pretty=format:%an -- "$file"
    echo ""
done