#!/bin/bash

path=$1

cd $path

for file in $(git ls-files ./)
do
    echo "FILE: $file"
    git blame --line-porcelain "$file" | sed -n 's/author //p' | uniq
done