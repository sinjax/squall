squall
======

An implementation of Rete for Jena Rules and SPARQL on the Storm distributed stream processing framework.

Details to follow!

Installation
============

squall is a java project which can be built using maven.

To prepare the project for development please use:

	mvn install eclipse:eclipse

To build the tool used to construct storm topologies for Jena rules and SPARQL queries try:

	mvn install
	tools/ReteStormTool/retestorm

This will compile the tool's requirements and prepare the tool to be ran. Running the tool gives documentation for its use