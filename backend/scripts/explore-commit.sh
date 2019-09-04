#!/bin/bash

for file in $(git ls-files $1)
do
    echo "developer-tracker-start-read-info-file"
    git log --pretty=format:"%an" --full-history --follow $file | uniq -c | sort -nr;
    echo "developer-tracker-stop-read-info-file"
    echo $file
done
