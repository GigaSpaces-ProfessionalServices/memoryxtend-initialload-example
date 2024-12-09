package com.mycompany.app;

import com.gigaspaces.client.ReadModifiers;
import com.gigaspaces.client.TakeModifiers;
import com.mycompany.app.model.Data;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.SpaceProxyConfigurer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class MultithreadedFeeder {

    private static final int MAX_NUM_THREADS = 8;
    private static final int MAX_WRITE_CHUNK_SIZE = 1_000_000;

    private static final int MAX_SLEEP_INTERVAL = 10 * 60;
    private static final int MAX_SLEEP_INTERVAL_AFTER_ERROR = 15 * 60;

    private static final int MAX_NUM_OBJECTS = 524_288_000;
    private static final int MAX_PAYLOAD_SIZE = 936;

    private static final int MAX_LEASE_TIMEOUT = 12 * 3600 * 1000;
    private static final int MAX_TIMEOUT = 12 * 3600;
    private static final int MAX_NUMBER_OF_PARTITIONS = 16;

    private static Logger log = Logger.getLogger(MultithreadedFeeder.class.getName());
    private static GigaSpace gigaSpace;

    private static final String DEFAULT_SPACE_NAME = "demo";

    private static final int DEFAULT_NUM_THREADS = 4;

    // number of objects written per interval
    private static final int DEFAULT_WRITE_CHUNK_SIZE = 500_000;

    private static final int DEFAULT_SLEEP_INTERVAL = 5;
    private static final int DEFAULT_SLEEP_INTERVAL_AFTER_ERROR = 5 * 60;

    private static final int DEFAULT_START_ID = 0;

    // max number of objects in space
//    private static final int DEFAULT_NUM_OBJECTS = 1_000_000;
    private static final int DEFAULT_NUM_OBJECTS = 100_000;
    private static final int BATCH_SIZE = 1000;


    // size of string in payload
    private static final int DEFAULT_PAYLOAD_SIZE = 936;

    private static final int DEFAULT_LEASE_TIMEOUT = 0;
    private static final int DEFAULT_TIMEOUT = 3600;


    private static String spaceName = DEFAULT_SPACE_NAME;
    private static int threadCount = DEFAULT_NUM_THREADS;
    private static int writeChunkSize = DEFAULT_WRITE_CHUNK_SIZE;

    private static int sleepInterval = DEFAULT_SLEEP_INTERVAL;
    private static int sleepIntervalAfterError = DEFAULT_SLEEP_INTERVAL_AFTER_ERROR;

    private static int startId = DEFAULT_START_ID;
    private static int maxObjects = DEFAULT_NUM_OBJECTS;
    private static int payloadSize = DEFAULT_PAYLOAD_SIZE;

    private static int leaseTimeout = DEFAULT_LEASE_TIMEOUT;
    private static int timeout = DEFAULT_TIMEOUT;
    private static int numberOfPartitions = 1;
    private static Integer[] DEFAULT_PARTITION_IDS = {1};
    private static Integer[] partitionIds = DEFAULT_PARTITION_IDS;
    private static String partitionIdsValue;

    private static String username;
    private static String password;

    private static byte[] b;

    //private static AtomicInteger runCount = new AtomicInteger();
    private static int totalNumberOfThreads = 0; // number of threads in entire space


    public MultithreadedFeeder() {
        SpaceProxyConfigurer configurer = new SpaceProxyConfigurer(spaceName);
        if (username != null && password != null) {
            configurer.credentials(username, password);
        }
        gigaSpace = new GigaSpaceConfigurer(configurer).gigaSpace();

        createPayload();
    }

    private void createPayload() {
        b = new byte[payloadSize];
        new Random().nextBytes(b);
    }

    private static void modifyPayload() {
        int index = (int) (Math.random() * payloadSize);
        b[index] = (byte) new Random().nextInt(Byte.MAX_VALUE);
    }

    private static Integer[] parsePartitionId(String partitionIdsValue, int numberOfPartitions) {


        String[] sPartitions = partitionIdsValue.split(",");
        Integer[] partitions = new Integer[sPartitions.length];

        if (sPartitions.length > MAX_NUMBER_OF_PARTITIONS) {
            log.info("Incorrect number of partitions specified.");
            return DEFAULT_PARTITION_IDS;
        }
        try {

            for (int i = 0; i < sPartitions.length; i++) {
                partitions[i] = Integer.parseInt(sPartitions[i]);
                if (partitions[i] < 1 || partitions[i] > MAX_NUMBER_OF_PARTITIONS) {
                    throw new NumberFormatException("Partition id cannot be less than 1 or greater than " + MAX_NUMBER_OF_PARTITIONS + ".");
                }
            }
        } catch (NumberFormatException nfe) {
            return DEFAULT_PARTITION_IDS;
        }
        return partitions;
    }

    private static int checkRange(String value, int min, int max, int defaultValue) {
        try {
            int val = Integer.parseInt(value);
            if (min <= val && val <= max) {
                return val;
            }
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            return defaultValue;
        }
        return defaultValue;
    }

    public static void printUsage() {
        System.out.println("This program is a multi-threaded feeder.");
        System.out.println("Available arguments are:");
        System.out.println("  -spaceName,      Space name.");
        System.out.println("       Default: " + DEFAULT_SPACE_NAME);
        System.out.println("  -numThreads,     Number of threads.");
        System.out.println("       Default: " + DEFAULT_NUM_THREADS + ", Max: " + MAX_NUM_THREADS);
        System.out.println("  -writeChunkSize, Number of objects written per interval.");
        System.out.println("       Default: " + DEFAULT_WRITE_CHUNK_SIZE + ", Max: " + MAX_WRITE_CHUNK_SIZE);
        System.out.println("  -sleepInterval,  Sleep interval after writing a chunk (in seconds).");
        System.out.println("       Default: " + DEFAULT_SLEEP_INTERVAL + ", Max: " + MAX_SLEEP_INTERVAL);
        System.out.println("  -startId,       The id to begin writing to the space. This is useful when resuming the feeder.");
        System.out.println("       Default: " + DEFAULT_START_ID + ", Max: " + MAX_NUM_OBJECTS);
        System.out.println("  -maxObjects,     Maximum number of objects in space.");
        System.out.println("       Default: " + DEFAULT_NUM_OBJECTS + ", Max: " + MAX_NUM_OBJECTS);
        System.out.println("  -payloadSize,    Payload size (in bytes).");
        System.out.println("       Default: " + DEFAULT_PAYLOAD_SIZE + ", Max: " + MAX_PAYLOAD_SIZE);
        System.out.println("  -leaseTimeout: Lease timeout (in milliseconds)");
        System.out.println("       Default: No lease timeout used.");
        System.out.println("  -timeout,        Timeout (in seconds) for the ExecutorService.");
        System.out.println("       Default: " + DEFAULT_TIMEOUT);
        System.out.println("  -numberOfPartitions,  Number of partitions.");
        System.out.println("  -partitionIds,    The partitionIds, used with -numberOfPartitions to route values to multiple partitions.");
        System.out.println("       For example, if numberOfPartitions is 3, partitionIds can be: 1, 2 or 3, separated with a comma.");
        System.out.println("  -username,       username. Use if XAP cluster is secured.");
        System.out.println("  -password,       password. Use if XAP cluster is secured.");
        System.exit(0);
    }

    public static void generateRecords(int numOfObjects) throws Exception {

        long TotalStartTime = System.currentTimeMillis();
        List<Data> dataList = new ArrayList<>();

        for (int k = 0; k < numOfObjects; k++) {
            long insertStartTime = System.currentTimeMillis();
            Data data = new Data();
            data.setId(k);
            data.setMessage(String.format("msg  - %d", k));
            modifyPayload();
            data.setPayload(b.clone());
            data.setProcessed(Boolean.FALSE);
            dataList.add(data);

            if (dataList.size() % BATCH_SIZE == 0) {
                gigaSpace.writeMultiple(dataList.toArray(), (long) leaseTimeout);
                dataList.clear();
                long endTime = System.currentTimeMillis();
                long duration = endTime - insertStartTime;
                log.info("Total time to write "+ BATCH_SIZE +" entries is "+ (duration/1000F) + " in sec");
            }
        }
        long TotalendTime = System.currentTimeMillis();
        long TotalDuration = TotalendTime - TotalStartTime;
        log.info("Total time to write "+ numOfObjects +" entries is "+ (TotalDuration/1000F) + " in sec");
        log.info("Average time to write 1 entries is "+ ((maxObjects/TotalDuration)/1000F) + " in sec");
        log.info(" ");
        log.info(" ");

        gigaSpace.readById(Data.class,1);
        gigaSpace.takeById(Data.class,1);

//        Data result = gigaSpace.take(new Data(),0, TakeModifiers.FIFO);
//        log.info(result.toString());
    }


    public static void readRecords(int numOfObjects) throws Exception {
        long TotalStartTime = System.currentTimeMillis();
        for (int k = 0; k < numOfObjects/BATCH_SIZE; k++) {
            long insertStartTime = System.currentTimeMillis();
            Data[] dataArray = gigaSpace.readMultiple(new Data(),BATCH_SIZE);
            List<Data> dataArrays = new ArrayList<Data>(Arrays.asList(dataArray));
            long endTime = System.currentTimeMillis();
            long duration = endTime - insertStartTime;
            log.info("Total time to Read "+ dataArrays.size() +" entries is "+ (duration/1000F) + " in sec");
        }
        long TotalendTime = System.currentTimeMillis();
        long TotalDuration = TotalendTime - TotalStartTime;
        log.info("Total time to Read "+ numOfObjects +" entries is "+ (TotalDuration/1000F) + " in sec");
        log.info("Average time to Read 1 entries is "+ ((maxObjects/TotalDuration)/1000F) + " in sec");
    }

    public static void main(String[] args) {
        try {

            int index = args.length;

            if (args[0].equalsIgnoreCase("-help")) {
                printUsage();
                System.exit(0);
            }

            if (index >= 2) {

                while (index >= 2) {
                    String property = args[index - 2];
                    String value = args[index - 1];
                    if (property.equalsIgnoreCase("-spaceName")) {
                        spaceName = value;
                    } else if (property.equalsIgnoreCase("-numThreads")) {
                        threadCount = checkRange(value, 1, MAX_NUM_THREADS, DEFAULT_NUM_THREADS);
                    } else if (property.equalsIgnoreCase("-writeChunkSize")) {
                        writeChunkSize = checkRange(value, 1, MAX_WRITE_CHUNK_SIZE, DEFAULT_WRITE_CHUNK_SIZE);
                    } else if (property.equalsIgnoreCase("-sleepInterval")) {
                        sleepInterval = checkRange(value, 0, MAX_SLEEP_INTERVAL, DEFAULT_SLEEP_INTERVAL);
                    } else if (property.equalsIgnoreCase("-startId")) {
                        startId = checkRange(value, 0, MAX_NUM_OBJECTS, DEFAULT_START_ID);
                    } else if (property.equalsIgnoreCase("-maxObjects")) {
                        maxObjects = checkRange(value, 1, MAX_NUM_OBJECTS, DEFAULT_NUM_OBJECTS);
                    } else if (property.equalsIgnoreCase("-payloadSize")) {
                        payloadSize = checkRange(value, 1, MAX_PAYLOAD_SIZE, DEFAULT_PAYLOAD_SIZE);
                    } else if (property.equalsIgnoreCase("-leaseTimeout")) {
                        leaseTimeout = checkRange(value, 0, MAX_LEASE_TIMEOUT, DEFAULT_LEASE_TIMEOUT);
                    } else if (property.equalsIgnoreCase("-timeout")) {
                        timeout = checkRange(value, 1, MAX_TIMEOUT, DEFAULT_TIMEOUT);
                    } else if (property.equalsIgnoreCase("-numberOfPartitions")) {
                        numberOfPartitions = checkRange(value, 1, MAX_NUMBER_OF_PARTITIONS, 1);
                    } else if (property.equalsIgnoreCase("-partitionIds")) {
                        partitionIdsValue = value;
                    } else if (property.equalsIgnoreCase("-username")) {
                        username = value;
                    } else if (property.equalsIgnoreCase("-password")) {
                        password = value;
                    } else {
                        System.out.println("Incorrect argument provided.");
                        printUsage();
                        System.exit(-1);
                    }

                    index -= 2;
                }
            }
            MultithreadedFeeder feeder = new MultithreadedFeeder();

            log.info("Space name: " + spaceName);
            log.info("Number of threads: " + threadCount);
            log.info("Start Id: " + startId);
            log.info("Number of objects written per interval: " + writeChunkSize);
            log.info("Interval (in seconds): " + sleepInterval);
            log.info("Max number of objects in space: " + maxObjects);
            log.info("Payload size (in bytes): " + payloadSize);
            if (leaseTimeout != 0) {
                log.info("Lease Timeout: " + leaseTimeout);
            }
            log.info("Timeout (in seconds): " + timeout);

            generateRecords(maxObjects);
            readRecords(maxObjects);

//            Data data = new Data();
//
//            data.setId(1);
//            data.setMessage(String.format("msg  - %d", 1));
//            modifyPayload();
//            data.setPayload(b.clone());
//            data.setProcessed(Boolean.FALSE);
//            gigaSpace.write(data,30000);
//

//            gigaSpace.readById(Data.class,1);
//            gigaSpace.takeById(Data.class,1);



        } catch (Throwable t) {
            t.printStackTrace();
            ;
        }
    }

}
