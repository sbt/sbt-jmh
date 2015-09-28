#!/bin/bash
set -e

if [ "$FLAMES_VERBOSE" == "1" ]; then
  set -x
fi

CUR_DIR=`pwd`
PID=$1
OPTIONS=$2
#ATTACH_JAR=attach-main.jar
#PERF_MAP_DIR=$(dirname $(readlink -f $0))/..
#ATTACH_JAR_PATH=$PERF_MAP_DIR/out/$ATTACH_JAR
# TODO replace with proper injection of jar path
PERF_MAP_DIR=/tmp/sbt-jmh-perf-flames
ATTACH_JAR_PATH=/home/ktoso/.ivy2/local/pl.project13.scala/sbt-jmh-flamegraph-lib_2.10/0.3.0/jars/sbt-jmh-flamegraph-lib_2.10.jar
PERF_MAP_FILE=/tmp/perf-$PID.map

if [ -z $JAVA_HOME ]; then
  JAVA_HOME=/usr/lib/jvm/default-java
fi

[ -d $JAVA_HOME ] || JAVA_HOME=/etc/alternatives/java_sdk
[ -d $JAVA_HOME ] || (echo "JAVA_HOME directory at '$JAVA_HOME' does not exist." && false)

sudo rm $PERF_MAP_FILE -f
(cd $PERF_MAP_DIR/out && java -cp $ATTACH_JAR_PATH:$JAVA_HOME/lib/tools.jar net.virtualvoid.perf.AttachOnce $PID $OPTIONS)
sudo chown root:root $PERF_MAP_FILE
