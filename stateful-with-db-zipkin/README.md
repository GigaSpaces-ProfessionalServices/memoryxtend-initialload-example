
## Requirements

 * Maven
 * JDK
 
## Getting Started
This package contains a script (demo-start.sh) that automatically builds and deploys GigaSpaces with both a Mirror and a Space. The Space is configured to initially load data from the configured data source, so you can explore the functionality of the GigaSpaces product and build applications on top of it.

## Configurations
1. Update Mirror database configurations in /memoryxtend-initialload-example/stateful-with-db-zipkin/scripts/pu.properties file like db.url, db.username, db.password
2. Update Space, Mirror, Memoryxtend, Hibernate, Db properties, main-path = GS_HOME Path in my-app-values.yaml file
3. Update GS_HOME and COMMAND_HOST in /memoryxtend-initialload-example/stateful-with-db-zipkin/scripts/my-app-env.sh file
4. For MEMORYXTEND Update export MEMORYXTEND="true" in /memoryxtend-initialload-example/stateful-with-db-zipkin/scripts/my-app-env.sh file
5. Run mvn clean install under /memoryxtend-initialload-example/stateful-with-db-zipkin folder
6. Run ./demo-start.sh script will start GSC and Deploy Space and Mirror
7. For feeder related properties like DEFAULT_NUM_OBJECTS and BATCH_SIZE update in /memoryxtend-initialload-example/stateful-with-db-zipkin/feeder/src/main/java/com/mycompany/app/MultithreadedFeeder.java file 
8. Run mvn clean install under /memoryxtend-initialload-example/stateful-with-db-zipkin/feeder folder 
9. Run ./feeder.sh script
10. Check the feeder output logs in that you will be able to see these below line which shows the total time to write 100000 entries is 2.909 in sec and Average time to write 1  1 entries is 0.034 in sec same for read also
    2024-11-05 14:58:10,946 INFO [com.mycompany.app.MultithreadedFeeder] - Total time to write 100000 entries is 2.909 in sec
    2024-11-05 14:58:10,946 INFO [com.mycompany.app.MultithreadedFeeder] - Average time to write 1 entries is 0.034 in sec
    2024-11-05 14:59:40,946 INFO [com.mycompany.app.MultithreadedFeeder] - Total time to Read 100000 entries is 2.132 in sec
    2024-11-05 14:59:40,946 INFO [com.mycompany.app.MultithreadedFeeder] - Average time to Read 1 entries is 0.046 in sec

## Note For to deploy Space without MEMORYXTEND update export MEMORYXTEND="false" in /memoryxtend-initialload-example/stateful-with-db-zipkin/scripts/my-app-env.sh file and rest other steps are same as above