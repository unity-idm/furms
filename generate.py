#!/usr/bin/python3

from os import listdir
from os.path import isdir, join
from audioop import reverse

def sortKey(version):
    verItems = version.split(".")
    suffix = "z"
    if "-" in verItems[2]:
        lastPart = verItems[2].split("-")
        verItems[2] = lastPart[0]
        suffix = lastPart[1]
    retKey = '{:04d}-{:04d}-{:04d}-{}'.format(int(verItems[0]), int(verItems[1]), int(verItems[2]), suffix)
    print(retKey)
    return retKey

manualDirs = [f for f in listdir("ver/") if isdir(join("ver/", f))]
manualDirs.sort(key=sortKey, reverse=True)


def printEntries(targetFile, manualFileName):
    for manualDir in manualDirs:
        targetFile.write("* [" + manualDir + "](ver/" + manualDir + "/" + manualFileName + ")\n")

def printFile(filename, manualFileName):        
    devops = open(filename, 'w')
    devops.write("[back](index)\n")
    devops.write("## [LATEST](ver/" + manualDirs[0] + "/" + manualFileName + ")\n")
    devops.write("## All versions\n")
    printEntries(devops, manualFileName)
    devops.close()


printFile("devops.md", "sysadmin-manual.html")
printFile("user.md", "user-manual.html")
printFile("rest-api.md", "openapi-static.html")