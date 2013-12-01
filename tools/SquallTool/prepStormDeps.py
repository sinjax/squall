#!/usr/bin/env python
import subprocess
import os
from subprocess import PIPE
import re
from optparse import OptionParser

parser = OptionParser()
parser.add_option("-d", "--dry-run", action="store_true", dest="dryrun",
                  help="Whether to do a dry run", metavar="BOOL")
parser.add_option("-o", "--overwrite", action="store_true", dest="overwrite",
                  help="Whether to overwrite exact matches", metavar="BOOL")
(options, args) = parser.parse_args()

squaldepscmd = ["mvn","-o","-q","dependency:build-classpath","-Dmdep.outputFile=/dev/stdout"]
stormdepscmd = ["/usr/local/storm/bin/storm","classpath"]
stormlib = "/usr/local/storm/lib"

squaldepsstr = subprocess.Popen(squaldepscmd, stdout=PIPE).communicate()[0]
stormdepsstr = subprocess.Popen(stormdepscmd, stdout=PIPE).communicate()[0]

squaldeps = squaldepsstr.split(":")
stormdeps = stormdepsstr.split(":")

stormdepjars = [os.path.basename(x) for x in stormdeps]
squalldepjars = dict([(os.path.basename(x),x) for x in squaldeps])

finaldeps = []

namereg = re.compile(r'(.+?)([0-9].*|$)')

stormdepnames = dict([(namereg.match(x).groups()[0],x) for x in stormdepjars])


for jar,path in squalldepjars.items():
	partial = namereg.match(jar).groups()[0]
	if partial == "storm-":
		print "Found storm jar, skipping"
		continue
	if jar in stormdepjars:
		print "EXACT found, skipping: %s"%jar
		if not options.overwrite:
			continue
	elif partial in stormdepnames:
		print "PARTIAL found, skipping: %s(oi) vs %s(strm)"%(jar,stormdepnames[partial])
		continue
	if not options.dryrun:
		print "copying: %s"%jar
		copycmd = ["sudo","cp","-rfv",path,stormlib]
		subprocess.Popen(copycmd).communicate()