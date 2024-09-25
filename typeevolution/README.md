

Prerequisites
1. GigaSpaces version 16.2.1

Steps

1. ./gs.sh host run-agent --manager --webui --gsc=4

2. ./gs.sh pu deploy demo space/target/space-1.0-SNAPSHOT.jar

3. Copy runConfigurations directory to .idea directory

4. Run the Feeder runConfiguration from Intellij

5. Shutdown GigaSpaces.

6. Extract the contents pu-common-after-type-change-1.0-SNAPSHOT.jar to PU_COMMON directory
TODO: doesn't need the lib subdirectory. I will fix this.

7. Start Gigaspaces. See step 1.

8. Deploy space pu. See step 2.

9. Run the TypeEvolutionExample runConfiguration from Intellj
TODO package code with the space.
