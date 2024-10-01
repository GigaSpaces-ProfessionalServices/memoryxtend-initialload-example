# Sample program to demonstrate type evolution

## Prerequisites
1. GigaSpaces version 16.2.1

## Description
When MemoryXtend is used, objects are loaded as they were last written. In some business use cases there may be a need to modify a type, for example add or modify a property. The following examples demonstrate how the old type can be converted to the new type.

## Steps - Option 1
Uses a bean that runs after the space is a started.
2. ./gs.sh host run-agent --manager --webui --gsc=2

3. ./gs.sh pu deploy demo space/target/space-1.0-SNAPSHOT.jar

4. Copy runConfigurations directory to .idea directory

5. Run the Feeder runConfiguration from Intellij

6. Shutdown GigaSpaces.

7. Extract the contents pu-common-after-type-change-1.0-SNAPSHOT.jar to PU_COMMON directory

8. Start Gigaspaces. See step 1.

9. Deploy space pu.
./gs.sh pu deploy demo spacewithbean/target/spacewithbean-1.0-SNAPSHOT.jar

10. After the space starts, the TypeEvolution bean runs. The old type should have been converted to a new type.


## Steps - Option 2
Uses a distributed task.

1. ./gs.sh host run-agent --manager --webui --gsc=2

2. ./gs.sh pu deploy demo space/target/space-1.0-SNAPSHOT.jar

3. Copy runConfigurations directory to .idea directory

4. Run the Feeder runConfiguration from Intellij

5. Shutdown GigaSpaces.

6. Extract the contents pu-common-after-type-change-1.0-SNAPSHOT.jar to PU_COMMON directory

7. Start Gigaspaces. See step 1.

8. Deploy space pu. See step 2.

9. Run the TypeEvolutionExample runConfiguration from Intellj
