package com.gigaspaces;

import com.gigaspaces.internal.version.PlatformLogicalVersion;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.DBOptions;
import org.rocksdb.Options;
import org.rocksdb.ReadOptions;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class App {
    /*private static ColumnFamilyDescriptor createColumnFamilyDescriptor(String columnFamilyName, ColumnFamilyOptions columnFamilyOptions) {
        return new ColumnFamilyDescriptor(columnFamilyName.getBytes());
    }*/

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        if (args.length < 3) {
            System.out.println("java -jar <baseRocksDBWorkPath> <spaceName> <partition>");
            System.out.println("For lease use : java -jar <baseRocksDBWorkPath> <spaceName> <partition> lease");
            System.out.println("Eg :  java -jar RocksDbUtility.jar \"/work/gigaspace/gigaspaces-insightedge-versions/gigaspaces-xap-enterprise-16.2.1/work/memoryxtend/rocksdb/\" \"demo\" 1 ");
            System.out.println("invalid args");
            System.exit(1);
        }
        String basePath = args[0];
        String spaceName = args[1];
        String partition = args[2];
        String isLease = "";
        if(args.length > 3 && args[3].equals("lease")) {
            isLease = args[3];
        }
        // Static block to load the RocksDB native library
        /*String basePath = "/home/jay/work/gigaspace/gigaspaces-insightedge-versions/gigaspaces-xap-enterprise-16.2.1/work/memoryxtend/rocksdb/";
        String spaceName = "demo";
        String partition = "2";
        String isLease = "lease";*/


        RocksDB.loadLibrary();
        //final String dbPath = "/home/jay/work/gigaspace/gigaspaces-insightedge-versions/gigaspaces-xap-enterprise-16.2.1/work/memoryxtend/rocksdb/demo_container1-demo/demo_container1-demo";
        final String dbPath = basePath + "/" + spaceName + "_container" + partition + "-" + spaceName + "/" + spaceName + "_container" + partition + "-" + spaceName;
        try (final Options options = new Options().setCreateIfMissing(true);
             final ColumnFamilyOptions cfOptions = new ColumnFamilyOptions()) {
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

            // Open DB with column families
            try (final DBOptions dbOptions = new DBOptions().setCreateIfMissing(true);
                 final RocksDB db = RocksDB.open(dbOptions, dbPath, cfDescriptors, columnFamilyHandleList)) {
                // Get the specific column family handle
                ColumnFamilyHandle cfHandle = columnFamilyHandleList.get(1);

                // Create read options
                try (final ReadOptions readOptions = new ReadOptions()) {
                    // Create iterator for the specific column family
                    try (final RocksIterator iterator = db.newIterator(cfHandle, readOptions)) {
                        // Iterate over all key-value pairs in the column family
                        for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
                            String key = new String(iterator.key());
                            String value = new String(iterator.value());
                            // System.out.printf("Key: %s, Value: %s%n", key, value);
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

                            // System.out.println(_typeName);
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
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                String timestamp = sdf.format(new Date());
                String fileName = "output-" + spaceName + "-" + partition + "-" + timestamp + ".txt";
                writeDataToFile(fileName, typesCount, partition, typesExpiration, isLease);
                readFileData(fileName);
                // System.out.println(typesCount);
            }
        } catch (RocksDBException e) {
            System.err.println("RocksDB Error: " + e.getMessage());
            e.printStackTrace();
        }

    }

    static void writeDataToFile(String fileName, HashMap<String, Long> typesCount, String partition, HashMap<String, Long> typesExpiration, String isLease) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter(fileName, "UTF-8");
        writer.println("Partition : " + partition);
        String headerStr = "Space_Type : Count";;
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
