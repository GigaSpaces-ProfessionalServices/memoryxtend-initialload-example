

Prerequisites
1. GigaSpaces version 16.2.1

Steps - Option 1
1. ./gs.sh host run-agent --manager --webui --gsc=2

2. ./gs.sh pu deploy demo space/target/space-1.0-SNAPSHOT.jar

3. Copy runConfigurations directory to .idea directory

4. Run the Feeder runConfiguration from Intellij

5. Shutdown GigaSpaces.

6. Extract the contents pu-common-after-type-change-1.0-SNAPSHOT.jar to PU_COMMON directory

7. Start Gigaspaces. See step 1.

8. Deploy space pu.
./gs.sh pu deploy demo spacewithbean/target/spacewithbean-1.0-SNAPSHOT.jar




Steps - Option 2
1. ./gs.sh host run-agent --manager --webui --gsc=2

2. ./gs.sh pu deploy demo space/target/space-1.0-SNAPSHOT.jar

3. Copy runConfigurations directory to .idea directory

4. Run the Feeder runConfiguration from Intellij

5. Shutdown GigaSpaces.

6. Extract the contents pu-common-after-type-change-1.0-SNAPSHOT.jar to PU_COMMON directory

7. Start Gigaspaces. See step 1.

8. Deploy space pu. See step 2.

9. Run the TypeEvolutionExample runConfiguration from Intellj
