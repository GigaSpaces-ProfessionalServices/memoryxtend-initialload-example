
## Requirements

 * Maven
 * JDK
 
## Getting Started
This package contains a script (demo-start.sh) that automatically builds and deploys GigaSpaces with both a Mirror and a Space. The Space is configured to initially load data from the configured data source, so you can explore the functionality of the GigaSpaces product and build applications on top of it.

## Space code with xml configurations
Space package contains code with memoryXtend   (for this keep MEMORYXTEND=true in my-app-env.sh)
space_without_memoryextend package contains code without memoryXtend (for this keep MEMORYXTEND=false in my-app-env.sh)

### Running demo-start.sh
This script does the following:

1. Builds the project.
2. Starts the service grid.
3. Deploys the Processing Units (space and mirror).

### Running feeder.sh
It will update 100k records to Space in the batch of 1k
If you want to change the above number so update these DEFAULT_NUM_OBJECTS and BATCH_SIZE paramters in MultithreadedFeeder file in feeder module