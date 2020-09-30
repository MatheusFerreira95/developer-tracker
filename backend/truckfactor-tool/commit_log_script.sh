#!/bin/bash

git config diff.renameLimit 999999 
#Extract commit information
git log --pretty=format:"%H-;-%aN-;-%aE-;-%at-;-%cN-;-%cE-;-%ct-;-%f"  > commitinfo.log
#Extract and format commit files information
git log --name-status --pretty=format:"commit	%H" --find-renames > log.log
#Get current file list
git ls-files > filelist.log
git config --unset diff.renameLimit