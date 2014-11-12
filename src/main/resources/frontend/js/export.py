#!/usr/bin/env python2
from os import walk
from subprocess import call

appendFirstRoots = []
appendFirstFiles = []
appendLastRoots = []
appendLastFiles = []

def addToAppendFirst(root, filename):
	appendFirstRoots.append(root)
	appendFirstFiles.append(filename)

def addToAppendLast(root, filename):
	appendLastRoots.append(root)
	appendLastFiles.append(filename)

def isInSpecificList(root, filename):
	return ((any(root in s for s in appendFirstRoots) and
		any(filename in s for s in appendFirstFiles)) or
		((any(root in s for s in appendLastRoots) and
		any(filename in s for s in appendLastFiles))))

def appendToJS(filename):
	out = ""
	file = open(filename)
	for line in file:
		out += line

	file.close()
	return out

def walkDirectory(dirName):
	out = ""
	for root, dirs, files in walk(dirName):
		for f in files:
			if isInSpecificList(root, f) != True:
				out += appendToJS(root + "/" + f)
		for d in dirs:
			out += walkDirectory(d)

	return out

def first():
	out = ""
	for i in range(0, len(appendFirstRoots)):
		out += appendToJS(appendFirstRoots[i] + "/" + appendFirstFiles[i])
	return out

def last():
	out = ""
	for i in range(0, len(appendLastRoots)):
		out += appendToJS(appendLastRoots[i] + "/" + appendLastFiles[i])
	return out

# Setup files that must be appended first or last to a file to make it work.
# Not required now.

# Write the first files to the js string
js = first()

# Write our middle files
dirsToWalk = ("core", "export")
for dir in dirsToWalk:
	js += walkDirectory(dir)

# Write our last files
js += last()

# Open our export.js file and write to it.
file = open("export.js", 'w')

# Write the actual javascript content.
file.write(js)
file.close()

try:
	call(["uglifyjs", "-o", "export.js", "export.js"])
	print("File export.js has been updated.\n")
except Exception:
	# Print some instructions to the user to finish minifying.
	print("Use http://jscompress.com/ to minify")
