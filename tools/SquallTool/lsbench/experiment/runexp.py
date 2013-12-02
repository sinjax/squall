#!/usr/bin/env python
import subprocess
import os
from subprocess import PIPE
import re
from optparse import OptionParser
import time
import xml.etree.ElementTree as ET
import urllib2
from IPython import embed
import simplejson
stormlist = ["storm","list"]


def topologyRunning(top):
	a = subprocess.Popen(stormlist,stdout=PIPE).communicate()
	realOutput = [x for x in a[0].strip().split("\n") if not "main" in x]
	return any([top in x for x in realOutput])
def topologyActive(top):
	a = subprocess.Popen(stormlist,stdout=PIPE).communicate()
	realOutput = [x for x in a[0].strip().split("\n") if not "main" in x]
	return any([top in x and "ACTIVE" in x for x in realOutput])

def killtop(top):
	stormkill = ["storm", "kill", top]
	subprocess.Popen(stormkill,stdout=PIPE,stderr=PIPE).communicate()
	return

def killandwait(top):
	print "Killing topology"
	killtop(top)
	while topologyRunning(top):
		print "Waiting for previous topology to die"
		time.sleep(5)

def kstatswait(output):
	kstats = ["kstats", "-q", output]
	statstr = subprocess.Popen(kstats,stdout=PIPE).communicate()[0]
	while statstr == "":
		statstr = subprocess.Popen(kstats,stdout=PIPE).communicate()[0]
		time.sleep(5)
	simplejson.loads(statstr)
	stats = simplejson.loads(statstr)
	return stats

def monitor(monitorfile, inputs,output,monitorTime,waitTime):
	with open(monitorfile, "a") as mfile:
		startTime = int(time.time())
		endTime = int(time.time()) + monitorTime
		def inoutitems():
			inputStats = [kstatswait(x) for x in inputs]
			outputStat = kstatswait(output)
			inputItems = sum([i['items'] for i in inputStats])
			outputItems = outputStat["items"]
			return inputItems,outputItems
		inputStart,outputStart = inoutitems()

		mfile.write("")
		print "Capturing state!"
		while int(time.time()) < endTime:
			inputCount,outputCount = inoutitems()
			inputsRead = inputStart-inputCount
			outputsWritten = outputCount-outputStart
			taken = int(time.time()) - startTime
			data = {
				"time": taken,
				"input": inputsRead,
				"inputRate": float(inputsRead)/float(taken),
				"output":outputsWritten,
				"outputRate": float(outputsWritten)/float(taken),
			}
			print data
			mfile.write(simplejson.dumps(data))
			mfile.write("\n")
			time.sleep(waitTime)

def experiment(**xargs):
	xargs["monitordir"] = os.sep.join([
		xargs["monitorroot"],
		"monitorTime=%d"%xargs["monitorTime"],
		"wcap=%d"%xargs["wcap"],
		"wtime=%d"%xargs["wtime"]
	])
	print "Performing experiment: %s"%xargs["monitordir"]
	if not os.path.exists(xargs["monitordir"]): os.makedirs(xargs["monitordir"])
	xargs["monitorfile"] = os.sep.join([xargs["monitordir"],"monitor.out"])

	xml = urllib2.urlopen(xargs["rifrule"])
	root = ET.fromstring(xml.read())
	inputQueues = [
		y for y in [
			urllib2.urlparse.urlparse(x.getchildren()[0].text).path.replace("/","") 
			for x in root.findall(".//directive/Import")
		] 
		if "lsbench-" in y
	]

	toolrun = ["../../stormtool.sh",
		"-bm", "STORM", 
			"-stm", "CLUSTER", 
			"-twork", "4",
			"-tname", xargs["topname"],
		"-o", "KESTREL",  
			"-kh", "kestrel://localhost/%s?predelete=true&writecache=1000"%xargs["outputQueue"], 
		"-pm", "GREEDYCS",  
			"-wcap", "%d"%xargs["wcap"], 
			"-wtimeu", "SECONDS",  
			"-wtime", "%d"%xargs["wtime"], 
		"-tm", "RIF",  
			"-rifr", xargs["rifrule"]
	]
	# Run the cluster until the output queue gets something
	outerr = subprocess.Popen(toolrun, stdout=PIPE,stderr=PIPE).communicate()
	if outerr[1] != "":
		print "Error starting experiment!"
		exit()
	while not topologyActive(xargs["topname"]):
		print "Waiting for topology activation"

	print "Topology active, running experiment"
	# periodically measure the size of various queues using kstats -q

	print "Waiting for first output"
	waitstart = int(time.time())
	kstatswait(xargs["outputQueue"])
	waitend = int(time.time())

	print "Got first output (Took: %ds), monitoring output..."%(waitend - waitstart)

	monitor(xargs["monitorfile"],inputQueues,xargs["outputQueue"],xargs.get("monitorTime",60),xargs.get("sample",5))

def runExperiment(**xargs):
	killandwait(xargs["topname"])
	print "Starting experiment!"
	try:
		experiment(**xargs)
		print "Experiment done!"
	except Exception, e:
		print "Experiment failed!"
		print e
	finally:
		pass

	

	killandwait(xargs["topname"])

if __name__ == '__main__':
	runExperiment(
		topname="kestrel-test", 
		wcap=100, 
		wtime=10, 
		sample=5,
		monitorTime=60*5,
		rifrule="file:///home/ss/Development/java/squall/tools/SquallTool/lsbench/query/kestrelqueriespre.rif",
		outputQueue="output-two",
		monitorroot="monitor"
	)





