## Requirements

 * Maven
 * JDK

1. Builds the project.
2. Starts the service grid.
3. Deploy the Processing Units (Space with Memory Extend).

## Getting Started
Use following command with RocksDbUtility Jar and Model Class Jar
- For Without Write data to file use : java -cp <RocksDbUtility.jar>:<Model_Class.jar> com.gigaspaces.Main WRITE_DATA_TO_FILE <baseRocksDBWorkPath> <spaceName> <partition> Nolease
- For lease Write data to file use : java -cp <RocksDbUtility.jar>:<Model_Class.jar> com.gigaspaces.Main WRITE_DATA_TO_FILE <baseRocksDBWorkPath> <spaceName> <partition> lease
- For RockDb list MetaData use : java -cp <RocksDbUtility.jar>:<Model_Class.jar> com.gigaspaces.Main ROCKSDB_LIST_METADATA <baseRocksDBWorkPath> <spaceName> <partition>
- For RockDb list Data use : java -cp <RocksDbUtility.jar>:<Model_Class.jar> com.gigaspaces.Main ROCKSDB_LIST_DATA <baseRocksDBWorkPath> <spaceName> <partition>
- For RockDb Put MetaData use : java -cp <RocksDbUtility.jar>:<Model_Class.jar> com.gigaspaces.Main ROCKSDB_PUT_METADATA <baseRocksDBWorkPath> <spaceName> <partition> <ClassName>
- For RockDb Delete MetaData use : java -cp <RocksDbUtility.jar>:<Model_Class.jar> com.gigaspaces.Main ROCKSDB_DELETE_METADATA <baseRocksDBWorkPath> <spaceName> <partition> <ClassName>
- For RockDb Delete Data use : java -cp <RocksDbUtility.jar>:<Model_Class.jar> com.gigaspaces.Main ROCKSDB_DELETE_DATA <baseRocksDBWorkPath> <spaceName> <partition> <ClassName>