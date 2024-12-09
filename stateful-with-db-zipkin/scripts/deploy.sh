#!/usr/bin/env bash

set -x

. ./my-app-env.sh

if [ "$MEMORYXTEND" == "true" ]; then
  echo "MEMORYXTEND False using pu.properties"
  JAR_FILENAME="../space-zipkin/target/space-zipkin-0.1.jar"
  PROP_FILENAME="pu.properties"
else
  echo "MEMORYXTEND False using pu.properties"
  JAR_FILENAME="../space_without_memoryXtend-zipkin/target/space_without_memoryXtend-zipkin-0.1.jar"
  PROP_FILENAME="pu.properties"
fi

if [ "true" == "$SPACE_HA" ]; then
  echo "Deploying space with ha..."
  if [ ! -z "$MAX_INSTANCES_PER_MACHINE" ]; then
    $GS_HOME/bin/gs.sh pu deploy --properties=$PROP_FILENAME --zones=space --partitions=${SPACE_PARTITIONS} --ha --max-instances-per-machine=$MAX_INSTANCES_PER_MACHINE space $JAR_FILENAME
  else
    $GS_HOME/bin/gs.sh pu deploy --properties=$PROP_FILENAME --zones=space --partitions=${SPACE_PARTITIONS} --ha space $JAR_FILENAME
  fi
else
  echo "Deploying space..."
  $GS_HOME/bin/gs.sh pu deploy --properties=$PROP_FILENAME --zones=space --partitions=${SPACE_PARTITIONS}        space $JAR_FILENAME
fi

if [ "true" == "$MIRRORED" ]; then
  echo "Deploying mirror..."
  $GS_HOME/bin/gs.sh pu deploy --properties=my-app-values.yaml \
    --zones=mirror --instances=1 -property=space.partitions=${SPACE_PARTITIONS} \
    mirror ../mirror/target/mirror-0.1.jar
fi

set +x
