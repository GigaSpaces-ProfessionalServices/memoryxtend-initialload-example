#!/usr/bin/env bash

set -x

#PROJ_DIR="/home/sushil/Sushil/Projects/git/memoryxtend-initialload-example/stateful-with-db"
PROJ_DIR="/home/ubuntu/deploy/initialloadexample/stateful-with-db"

export GS_LOOKUP_LOCATORS="ip-10-0-0-39.us-west-2.compute.internal:4174"
export GS_LOOKUP_GROUPS="xap-16.2.1"

java -jar ../feeder-zipkin/target/feeder-zipkin-0.1-jar-with-dependencies.jar \
  --max_objects=70000


#java -jar ../feeder/target/feeder-0.1-jar-with-dependencies.jar space
