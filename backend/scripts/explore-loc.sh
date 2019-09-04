#!/bin/bash

for file in $(git ls-files $1)
do
    echo "developer-tracker-start-read-info-file"
    git blame --line-porcelain $file | sed -n 's/author //p' | sort | uniq -c | sort -rn;
    echo "developer-tracker-stop-read-info-file"
    echo $file
done
