#!/bin/bash
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
pushd $DIR > /dev/null
java -cp $DIR/src/main/resources:`mvn -o -q dependency:build-classpath -Dmdep.outputFile=/dev/stdout`:./target/SquallTool-1.0.0-SNAPSHOT.jar org.openimaj.squall.tool.SquallTool $*
popd > /dev/null
