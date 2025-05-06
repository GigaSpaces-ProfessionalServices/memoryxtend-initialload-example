package com.gigaspaces;

import com.gigaspaces.internal.metadata.TypeDesc;
import com.gigaspaces.internal.version.PlatformLogicalVersion;
import com.gigaspaces.metadata.SpaceTypeDescriptor;
import com.gigaspaces.metadata.SpaceTypeDescriptorBuilder;
import com.j_spaces.core.cache.blobStore.sadapter.BlobStoreStorageAdapterClassInfo;
import com.j_spaces.core.cache.blobStore.sadapter.BlobStoreTypeDescSerializable;
import org.rocksdb.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {
    public static void main(String[] args) throws RocksDBException, IOException, ClassNotFoundException {
        List ValidActionParamsList = new ArrayList<>();
        Collections.addAll(ValidActionParamsList, "WRITE_DATA_TO_FILE", "ROCKSDB_LIST_METADATA", "ROCKSDB_LIST_DATA",
                "ROCKSDB_PUT_METADATA", "ROCKSDB_DELETE_METADATA", "ROCKSDB_DELETE_DATA");
        String actionParam = "";
        String className = "";
        String basePath = "";
        String modelJarPath = "";
        String spaceName = "";
        String partition = "";
        String isLease = "";
        Boolean forceDelete = false;

        if (args.length < 3) {
            printErrorArgs();
        } else {
            actionParam = args[0];
            if (!ValidActionParamsList.contains(actionParam)) {
                System.out.println("Choose an Valid Action. Options are - " + ValidActionParamsList);
            }
            basePath = args[1];
            spaceName = args[2];
            partition = args[3];
            if (args.length < 4) {
                printErrorArgs();
            } else {
                if (actionParam.equals("WRITE_DATA_TO_FILE")) {
                    isLease = args[4];
                } else if (actionParam.equals("ROCKSDB_PUT_METADATA") || actionParam.equals("ROCKSDB_DELETE_METADATA") || actionParam.equals("ROCKSDB_DELETE_DATA")) {
                    className = args[4];
                    if (actionParam.equals("ROCKSDB_DELETE_METADATA") || actionParam.equals("ROCKSDB_DELETE_DATA")) {
                        if (args.length > 5 && args[5].equals("force")) {
                            forceDelete = true;
                        }
                    }
                }
            }
        }
        // Static block to load the RocksDB native library

//        String basePath = "/home/sushil/Sushil/gigaspaces/gigaspaces-xap-enterprise-16.2.1/work/memoryxtend/rocksdb";
//        String spaceName = "mySpace62";
//        String partition = "1";
//        String isLease = "lease";
//        String ClassNameA = "com.space.ClassA";
//        String ClassNameC = "com.space.ClassC";
//        String ClassNameD = "com.space.ClassD";
//        String ClassNameE = "com.space.ClassE";
//        String ClassNameF = "com.space.ClassF";
//        String ClassNameI = "com.space.ClassI";
//        int MetaDataHandleValue = 3;
//        int DATAHandleValue = 1;


        RocksDB.loadLibrary();
        //final String dbPath = "/home/jay/work/gigaspace/gigaspaces-insightedge-versions/gigaspaces-xap-enterprise-16.2.1/work/memoryxtend/rocksdb/demo_container1-demo/demo_container1-demo";


        for (int partitionNo = 1; partitionNo <= Integer.parseInt(partition); partitionNo++) {
            String _partition = String.valueOf(partitionNo);
            final String dbPath = basePath + "/" + spaceName + "_container" + _partition + "-" + spaceName + "/" + spaceName + "_container" + _partition + "-" + spaceName;
            System.out.println("\n\n\nRocksDbPath - " + dbPath);

            final List<ColumnFamilyHandle> columnFamilyHandleList = new ArrayList<>();
            HashMap<String, Long> typesCount = new HashMap<>();
            HashMap<String, Long> typesExpiration = new HashMap<>();
            List<ColumnFamilyDescriptor> cfDescriptors = Arrays.asList(
                    new ColumnFamilyDescriptor("default".getBytes()),
                    new ColumnFamilyDescriptor("DATA".getBytes()),
                    new ColumnFamilyDescriptor("SYNC".getBytes()),
                    new ColumnFamilyDescriptor("METADATA".getBytes()),
                    new ColumnFamilyDescriptor("ADMIN".getBytes()),
                    new ColumnFamilyDescriptor("SYNC_OVERFLOW".getBytes())
            );

            if (actionParam.equals("WRITE_DATA_TO_FILE")) {
                RocksDbWriteFile(spaceName, _partition, isLease, typesExpiration, typesCount, dbPath, cfDescriptors, columnFamilyHandleList, modelJarPath);

            } else if (actionParam.equals("ROCKSDB_LIST_METADATA")) {
                RocksDBListMetadata(dbPath, cfDescriptors, columnFamilyHandleList);

            } else if (actionParam.equals("ROCKSDB_LIST_DATA")) {
                HashMap<String, Long> rocksDbListDataMap = RocksDbListData(typesCount, typesExpiration, dbPath, cfDescriptors, columnFamilyHandleList);

                int rocksDbListDataMapcount = 1;
                for (Map.Entry<String, Long> entry : rocksDbListDataMap.entrySet()) {
                    System.out.println(rocksDbListDataMapcount + " - " + entry.getKey() + " = " + entry.getValue());
                    rocksDbListDataMapcount++;
                }
                System.out.println("RocksDb Data Listed Successfully");


            } else if (actionParam.equals("ROCKSDB_PUT_METADATA")) {
                RocksDBPutMetadata(className, dbPath, cfDescriptors, columnFamilyHandleList);

            } else if (actionParam.equals("ROCKSDB_DELETE_METADATA")) {
                RocksDBDeleteMetaDataKey(className, dbPath, cfDescriptors, columnFamilyHandleList, forceDelete);

            } else if (actionParam.equals("ROCKSDB_DELETE_DATA")) {
                RocksDBDeleteDataKey(className, dbPath, cfDescriptors, columnFamilyHandleList, forceDelete);
            }
        }
    }

    public static void printErrorArgs() {
        System.out.println("\nFor Write data to file use : java -cp <RocksDbUtility.jar>:<Model_Class.jar> com.gigaspaces.Main WRITE_DATA_TO_FILE <baseRocksDBWorkPath> <spaceName> <partition> Nolease");
        System.out.println("For lease Write data to file use : java -cp <RocksDbUtility.jar>:<Model_Class.jar> com.gigaspaces.Main WRITE_DATA_TO_FILE <baseRocksDBWorkPath> <spaceName> <partition> lease");
        System.out.println("For RockDb list MetaData use : java -cp <RocksDbUtility.jar>:<Model_Class.jar> com.gigaspaces.Main ROCKSDB_LIST_METADATA <baseRocksDBWorkPath> <spaceName> <partition>");
        System.out.println("For RockDb list Data use : java -cp <RocksDbUtility.jar>:<Model_Class.jar> com.gigaspaces.Main ROCKSDB_LIST_DATA <baseRocksDBWorkPath> <spaceName> <partition>");
        System.out.println("For RockDb Put MetaData use : java -cp <RocksDbUtility.jar>:<Model_Class.jar> com.gigaspaces.Main ROCKSDB_PUT_METADATA <baseRocksDBWorkPath> <spaceName> <partition> <ClassName>");
        System.out.println("For RockDb Delete MetaData use : java -cp <RocksDbUtility.jar>:<Model_Class.jar> com.gigaspaces.Main ROCKSDB_DELETE_METADATA <baseRocksDBWorkPath> <spaceName> <partition> <ClassName>");
        System.out.println("For RockDb Delete Data use : java -cp <RocksDbUtility.jar>:<Model_Class.jar> com.gigaspaces.Main ROCKSDB_DELETE_DATA <baseRocksDBWorkPath> <spaceName> <partition> <ClassName>");

        System.out.println("Eg :  java -jar RocksDbUtility.jar \"/work/gigaspace/gigaspaces-insightedge-versions/gigaspaces-xap-enterprise-16.2.1/work/memoryxtend/rocksdb/\" \"demo\" 1 \"lease\"");
        System.out.println("invalid args");
        System.exit(1);
    }

    private static Serializable deserializeObject(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (Serializable) ois.readObject(); // Deserialize here
        }
    }

    private static byte[] serializeObject(Serializable obj) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
            return baos.toByteArray();
        }
    }

    public static void RocksDBListMetadata(String dbPath, List<ColumnFamilyDescriptor> cfDescriptors, List<ColumnFamilyHandle> columnFamilyHandleList) {
        try (final DBOptions dbOptionsRead = new DBOptions().setCreateIfMissing(true);
             final RocksDB dbList = RocksDB.open(dbOptionsRead, dbPath, cfDescriptors, columnFamilyHandleList)) {

            ColumnFamilyHandle METADATAcfHandle = columnFamilyHandleList.get(3);
            // Create read options
            final ReadOptions readOptions = new ReadOptions();
            final RocksIterator iteratorList = dbList.newIterator(METADATAcfHandle, readOptions);
            // Iterate over all key-value pairs in the column family
            List MetadataList = new ArrayList();
            for (iteratorList.seekToFirst(); iteratorList.isValid(); iteratorList.next()) {
                String key = new String(iteratorList.key());
                MetadataList.add(key);
            }

            int count = 1;
            for (int i = 0; i < MetadataList.size(); i++) {
                System.out.println(count + " - " + MetadataList.get(i));
                count++;
            }

            for (ColumnFamilyHandle handle : columnFamilyHandleList) {
                handle.close();
            }
            dbList.close();
            System.out.println("RocksDb Metadata Listed Successfully");
        } catch (RocksDBException e) {
            System.err.println("RocksDB Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    static HashMap<String, Long> RocksDbListData(HashMap<String, Long> typesCount, HashMap<String, Long> typesExpiration, String dbPath,
                                                 List<ColumnFamilyDescriptor> cfDescriptors, List<ColumnFamilyHandle> columnFamilyHandleList) {
        List<String> rocksDbDataObjectList = new ArrayList<>();
        try (final DBOptions dbOptions = new DBOptions().setCreateIfMissing(true);
             final RocksDB db = RocksDB.open(dbOptions, dbPath, cfDescriptors, columnFamilyHandleList)) {

            // Create iterator for the specific column family
            ColumnFamilyHandle cfHandle = columnFamilyHandleList.get(1);
            final ReadOptions readOptions = new ReadOptions();
            try (final RocksIterator iterator = db.newIterator(cfHandle, readOptions)) {
                // Iterate over all key-value pairs in the column family
                for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
                    String key = new String(iterator.key());
                    String value = new String(iterator.value());
//                            System.out.printf("Key: %s %n", key);
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(iterator.value());
                    ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                    byte flags = objectInputStream.readByte();
                    PlatformLogicalVersion version = new PlatformLogicalVersion();
                    version.read(objectInputStream);
                    String _m_Uid = objectInputStream.readUTF();
                    String _expirationTime = "";

                    if (version.greaterOrEquals(PlatformLogicalVersion.v11_0_0)) {
                        byte embeddedSyncInfoFlags = objectInputStream.readByte();
                        if ((embeddedSyncInfoFlags & (((byte) 1))) == (((byte) 1))) {
                            Object _generationId = objectInputStream.readLong();
                            Object _sequenceId = objectInputStream.readLong();
                        }
                    }
                    Object _blobStoreVersion = objectInputStream.readShort();
                    String _typeName = objectInputStream.readUTF();
                    objectInputStream.readByte();
                    objectInputStream.readLong();
                    if ((flags & (((byte) 1) << 1)) == (((byte) 1) << 1))
                        objectInputStream.readInt();

                    if ((flags & (((byte) 1) << 2)) == (((byte) 1) << 2))
                        objectInputStream.readInt();

                    if ((flags & (((byte) 1) << 3)) == (((byte) 1) << 3))
                        _expirationTime = "" + objectInputStream.readLong();

//                             System.out.println(_typeName);
                    //System.out.println(_expirationTime);
                    typesCount.put(_typeName, typesCount.getOrDefault(_typeName, 0L) + 1L);

                    if (!_expirationTime.isEmpty()) {
                        typesExpiration.put(_typeName, typesExpiration.getOrDefault(_typeName, 0L) + 1L);
                    }
                }

            } catch (Exception e) {
                System.err.println("RocksDB Error: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("RocksDB Error: " + e.getMessage());
            e.printStackTrace();
        }
        return typesCount;
    }

    public static void RocksDbWriteFile(String spaceName, String partition, String isLease, HashMap<String, Long> typesExpiration,
                                        HashMap<String, Long> typesCount, String dbPath, List<ColumnFamilyDescriptor> cfDescriptors,
                                        List<ColumnFamilyHandle> columnFamilyHandleList, String modelJarPath) {
        // Open DB with column families
        try (final DBOptions dbOptions = new DBOptions().setCreateIfMissing(true);
             final RocksDB db = RocksDB.open(dbOptions, dbPath, cfDescriptors, columnFamilyHandleList)) {
            // Get the specific column family handle
            ColumnFamilyHandle METADATAcfHandle = columnFamilyHandleList.get(3);
//            System.out.printf(METADATAcfHandle.toString());

            List<String> MetadataKeyClassNameList = new ArrayList<>();
            List<String> serializableSuperTypesNamesClassNameList = new ArrayList<>();
            HashMap<String, String> CorrespondingTypeMetadataErrorMsg = new HashMap<>();
            // Create read options
            try (final ReadOptions readOptions = new ReadOptions()) {
                try (final RocksIterator iterator = db.newIterator(METADATAcfHandle, readOptions)) {
                    // Iterate over all key-value pairs in the column family
                    for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
                        String key = new String(iterator.key());
//                        System.out.println("key - > " + key);
                        byte[] value = iterator.value();
                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(iterator.value());
                        MetadataKeyClassNameList.add(key);
                        boolean serializableSuperTypesNamesClassNameCheckValue = true;

//                        Supper Class Check Logic
                        Serializable serializable = deserializeObject(value);
                        serializableSuperTypesNamesClassNameList = List.of(((BlobStoreTypeDescSerializable) serializable).getTypeDesc().getSuperClassesNames());
//                        System.out.println("TypeDesc -> " + ((BlobStoreTypeDescSerializable) serializable).getTypeDesc());
//                        System.out.println("serializableSuperTypesNamesClassNameList - > " + serializableSuperTypesNamesClassNameList);

                        for (String superTypeName : serializableSuperTypesNamesClassNameList) {
                            if (!superTypeName.equals("java.lang.Object")) {
                                if (!MetadataKeyClassNameList.contains(superTypeName)) {
                                    serializableSuperTypesNamesClassNameCheckValue = false;
                                    CorrespondingTypeMetadataErrorMsg.put(key, "Error can't create type Description for " + key + ", missing Super Class " + superTypeName);
                                }
                            }
                        }

                        if (serializableSuperTypesNamesClassNameCheckValue) {
                            CorrespondingTypeMetadataErrorMsg.put(key, "");
                        }
                    }

                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                // Create iterator for the specific column family
                ColumnFamilyHandle cfHandle = columnFamilyHandleList.get(1);
                try (final RocksIterator iterator = db.newIterator(cfHandle, readOptions)) {
                    // Iterate over all key-value pairs in the column family
                    for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
                        String key = new String(iterator.key());
                        String value = new String(iterator.value());
//                            System.out.printf("Key: %s %n", key);
                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(iterator.value());
                        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                        byte flags = objectInputStream.readByte();
                        PlatformLogicalVersion version = new PlatformLogicalVersion();
                        version.read(objectInputStream);
                        String _m_Uid = objectInputStream.readUTF();
                        String _expirationTime = "";

                        if (version.greaterOrEquals(PlatformLogicalVersion.v11_0_0)) {
                            byte embeddedSyncInfoFlags = objectInputStream.readByte();
                            if ((embeddedSyncInfoFlags & (((byte) 1))) == (((byte) 1))) {
                                Object _generationId = objectInputStream.readLong();
                                Object _sequenceId = objectInputStream.readLong();
                            }
                        }
                        Object _blobStoreVersion = objectInputStream.readShort();
                        String _typeName = objectInputStream.readUTF();
                        objectInputStream.readByte();
                        objectInputStream.readLong();
                        if ((flags & (((byte) 1) << 1)) == (((byte) 1) << 1))
                            objectInputStream.readInt();

                        if ((flags & (((byte) 1) << 2)) == (((byte) 1) << 2))
                            objectInputStream.readInt();

                        if ((flags & (((byte) 1) << 3)) == (((byte) 1) << 3))
                            _expirationTime = "" + objectInputStream.readLong();

//                             System.out.println(_typeName);
                        //System.out.println(_expirationTime);
                        typesCount.put(_typeName, typesCount.getOrDefault(_typeName, 0L) + 1L);

                        if (!_expirationTime.isEmpty()) {
                            typesExpiration.put(_typeName, typesExpiration.getOrDefault(_typeName, 0L) + 1L);
                        }
                    }


                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            // Clean up column family handles
            for (ColumnFamilyHandle handle : columnFamilyHandleList) {
                handle.close();
            }
            db.close();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String timestamp = sdf.format(new Date());
            String fileName = "output-" + spaceName + "-" + partition + "-" + timestamp + ".txt";

            writeDataToFile(fileName, typesCount, partition, typesExpiration, isLease, CorrespondingTypeMetadataErrorMsg);
            readFileData(fileName);
            // System.out.println(typesCount);

        } catch (RocksDBException | FileNotFoundException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }


    public static void RocksDBPutMetadata(String ClassName, String dbPath, List<ColumnFamilyDescriptor> cfDescriptors,
                                          List<ColumnFamilyHandle> columnFamilyHandleList)
            throws IOException, RocksDBException, ClassNotFoundException {
        try (RocksDB rocksDBPutDb = RocksDB.open(dbPath, cfDescriptors, columnFamilyHandleList);
             ColumnFamilyHandle METADATAcfHandle = columnFamilyHandleList.get(3)) {

//            System.out.println("createClassName -> " + createClassName);
//            if ((rocksDBPutDb.get(METADATAcfHandle, ClassName.getBytes())).equals(null)) {
            SpaceTypeDescriptor typeDescriptorDoc1 = new SpaceTypeDescriptorBuilder(ClassName, null)
                    .create();

            BlobStoreStorageAdapterClassInfo blobStoreStorageAdapterClassInfo = new BlobStoreStorageAdapterClassInfo(new boolean[typeDescriptorDoc1.getNumOfFixedProperties()],
                    null, (short) 0);
            BlobStoreTypeDescSerializable blobStoreTypeDescSerializable = new BlobStoreTypeDescSerializable((TypeDesc) typeDescriptorDoc1,
                    blobStoreStorageAdapterClassInfo);

            rocksDBPutDb.put(METADATAcfHandle, ClassName.getBytes(StandardCharsets.UTF_8), serializeObject(blobStoreTypeDescSerializable));
            System.out.println("Updated Class - " + ClassName + " is Successfully");
//            }

            for (ColumnFamilyHandle handle : columnFamilyHandleList) {
                handle.close();
            }
        } catch (IOException | RocksDBException e) {
            System.err.println("RocksDB Error: " + e.getMessage());
            e.printStackTrace();
        }

    }

    public static void RocksDBDeleteMetaDataKey(String ClassName, String dbPath, List<ColumnFamilyDescriptor> cfDescriptors,
                                                List<ColumnFamilyHandle> columnFamilyHandleList, boolean forceDelete) throws RocksDBException {

        RocksDB rocksDBDeleteDb = RocksDB.open(dbPath, cfDescriptors, columnFamilyHandleList);
        ColumnFamilyHandle METADATAcfHandle = columnFamilyHandleList.get(3);
        byte[] rocksDBDeleteMetaDataKeyPresentCheck = rocksDBDeleteDb.get(METADATAcfHandle, ClassName.getBytes());
        if (rocksDBDeleteMetaDataKeyPresentCheck == null) {
            System.out.println("In Rocksdb ClassName - " + ClassName + " not present in Column Family Handle MetaData");
        } else {
            if (forceDelete) {
                rocksDBDeleteDb.delete(METADATAcfHandle, ClassName.getBytes());
                System.out.println("Rocksdb " + ClassName + " MetaData Deleted Successfully");
            } else {
                System.out.println("Are you sure you want to delete type name - " + ClassName);
                System.out.println("If yes so use this command with force parameter");
                System.out.println("For RockDb Delete MetaData use : java -cp <RocksDbUtility.jar>:<Model_Class.jar> com.gigaspaces.Main ROCKSDB_DELETE_METADATA <baseRocksDBWorkPath> <spaceName> <partition> <ClassName> force");
            }
        }
        for (ColumnFamilyHandle handle : columnFamilyHandleList) {
            handle.close();
        }
        rocksDBDeleteDb.close();
    }

    public static void RocksDBDeleteDataKey(String ClassName, String dbPath, List<ColumnFamilyDescriptor> cfDescriptors,
                                            List<ColumnFamilyHandle> columnFamilyHandleList, boolean forceDelete) throws RocksDBException {

        RocksDB rocksDBDeleteDb = RocksDB.open(dbPath, cfDescriptors, columnFamilyHandleList);
        ColumnFamilyHandle cfHandle = columnFamilyHandleList.get(1);
        final ReadOptions readOptions = new ReadOptions();
        boolean deletedData = false;
        List<String> classNamePresent = new ArrayList();
        try (final RocksIterator iterator = rocksDBDeleteDb.newIterator(cfHandle, readOptions)) {
            // Iterate over all key-value pairs in the column family
            for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
//                String key = new String(iterator.key());
//                String value = new String(iterator.value());
//                            System.out.printf("Key: %s %n", key);
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(iterator.value());
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                byte flags = objectInputStream.readByte();
                PlatformLogicalVersion version = new PlatformLogicalVersion();
                version.read(objectInputStream);
                String _m_Uid = objectInputStream.readUTF();
                String _expirationTime = "";

                if (version.greaterOrEquals(PlatformLogicalVersion.v11_0_0)) {
                    byte embeddedSyncInfoFlags = objectInputStream.readByte();
                    if ((embeddedSyncInfoFlags & (((byte) 1))) == (((byte) 1))) {
                        Object _generationId = objectInputStream.readLong();
                        Object _sequenceId = objectInputStream.readLong();
                    }
                }
                Object _blobStoreVersion = objectInputStream.readShort();
                String _typeName = objectInputStream.readUTF();
                if (!classNamePresent.contains(_typeName)) {
                    classNamePresent.add(_typeName);
                }
                if (_typeName.equals(ClassName)) {
                    if (forceDelete) {
                        rocksDBDeleteDb.delete(cfHandle, iterator.key());
                        deletedData = true;
                    } else {
                        System.out.println("Are you sure you want to delete type name - " + ClassName);
                        System.out.println("If yes so use this command with force parameter");
                        System.out.println("For RockDb Delete Data use : java -cp <RocksDbUtility.jar>:<Model_Class.jar> com.gigaspaces.Main ROCKSDB_DELETE_DATA <baseRocksDBWorkPath> <spaceName> <partition> <ClassName> force");
                        break;
                    }
                }
            }
            if (deletedData) {
                System.out.println("Rocksdb " + ClassName + " Data Deleted Successfully");
            } else if (!classNamePresent.contains(ClassName)) {
                System.out.println("In Rocksdb ClassName - " + ClassName + " not present in Column Family Handle Data");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (ColumnFamilyHandle handle : columnFamilyHandleList) {
            handle.close();
        }
        rocksDBDeleteDb.close();
    }


    static void writeDataToFile(String fileName, HashMap<String, Long> typesCount, String
                                        partition, HashMap<String, Long> typesExpiration,
                                String isLease, HashMap<String, String> CorrespondingTypeMetadataErrorMsg) throws
            FileNotFoundException, UnsupportedEncodingException {

        PrintWriter writer = new PrintWriter(fileName, "UTF-8");
        writer.println("Partition : " + partition);
        String headerStr = "Space_Type : Count";
        if (isLease.equals("lease")) {
            headerStr += " : Lease_Type_Count";
        }
        writer.println(headerStr);
        //writer.println("Space_Type : Count : Lease_Type_Count");
        for (String key : typesCount.keySet()) {

            String leaseTypeCount = "";
            if (typesExpiration.get(key) != null) {
                leaseTypeCount = "" + typesExpiration.get(key);
            }
            String dataBody = key + " : " + typesCount.get(key);
            if (isLease.equals("lease")) {
                dataBody += " : " + leaseTypeCount;
            }
            //writer.println(key + " : " + typesCount.get(key) + " : " + leaseTypeCount);
            writer.println(dataBody);

//            deleted from metadata only check
            Boolean PresentInDataButNotInMetaDataErrorMsgPresent = false;
            for (Map.Entry<String, String> entry : CorrespondingTypeMetadataErrorMsg.entrySet()) {
                if (entry.getKey().equals(key)) {
                    PresentInDataButNotInMetaDataErrorMsgPresent = true;
                }
            }
            if (!PresentInDataButNotInMetaDataErrorMsgPresent) {
                CorrespondingTypeMetadataErrorMsg.put(key, "Class Type " + key + " Present in Data but not in Metadata");
            }
        }

//            deleted from data only check
        for (Map.Entry<String, String> entry : CorrespondingTypeMetadataErrorMsg.entrySet()) {
            if (!typesCount.containsKey(entry.getKey())) {
                CorrespondingTypeMetadataErrorMsg.put(entry.getKey(), "Class Type " + entry.getKey() + " Present in Metadata but not in Data");
            }
        }

        for (Map.Entry<String, String> entry : CorrespondingTypeMetadataErrorMsg.entrySet()) {
//            Both present Data and Metadata
            if (entry.getValue().equals("")) {
                writer.println("Corresponding msg for type " + entry.getKey() + " : " + "Data and Metadata correlate Successfully");
            } else {
                writer.println("Corresponding msg for type " + entry.getKey() + " : " + entry.getValue());
            }
        }
        writer.close();
    }


    static void readFileData(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            // Read and print each line
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("An error occurred while reading the file.");
            e.printStackTrace();
        }
    }
}
