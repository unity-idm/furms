#!/usr/bin/env python

from os import listdir
from os.path import isdir, join
from audioop import reverse

def sortKey(version):
    verItems = version.split(".")
    return int(verItems[0])*10000 + int(verItems[1])*100 + int(verItems[2])

manualDirs = [f for f in listdir("ver/") if isdir(join("ver/", f))]
manualDirs.sort(key=sortKey, reverse=True)


def printEntries(targetFile, manualFileName):
    for manualDir in manualDirs:
        targetFile.write("[" + manualDir + "](ver/" + manualDir + "/" + manualFileName + ")\n")

def printFile(filename, manualFileName):        
    devops = open(filename, 'w')
    devops.write("[back](index)\n")
    devops.write("## [LATEST](ver/" + manualDirs[0] + "/" + manualFileName + ")\n")
    devops.write("## All versions\n")
    printEntries(devops, manualFileName)
    devops.write("[back](index)\n")
    devops.close()


printFile("devops.md", "sysadmin-manual.html")
printFile("user.md", "user-manual.html")