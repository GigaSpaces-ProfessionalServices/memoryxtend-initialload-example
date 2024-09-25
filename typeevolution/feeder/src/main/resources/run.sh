#!/usr/bin/env bash

set -x
JAVA_HOME="/usr/lib/jvm/java-11-openjdk-amd64"

GIGA="/home/dixson/work/gs/smart/gigaspaces-smart-dih-enterprise-16.2.1"

PROJ_DIR="/home/dixson/work/project/39/typeevolution"

export CLASSES_DIR="$PROJ_DIR/feeder/target/classes"

export MODEL_DIR="$PROJ_DIR/model/target/classes"

export CLASSPATH=$GIGA/lib/required/*

export CLASSPATH=$CLASSPATH:$MODEL_DIR

export CLASSPATH=$CLASSPATH:$CLASSES_DIR

export GS_LOOKUP_LOCATORS="localhost"
export GS_LOOKUP_GROUPS="xap-16.2.1"

$JAVA_HOME/bin/java -Xms1g -Xmx1g -classpath "$CLASSPATH" com.samples.feeder.Feeder
