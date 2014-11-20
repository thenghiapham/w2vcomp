#!/bin/bash
#$ -S /bin/bash
export MAVEN_OPTS=-Xmx512m
MAIN_CLASS=$1
shift
PROJECT_DIR=$1/w2vcomp/w2vcomp
#shift
cd $PROJECT_DIR
PARAMS="$@"
echo "Starting $MAIN_CLASS"
mvn exec:java -Djava.library.path="/usr/local/lib:$HOME/.local/lib" -Dexec.mainClass="$MAIN_CLASS" -Dexec.args="$PARAMS"
echo "Done"
