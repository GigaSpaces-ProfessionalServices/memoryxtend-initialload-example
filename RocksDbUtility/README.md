## Requirements

 * Maven
 * JDK

1. Builds the project.
2. Starts the service grid.
3. Deploy the Processing Units (Space with Memory Extend).
4. Run Feeder

## Getting Started
Use following command with RocksDbUtility Jar and Model Class Jar
- Write data Without lease to file use : java -cp <RocksDbUtility.jar>:<Model_Class.jar> com.gigaspaces.Main WRITE_DATA_TO_FILE <baseRocksDBWorkPath> <spaceName> <partition> Nolease
- Write data Without lease to file use : java -cp <RocksDbUtility.jar>:<Model_Class.jar> com.gigaspaces.Main WRITE_DATA_TO_FILE <baseRocksDBWorkPath> <spaceName> <partition> lease
- RockDb list MetaData use : java -cp <RocksDbUtility.jar>:<Model_Class.jar> com.gigaspaces.Main ROCKSDB_LIST_METADATA <baseRocksDBWorkPath> <spaceName> <partition>
- RockDb list Data use : java -cp <RocksDbUtility.jar>:<Model_Class.jar> com.gigaspaces.Main ROCKSDB_LIST_DATA <baseRocksDBWorkPath> <spaceName> <partition>
- RockDb Put MetaData use : java -cp <RocksDbUtility.jar>:<Model_Class.jar> com.gigaspaces.Main ROCKSDB_PUT_METADATA <baseRocksDBWorkPath> <spaceName> <partition> <ClassName>

To Delete types you have to use force parameter at the end of the command
- RockDb Delete MetaData use : java -cp <RocksDbUtility.jar>:<Model_Class.jar> com.gigaspaces.Main ROCKSDB_DELETE_METADATA <baseRocksDBWorkPath> <spaceName> <partition> <ClassName>
- RockDb Delete Data use : java -cp <RocksDbUtility.jar>:<Model_Class.jar> com.gigaspaces.Main ROCKSDB_DELETE_DATA <baseRocksDBWorkPath> <spaceName> <partition> <ClassName>