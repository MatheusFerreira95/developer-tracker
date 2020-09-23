#!/bin/bash

path=$1
truckfactorFolderPath=$2
#${PWD}
#clear
now=$(date)
echo -e $now: BEGIN git log extraction: $path \\n 

cd $path

git config diff.renameLimit 999999 

#Extract commit information
git log --pretty=format:"%H-;-%aN-;-%aE-;-%at-;-%cN-;-%cE-;-%ct-;-%f"  > commitinfo.log

#Extract and format commit files information
git log --name-status --pretty=format:"commit	%H" --find-renames > $path/log.log
#awk -F$'\t' -f $truckfactorFolderPath/log.awk $path/log.log > $path/commitfileinfo.log

#Get current file list
git ls-files > filelist.log

#Remove temp file
#rm log.log

git config --unset diff.renameLimit


now=$(date)
echo -e "Log files (commitinfo.log, commitfileinfo.log and filelist.log) were generated in $path folder:  \\n"
echo -e $now: END git log extraction: $path \\n 
