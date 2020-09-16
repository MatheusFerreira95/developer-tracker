#!/bin/bash

path=$1
truckfactorFolderPath=$2

# cd $path

awk -F$'\t' -f log.awk $path/log.log > $path/commitfileinfo.log