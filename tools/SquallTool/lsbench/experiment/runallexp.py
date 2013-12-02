import runexp

for wtime in range(10,60,10):
	for wcap in range(50,500,50):
		runexp.runExperiment(
			topname="kestrel-test", 
			wcap=wcap, 
			wtime=wtime, 
			sample=5,
			monitorTime=60*5,
			rifrule="file:///home/ss/Development/java/squall/tools/SquallTool/lsbench/query/kestrelqueriespre.rif",
			outputQueue="testoutput",
			monitorroot="monitor-exhaustive"
		)