#!/usr/bin/env python2
from os import walk
from subprocess import call

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
			out += appendToJS(root + "/" + f)
		for d in dirs:
			out += walkDirectory(d)

	return out


# Setup files that must be appended first or last to a file to make it work.
# Not required now.

# Write our middle files
js = ""
dirsToWalk = ("core", "export")
for dir in dirsToWalk:
	js += walkDirectory(dir)

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
