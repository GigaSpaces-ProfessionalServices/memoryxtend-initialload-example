package com.mycompany.app;

import com.mycompany.app.model.*;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.SpaceProxyConfigurer;

import java.util.*;
import java.util.logging.Logger;

public class AAMultithreadedFeeder {

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


    public AAMultithreadedFeeder() {
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

    public static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder randomString = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            randomString.append(characters.charAt(index));
        }
        return randomString.toString();
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

    public static CrewFlightDetail getCrewFlightDetail() {
        Date tempDate = new Date();
        CrewFlightDetail crewFlightDetail = new CrewFlightDetail();
        crewFlightDetail.setFlightKey(getTempLKAFlightKey());
        crewFlightDetail.setFlightPosition(generateRandomString(10));
        crewFlightDetail.setLegType("00001000100100000000000000000011");
        crewFlightDetail.setRequiredFAStaffing(new Random().nextBoolean());
        crewFlightDetail.setCode21E2(generateRandomString(10));
        crewFlightDetail.setLanguageRequirement(generateRandomString(10));
        crewFlightDetail.setAtcDelayMinutes(new Random().nextInt());
        crewFlightDetail.setDeicingDelayMinutes(new Random().nextInt());
        crewFlightDetail.setRampDelayMinutes(2);
        crewFlightDetail.setLegAssignmentCode(generateRandomString(10));
        crewFlightDetail.setLegRemovalCode(generateRandomString(10));
        crewFlightDetail.setDepartureTimeLocalReferenceMinutes(new Random().nextInt());
        crewFlightDetail.setArrivalTimeLocalReferenceMinutes(new Random().nextInt());
        crewFlightDetail.setActualEquipmentType(generateRandomString(10));
        crewFlightDetail.setExpansionBits("00000000");
        crewFlightDetail.setFlightOrderInSequence(new Random().nextInt());
        crewFlightDetail.setActualSeat(generateRandomString(10));
        crewFlightDetail.setSeatSpecificData("0000000000000000");
        crewFlightDetail.setPreviousFlightKey(getTempLKAFlightKey());
        crewFlightDetail.setNextFlightKey(getTempLKAFlightKey());
        crewFlightDetail.setCrewTypeCode(generateRandomString(10));
        crewFlightDetail.setDeadHeadType(generateRandomString(10));
        crewFlightDetail.setDutyKey(getTempCrewDutyKey());
        crewFlightDetail.setOalDepartureTime(tempDate);
        crewFlightDetail.setOalArrivalTime(tempDate);
        crewFlightDetail.setFlightType(null);
        crewFlightDetail.setReportOrSurfaceLegArrivalStn(generateRandomString(10));
        crewFlightDetail.setBaseLeg(generateRandomString(10));
        crewFlightDetail.setOperatingFlight(new Random().nextBoolean());
        crewFlightDetail.setFirstFlightInDuty(new Random().nextBoolean());
        crewFlightDetail.setLastFlightInDuty(new Random().nextBoolean());
        crewFlightDetail.setPreviousLegArrivalStation(generateRandomString(10));
        crewFlightDetail.setCrewComingFrom(generateRandomString(10));
        crewFlightDetail.setArrivalStation(generateRandomString(10));
        crewFlightDetail.setIsHRD(new Random().nextBoolean());
        crewFlightDetail.setIsIPD(new Random().nextBoolean());
        crewFlightDetail.setNextFlightCrewTypeCode(generateRandomString(10));
        crewFlightDetail.setContractualMonth(generateRandomString(10));
        return crewFlightDetail;
    }

    public static List<CrewFlightDetail> getcrewFlightDetailList() {
        List<CrewFlightDetail> crewFlightDetailList = new ArrayList<>();
        crewFlightDetailList.add(getCrewFlightDetail());
        crewFlightDetailList.add(getCrewFlightDetail());
        crewFlightDetailList.add(getCrewFlightDetail());
        crewFlightDetailList.add(getCrewFlightDetail());
        crewFlightDetailList.add(getCrewFlightDetail());
        crewFlightDetailList.add(getCrewFlightDetail());
        crewFlightDetailList.add(getCrewFlightDetail());
        crewFlightDetailList.add(getCrewFlightDetail());
        crewFlightDetailList.add(getCrewFlightDetail());
        crewFlightDetailList.add(getCrewFlightDetail());
        return crewFlightDetailList;
    }


    public static LKAFlightKey getTempLKAFlightKey() {
        String tempString = "AA";
        Date tempDate = new Date();
        Integer tempInteger = 1;
        LKAFlightKey lkaFlightKey = new LKAFlightKey();
        lkaFlightKey.setAirlineCode(generateRandomString(10));
        lkaFlightKey.setFlightNumber(generateRandomString(10));
        lkaFlightKey.setFlightDate(generateRandomString(10));
        lkaFlightKey.setDepartureStation(generateRandomString(10));
        lkaFlightKey.setDupDepCode(tempInteger);
        lkaFlightKey.setSnapshotId(generateRandomString(10));
        return lkaFlightKey;
    }

    public static CrewDutyKey getTempCrewDutyKey() {
        CrewDutyKey dutyKey = new CrewDutyKey(generateRandomString(20), generateRandomString(20), generateRandomString(20), generateRandomString(20), generateRandomString(20), generateRandomString(20), generateRandomString(20), new Random().nextInt(), generateRandomString(20));
        return dutyKey;
    }

    public static CrewPositionKey getTempCrewPositionKey() {
        CrewPositionKey crewPositionKey = new CrewPositionKey(generateRandomString(20), generateRandomString(20), generateRandomString(20), generateRandomString(20), generateRandomString(20), generateRandomString(20), generateRandomString(20), generateRandomString(20));
        return crewPositionKey;
    }

    public static PositionInd getTempPositionInd() {
        PositionInd positionInd = new PositionInd();
        positionInd.setBits(generateRandomString(20));
        positionInd.setLongRange(new Random().nextBoolean());
        return positionInd;
    }

    public static CrewSequenceKey getCrewSequenceKey() {
        String tempString = "AA";
        Date tempDate = new Date();
        Integer tempInteger = 1;
        CrewSequenceKey crewSequenceKey = new CrewSequenceKey();
        crewSequenceKey.setSequenceNumber(generateRandomString(10));
        crewSequenceKey.setSequenceOrignDate(generateRandomString(10));
        crewSequenceKey.setCrewBase(generateRandomString(10));
        crewSequenceKey.setCrewType(generateRandomString(10));
        crewSequenceKey.setAirlineCode(generateRandomString(10));
        crewSequenceKey.setDomesticOrInternational(generateRandomString(10));
        crewSequenceKey.setSnapshotId(generateRandomString(10));
        return crewSequenceKey;
    }

    public static CrewHotel getTempCrewHotel() {
        String tempString = "AA";
        Date tempDate = new Date();
        CrewHotel crewHotel = new CrewHotel();
        crewHotel.setCrewHotelKey(generateRandomString(20));
        crewHotel.setRoutingId(generateRandomString(10));
        crewHotel.setEmployeeNumber(generateRandomString(10));
        crewHotel.setSourceTimeStamp(tempDate);
        crewHotel.setHotelName(generateRandomString(10));
        crewHotel.setHotelPhone(generateRandomString(10));
        crewHotel.setPositionKey(getTempCrewPositionKey());
        crewHotel.setContractualMonth(generateRandomString(10));
        return crewHotel;
    }

    public static CrewPositionDetail getCrewPositionDetail() {
        String tempString = "AA";
        Date tempDate = new Date();
        CrewPositionDetail crewPositionDetail = new CrewPositionDetail();
        crewPositionDetail.setEmployeeNumber(generateRandomString(10));
        crewPositionDetail.setSequencePosition(generateRandomString(10));
        crewPositionDetail.setDepartureStation(generateRandomString(10));
        crewPositionDetail.setFailingContinuityIndicator(generateRandomString(10));
        crewPositionDetail.setNeededRestMinutes(new Random().nextInt());
        crewPositionDetail.setRestType(generateRandomString(10));
        crewPositionDetail.setArrivalOfLastLeg(generateRandomString(10));
        return crewPositionDetail;
    }

    public static LegalityInd getLegalityInd() {

        LegalityInd legalityInd = new LegalityInd();
        legalityInd.setBits(generateRandomString(20));
        legalityInd.setExtendedLongRange(new Random().nextBoolean());
        return legalityInd;
    }


    public static RemainingFlightDutyKey getRemainingFlightDutyKey() {
        String tempString = "AA";
        Date tempDate = new Date();
        RemainingFlightDutyKey remainingFlightDutyKey = new RemainingFlightDutyKey(getTempLKAFlightKey(), tempString, getCrewSequenceKey());
//        remainingFlightDutyKey.setFlightKey(getTempLKAFlightKey());
//        remainingFlightDutyKey.setEmployeeNumber(generateRandomString(10));
//        remainingFlightDutyKey.setCrewSequenceKey(getCrewSequenceKey());
        return remainingFlightDutyKey;
    }

    public static List<CrewPositionDetail> getCrewPositionDetailList() {
        List<CrewPositionDetail> crewPositionDetailList = new ArrayList<>();
        crewPositionDetailList.add(getCrewPositionDetail());
        crewPositionDetailList.add(getCrewPositionDetail());
        crewPositionDetailList.add(getCrewPositionDetail());
        crewPositionDetailList.add(getCrewPositionDetail());
        crewPositionDetailList.add(getCrewPositionDetail());
        crewPositionDetailList.add(getCrewPositionDetail());
        crewPositionDetailList.add(getCrewPositionDetail());
        crewPositionDetailList.add(getCrewPositionDetail());
        crewPositionDetailList.add(getCrewPositionDetail());
        crewPositionDetailList.add(getCrewPositionDetail());

        crewPositionDetailList.add(getCrewPositionDetail());
        crewPositionDetailList.add(getCrewPositionDetail());
        crewPositionDetailList.add(getCrewPositionDetail());
        crewPositionDetailList.add(getCrewPositionDetail());
        crewPositionDetailList.add(getCrewPositionDetail());
        crewPositionDetailList.add(getCrewPositionDetail());
        crewPositionDetailList.add(getCrewPositionDetail());
        crewPositionDetailList.add(getCrewPositionDetail());
        crewPositionDetailList.add(getCrewPositionDetail());
        crewPositionDetailList.add(getCrewPositionDetail());

        return crewPositionDetailList;
    }


    public static void generateCrewDutyFlightDetailRecords(int numOfObjects) throws Exception {

        long TotalStartTime = System.currentTimeMillis();
        List<CrewDutyFlightDetail> crewDutyFlightDetailRecordsList = new ArrayList<>();

        for (int k = 0; k < numOfObjects; k++) {
            long insertStartTime = System.currentTimeMillis();
            String tempString = "AA";
            Date tempDate = new Date();
            CrewDutyFlightDetail crewDutyFlightDetail = new CrewDutyFlightDetail();
            crewDutyFlightDetail.setAssignedDuringTraining(generateRandomString(10));

            crewDutyFlightDetail.setDutyKey(getTempCrewDutyKey());
            crewDutyFlightDetail.setRoutingId(generateRandomString(10) + k);
            crewDutyFlightDetail.setEmployeeNumber(generateRandomString(10));

            crewDutyFlightDetail.setPositionKey(getTempCrewPositionKey());
            crewDutyFlightDetail.setFosUpdateTime(tempDate);

            crewDutyFlightDetail.setSequencePosition(generateRandomString(20));
            crewDutyFlightDetail.setDutyStartTime(tempDate);
            crewDutyFlightDetail.setDutyEndTime(tempDate);
            crewDutyFlightDetail.setEndSequenceCode(new Random().nextBoolean());
            crewDutyFlightDetail.setPreviousDutyEndTime(tempDate);
            crewDutyFlightDetail.setActivityDate(tempDate);
            crewDutyFlightDetail.setDutyStartDepartureStation(generateRandomString(20));
            crewDutyFlightDetail.setBaseLongtitudeDegrees(new Random().nextInt());
            crewDutyFlightDetail.setDepartureStationLongtitudeDegrees(2);
            crewDutyFlightDetail.setStartFdpSeries(new Random().nextBoolean());
            crewDutyFlightDetail.setTenHourRest(new Random().nextBoolean());


            List<CrewFlightDetail> crewFlightDetailArrayList = new ArrayList<>();
            crewFlightDetailArrayList.add(getCrewFlightDetail());
            crewFlightDetailArrayList.add(getCrewFlightDetail());
            crewFlightDetailArrayList.add(getCrewFlightDetail());
            crewFlightDetailArrayList.add(getCrewFlightDetail());
            crewFlightDetailArrayList.add(getCrewFlightDetail());
            crewFlightDetailArrayList.add(getCrewFlightDetail());
            crewFlightDetailArrayList.add(getCrewFlightDetail());
            crewFlightDetailArrayList.add(getCrewFlightDetail());
            crewFlightDetailArrayList.add(getCrewFlightDetail());
            crewFlightDetailArrayList.add(getCrewFlightDetail());
            crewDutyFlightDetail.setCrewFlights(crewFlightDetailArrayList);

            crewDutyFlightDetail.setTempDutyStartDate(tempDate);
            crewDutyFlightDetail.setTempDutyEndDate(tempDate);
            crewDutyFlightDetail.setTempCrewBase(generateRandomString(10));
            crewDutyFlightDetail.setTempDutyIndicator(new Random().nextBoolean());
            crewDutyFlightDetail.setRapMinutes(new Random().nextInt());
            crewDutyFlightDetail.setCrewHotel(getTempCrewHotel());
            crewDutyFlightDetail.setSequenceChanged(new Random().nextBoolean());
            crewDutyFlightDetail.setCrewRestStartTime(tempDate);
            crewDutyFlightDetail.setCrewRestEndTime(tempDate);
            crewDutyFlightDetail.setIsReducedRest(new Random().nextBoolean());
            crewDutyFlightDetail.setThirtyHourRestStartTime(tempDate);
            crewDutyFlightDetail.setThirtyHourRestEndTime(tempDate);
            crewDutyFlightDetail.setPreviousRestMinutes(new Random().nextInt());
            crewDutyFlightDetail.setNextRestMinutes(new Random().nextInt());
            crewDutyFlightDetail.setDomesticOrInternationalFromFlights(generateRandomString(10));
            crewDutyFlightDetail.setNextRestMinutesPreviousState(generateRandomString(10));
            crewDutyFlightDetail.setSequenceDate(new Random().nextLong());
            crewDutyFlightDetail.setNextDutyKey(getTempCrewDutyKey());
            crewDutyFlightDetail.setPreviousDutyKey(getTempCrewDutyKey());
            crewDutyFlightDetail.setScheduledOnDutyMinutes(new Random().nextInt());
            crewDutyFlightDetail.setContractualMonth(generateRandomString(10));
            crewDutyFlightDetail.setScheduledDutyStartTime(tempDate);
            crewDutyFlightDetail.setScheduledDutyEndTime(tempDate);
            crewDutyFlightDetail.setDutyStartTimestamp(new Random().nextLong());
            crewDutyFlightDetail.setDutyEndTimestamp(new Random().nextLong());
            crewDutyFlightDetail.setRapPosition(generateRandomString(10));
            crewDutyFlightDetail.setPositionInd(getTempPositionInd());
            crewDutyFlightDetail.setLegalityInd(getLegalityInd());
            crewDutyFlightDetail.setMiscInd(generateRandomString(10));
            crewDutyFlightDetail.setRestClassFacility(new Random().nextInt());
            crewDutyFlightDetail.setDoubleUpSequence(new Random().nextBoolean());
            crewDutyFlightDetail.setAmoc(new Random().nextBoolean());
            crewDutyFlightDetail.setSeaBlrAmoc(new Random().nextBoolean());
            crewDutyFlightDetail.setDelJfkAmoc(new Random().nextBoolean());
            crewDutyFlightDetail.setPreviousEmployeeNumber(generateRandomString(10));
            crewDutyFlightDetailRecordsList.add(crewDutyFlightDetail);

            if (crewDutyFlightDetailRecordsList.size() % BATCH_SIZE == 0) {
                gigaSpace.writeMultiple(crewDutyFlightDetailRecordsList.toArray(), (long) leaseTimeout);
                crewDutyFlightDetailRecordsList.clear();
                long endTime = System.currentTimeMillis();
                long duration = endTime - insertStartTime;
                log.info("CrewDutyFlightDetail Total time to write " + BATCH_SIZE + " entries is " + (duration / 1000F) + " in sec");
            }
        }
        long TotalendTime = System.currentTimeMillis();
        long TotalDuration = TotalendTime - TotalStartTime;
        log.info("CrewDutyFlightDetail Total time to write " + numOfObjects + " entries is " + (TotalDuration / 1000F) + " in sec");
        log.info("CrewDutyFlightDetail Average time to write 1 entries is " + ((maxObjects / TotalDuration) / 1000F) + " in sec");
        log.info(" ");
    }

    public static void generateFlightOperatingCrewRecords(int numOfObjects) throws Exception {

        long TotalStartTime = System.currentTimeMillis();
        List<FlightOperatingCrew> flightOperatingCrewRecordsList = new ArrayList<>();

        for (int k = 0; k < numOfObjects; k++) {
            long insertStartTime = System.currentTimeMillis();
            String tempString = "AA";
            Date tempDate = new Date();
            FlightOperatingCrew flightOperatingCrew = new FlightOperatingCrew();
            flightOperatingCrew.setFlightKey(getTempLKAFlightKey());
            flightOperatingCrew.setCockPitCrewList(getcrewFlightDetailList());
            flightOperatingCrew.setCabinCrewList(getcrewFlightDetailList());
            flightOperatingCrew.setDeadHeadCrewList(getcrewFlightDetailList());
            flightOperatingCrew.setRegionalDHCrewList(getcrewFlightDetailList());
            flightOperatingCrew.setFlightDate(tempDate);
            flightOperatingCrew.setPositionEmployeeMap(new HashMap<>());
            flightOperatingCrew.setRoutingFlightKey(getTempLKAFlightKey());
            flightOperatingCrew.setFlightDtLong(1l);
            flightOperatingCrew.setFosUpdateTime(tempDate);
            flightOperatingCrewRecordsList.add(flightOperatingCrew);

            if (flightOperatingCrewRecordsList.size() % BATCH_SIZE == 0) {
                gigaSpace.writeMultiple(flightOperatingCrewRecordsList.toArray(), (long) leaseTimeout);
                flightOperatingCrewRecordsList.clear();
                long endTime = System.currentTimeMillis();
                long duration = endTime - insertStartTime;
                log.info("FlightOperatingCrew Total time to write " + BATCH_SIZE + " entries is " + (duration / 1000F) + " in sec");
            }
        }
        long TotalendTime = System.currentTimeMillis();
        long TotalDuration = TotalendTime - TotalStartTime;
        log.info("FlightOperatingCrew Total time to write " + numOfObjects + " entries is " + (TotalDuration / 1000F) + " in sec");
        log.info("FlightOperatingCrew Average time to write 1 entries is " + ((maxObjects / TotalDuration) / 1000F) + " in sec");
        log.info(" ");
    }


    public static void generateCrewsequencePositionDetailRecords(int numOfObjects) throws Exception {

        long TotalStartTime = System.currentTimeMillis();
        List<CrewSequencePositionDetail> crewSequencePositionDetailRecordsList = new ArrayList<>();

        for (int k = 0; k < numOfObjects; k++) {
            long insertStartTime = System.currentTimeMillis();
            String tempString = "AA";
            Date tempDate = new Date();
            CrewSequencePositionDetail crewSequencePositionDetail = new CrewSequencePositionDetail();
            crewSequencePositionDetail.setSequenceKey(getCrewSequenceKey());
            crewSequencePositionDetail.setActualSequenceStartDate(tempDate);
            crewSequencePositionDetail.setActualSequenceEndDate(tempDate);
            crewSequencePositionDetail.setEventDate(tempDate);
            crewSequencePositionDetail.setFlownSequenceMinutes(new Random().nextInt());
            crewSequencePositionDetail.setPositions(getCrewPositionDetailList());
            crewSequencePositionDetail.setSequenceInd("00000000000000000000000000000000");
            crewSequencePositionDetailRecordsList.add(crewSequencePositionDetail);

            if (crewSequencePositionDetailRecordsList.size() % BATCH_SIZE == 0) {
                gigaSpace.writeMultiple(crewSequencePositionDetailRecordsList.toArray(), (long) leaseTimeout);
                crewSequencePositionDetailRecordsList.clear();
                long endTime = System.currentTimeMillis();
                long duration = endTime - insertStartTime;
                log.info("CrewsequencePositionDetail Total time to write " + BATCH_SIZE + " entries is " + (duration / 1000F) + " in sec");
            }
        }
        long TotalendTime = System.currentTimeMillis();
        long TotalDuration = TotalendTime - TotalStartTime;
        log.info("CrewsequencePositionDetail Total time to write " + numOfObjects + " entries is " + (TotalDuration / 1000F) + " in sec");
        log.info("CrewsequencePositionDetail Average time to write 1 entries is " + ((maxObjects / TotalDuration) / 1000F) + " in sec");
        log.info(" ");
    }

    public static void generateRemainingFlightDutyRecords(int numOfObjects) throws Exception {

        long TotalStartTime = System.currentTimeMillis();
        List<RemainingFlightDuty> RemainingFlightDutyRecordsList = new ArrayList<>();

        for (int k = 0; k < numOfObjects; k++) {
            long insertStartTime = System.currentTimeMillis();
            String tempString = "AA";
            Date tempDate = new Date();
            RemainingFlightDuty remainingFlightDuty = new RemainingFlightDuty();
            remainingFlightDuty.setRemainingFlightDutyKey(getRemainingFlightDutyKey());
            remainingFlightDuty.setLayOverLTA(tempDate);
            remainingFlightDuty.setlTR(new Random().nextLong());
            remainingFlightDuty.setProjectedLTR(new Random().nextLong());
            remainingFlightDuty.setControlLTR(new Random().nextLong());
            remainingFlightDuty.setDtr(new Random().nextLong());
            remainingFlightDuty.setDtr(new Random().nextLong());
            remainingFlightDuty.setProjectedDTR(new Random().nextLong());
            remainingFlightDuty.setProjectedDTR(new Random().nextLong());
            remainingFlightDuty.setControlDTR(new Random().nextLong());
            remainingFlightDuty.setControlDTR(new Random().nextLong());
            remainingFlightDuty.setEdtr(new Random().nextLong());
            remainingFlightDuty.setLatestGMTArvTime(tempDate);
            remainingFlightDuty.setLatestGMTArvTime(tempDate);
            remainingFlightDuty.setLatestGMTDepTime(tempDate);
            remainingFlightDuty.setLatestGMTDepTime(tempDate);
            remainingFlightDuty.setlTA(tempDate);
            remainingFlightDuty.setFdpEndTime(tempDate);
            remainingFlightDuty.setFdpEndTime(tempDate);
            remainingFlightDuty.setProjGMTArvTime(tempDate);
            remainingFlightDuty.setProjGMTArvTime(tempDate);
            remainingFlightDuty.setProjGMTDepTime(tempDate);
            remainingFlightDuty.setProjGMTDepTime(tempDate);
            remainingFlightDuty.setCntlGMTDepTime(tempDate);
            remainingFlightDuty.setCntlGMTDepTime(tempDate);
            remainingFlightDuty.setCntlGMTArvTime(tempDate);
            remainingFlightDuty.setCntlGMTArvTime(tempDate);
            remainingFlightDuty.setFtr(new Random().nextLong());
            remainingFlightDuty.setFtr(new Random().nextLong());
            remainingFlightDuty.setDutyTimeRemaining(new Random().nextLong());
            remainingFlightDuty.setDropDeadLegMot(tempDate);
            remainingFlightDuty.setDropDeadLegEMOT(tempDate);
            remainingFlightDuty.setDropDeadSequenceMot(tempDate);
            remainingFlightDuty.setDropDeadSequenceEMot(tempDate);
            remainingFlightDuty.setProjDropDeadLegMot(tempDate);
            remainingFlightDuty.setProjdropDeadLegEMOT(tempDate);
            remainingFlightDuty.setProjDropDeadSequenceMot(tempDate);
            remainingFlightDuty.setProjDropDeadSequenceEmot(tempDate);
            remainingFlightDuty.setLatestTaxiOutTimeInMinutes(new Random().nextInt());
            remainingFlightDuty.setArrivalTimeOfLastLeg(tempDate);
            remainingFlightDuty.setProjArrivalTimeOfLastLeg(tempDate);
            remainingFlightDuty.setFdpMOT(tempDate);
            remainingFlightDuty.setFlightMOT(tempDate);
            remainingFlightDuty.setEmployeeNumber(generateRandomString(10));
            remainingFlightDuty.setDutyStartTime(tempDate);
            remainingFlightDuty.setDutyEndTime(tempDate);
            remainingFlightDuty.setAllowableMaxDuty(new Random().nextInt());
            remainingFlightDuty.setMinimumRestNeeded(new Random().nextInt());
            remainingFlightDuty.setControlledCurrentRestTime(new Random().nextInt());
            remainingFlightDuty.setControlledCurrentRestTime(new Random().nextInt());
            remainingFlightDuty.setProjectedCurrentRestTime(new Random().nextInt());
            remainingFlightDuty.setProjectedCurrentRestTime(new Random().nextInt());
            remainingFlightDuty.setLatestCurrentRestTime(new Random().nextInt());
            remainingFlightDuty.setLatestCurrentRestTime(new Random().nextInt());
            remainingFlightDuty.setNextDutyStartTime(tempDate);
            remainingFlightDuty.setTravelTimeForCrew(new Random().nextInt());
            remainingFlightDuty.setDebriefMinutes(new Random().nextInt());
            remainingFlightDuty.setNextDaySignInMinutes(new Random().nextInt());
            remainingFlightDuty.setSequenceFTR(new Random().nextInt());
            remainingFlightDuty.setSequenceDTR(new Random().nextLong());
            remainingFlightDuty.setSequenceDTR(new Random().nextLong());
            remainingFlightDuty.setMaxDutyEndTime(tempDate);
            remainingFlightDuty.setNoOfLegsInDuty(new Random().nextInt());
            remainingFlightDuty.setFlyingTimeLeft(new Random().nextInt());
            remainingFlightDuty.setMaxAllowedFlying(new Random().nextInt());
            remainingFlightDuty.setControlArrivalTimeOfLastLeg(tempDate);
            remainingFlightDuty.setPreviousSequenceDTR(new Random().nextLong());
            remainingFlightDuty.setPreviousSequenceFTR(new Random().nextLong());
            remainingFlightDuty.setFdpExtnMins(new Random().nextInt());
            remainingFlightDuty.setFdpMOTGMT(tempDate);
            remainingFlightDuty.setFlightMOTGMT(tempDate);
            remainingFlightDuty.setUpdateTime(tempDate);
            remainingFlightDuty.setMinimumConnectTime(new Random().nextInt());
            remainingFlightDuty.setSnapshots(new HashMap<>());
            RemainingFlightDutyRecordsList.add(remainingFlightDuty);


            if (RemainingFlightDutyRecordsList.size() % BATCH_SIZE == 0) {
                gigaSpace.writeMultiple(RemainingFlightDutyRecordsList.toArray(), (long) leaseTimeout);
                RemainingFlightDutyRecordsList.clear();
                long endTime = System.currentTimeMillis();
                long duration = endTime - insertStartTime;
                log.info("RemainingFlightDuty Total time to write " + BATCH_SIZE + " entries is " + (duration / 1000F) + " in sec");
            }
        }
        long TotalendTime = System.currentTimeMillis();
        long TotalDuration = TotalendTime - TotalStartTime;
        log.info("RemainingFlightDuty Total time to write " + numOfObjects + " entries is " + (TotalDuration / 1000F) + " in sec");
        log.info("RemainingFlightDuty Average time to write 1 entries is " + ((maxObjects / TotalDuration) / 1000F) + " in sec");
        log.info(" ");
    }

    public static void generateStatusRecords(int numOfObjects) throws Exception {

        long TotalStartTime = System.currentTimeMillis();
        List<Status> statusRecordsList = new ArrayList<>();

        for (int k = 0; k < numOfObjects; k++) {
            long insertStartTime = System.currentTimeMillis();
            Date tempDate = new Date();
            Status satus = new Status();
            satus.setId(generateRandomString(20) + k);
            satus.setArrivalStatus(generateRandomString(20));
            satus.setFlightReleaseStatus(generateRandomString(20));
            satus.setDepartureStatus(generateRandomString(20));
            satus.setLegStatus(generateRandomString(20));
            satus.setArrivalStatus(generateRandomString(20));
            satus.setArrivalStatus_timestamp(tempDate);
            satus.setDepartureStatus(generateRandomString(20));
            satus.setDepartureStatus_timestamp(tempDate);
            satus.setLegStatus(generateRandomString(20));
            satus.setLegStatus_timestamp(tempDate);
            satus.setPreviousLegStatus(generateRandomString(20));
            satus.setFlightReleaseStatus(generateRandomString(20));
            satus.setFlightReleaseStatus_timestamp(tempDate);
            satus.setStubbedFlightNumber(generateRandomString(10));
            satus.setStubbedFlightNumber_timestamp(tempDate);
            satus.setPreviousArrivalStatus(generateRandomString(20));
            satus.setPreviousDepartureStatus(generateRandomString(20));
            satus.setCancelledReason(generateRandomString(10));
            satus.setCancelledCode(generateRandomString(10));
            satus.setReasonCode(generateRandomString(10));
            satus.setReasonDescription(generateRandomString(10));
            satus.setPreviousArrivalStatus_timestamp(tempDate);
            satus.setPreviousDepartureStatus_timestamp(tempDate);
            satus.setPreviousLegStatus_timestamp(tempDate);
            satus.setNext_leg_recovery(getTempLKAFlightKey());
            satus.setPrev_leg_div(getTempLKAFlightKey());
            satus.setCreated(new Random().nextBoolean());
            satus.setThruFlight(new Random().nextBoolean());
            satus.setGroundInterrupted(new Random().nextBoolean());
            satus.setDelayReasonCodes(generateRandomString(10));
            satus.setDelayReasonCodes_timestamp(tempDate);
            satus.setSoarFlight(new Random().nextBoolean());
            satus.setFlightPlanReleaseCount(new Random().nextInt());
            satus.setSnapshots(new HashMap<>());
            satus.setPreviousLegStatus(generateRandomString(20));
            satus.setLegStatus(generateRandomString(20));
            statusRecordsList.add(satus);

            if (statusRecordsList.size() % BATCH_SIZE == 0) {
                gigaSpace.writeMultiple(statusRecordsList.toArray(), (long) leaseTimeout);
                statusRecordsList.clear();
                long endTime = System.currentTimeMillis();
                long duration = endTime - insertStartTime;
                log.info("Status Total time to write " + BATCH_SIZE + " entries is " + (duration / 1000F) + " in sec");
            }
        }
        long TotalendTime = System.currentTimeMillis();
        long TotalDuration = TotalendTime - TotalStartTime;
        log.info("Status Total time to write " + numOfObjects + " entries is " + (TotalDuration / 1000F) + " in sec");
        log.info("Status Average time to write 1 entries is " + ((maxObjects / TotalDuration) / 1000F) + " in sec");
        log.info(" ");
    }

    public static void generateCrewMemberFDPDataRecords(int numOfObjects) throws Exception {

        long TotalStartTime = System.currentTimeMillis();
        List<CrewMemberFDPData> crewMemberFDPDataRecordsList = new ArrayList<>();

        for (int k = 0; k < numOfObjects; k++) {
            long insertStartTime = System.currentTimeMillis();
            String tempString = "AA";
            Date tempDate = new Date();
            CrewMemberFDPData crewMemberFDPData = new CrewMemberFDPData();
            crewMemberFDPData.setFdpDataKey(generateRandomString(20) + k);
            crewMemberFDPData.setEmployeeNumber(generateRandomString(10));
            crewMemberFDPData.setSourceTimestamp(tempDate);
            crewMemberFDPData.setEventGMTEndDate(tempDate);
            crewMemberFDPData.setFlightDuration(new Random().nextInt());
            crewMemberFDPData.setEventGMTStartDate(tempDate);
            crewMemberFDPData.setFdpIndicators(generateRandomString(20));
            crewMemberFDPData.setAcclimatedIndicators(generateRandomString(20));
            crewMemberFDPData.setDutyPeriodNumber(new Random().nextInt());
            crewMemberFDPData.setSequenceNumber(generateRandomString(10));
            crewMemberFDPData.setSequenceOrignDate(tempDate);
            crewMemberFDPData.setPositionCode(generateRandomString(10));
            crewMemberFDPData.setDepLongitude(generateRandomString(10));
            crewMemberFDPData.setArvLongitude(generateRandomString(10));
            crewMemberFDPData.setAcclimatedTheaterLongitude(generateRandomString(10));
            crewMemberFDPData.setCurrentTheaterLongitude(generateRandomString(10));
            crewMemberFDPData.setCurrentAcclimatedTheaterStation(generateRandomString(10));
            crewMemberFDPData.setCurrentTheaterStation(generateRandomString(10));
            crewMemberFDPData.setFdpExtensionLimit(new Random().nextInt());
            crewMemberFDPData.setContractualMonth(generateRandomString(10));
            crewMemberFDPDataRecordsList.add(crewMemberFDPData);

            if (crewMemberFDPDataRecordsList.size() % BATCH_SIZE == 0) {
                gigaSpace.writeMultiple(crewMemberFDPDataRecordsList.toArray(), (long) leaseTimeout);
                crewMemberFDPDataRecordsList.clear();
                long endTime = System.currentTimeMillis();
                long duration = endTime - insertStartTime;
                log.info("CrewMemberFDPData Total time to write " + BATCH_SIZE + " entries is " + (duration / 1000F) + " in sec");
            }
        }
        long TotalendTime = System.currentTimeMillis();
        long TotalDuration = TotalendTime - TotalStartTime;
        log.info("CrewMemberFDPData Total time to write " + numOfObjects + " entries is " + (TotalDuration / 1000F) + " in sec");
        log.info("CrewMemberFDPData Average time to write 1 entries is " + ((maxObjects / TotalDuration) / 1000F) + " in sec");
        log.info(" ");
    }


    public static void readCrewDutyFlightDetailRecords(int numOfObjects) throws Exception {
        long TotalStartTime = System.currentTimeMillis();
        for (int k = 0; k < numOfObjects / BATCH_SIZE; k++) {
            long insertStartTime = System.currentTimeMillis();
            CrewDutyFlightDetail[] dataArray = gigaSpace.readMultiple(new CrewDutyFlightDetail(), BATCH_SIZE);
            List<CrewDutyFlightDetail> dataArrays = new ArrayList<CrewDutyFlightDetail>(Arrays.asList(dataArray));
            long endTime = System.currentTimeMillis();
            long duration = endTime - insertStartTime;
            log.info("CrewDutyFlightDetail Total time to Read " + dataArrays.size() + " entries is " + (duration / 1000F) + " in sec");
        }
        long TotalendTime = System.currentTimeMillis();
        long TotalDuration = TotalendTime - TotalStartTime;
        log.info("CrewDutyFlightDetail Total time to Read " + numOfObjects + " entries is " + (TotalDuration / 1000F) + " in sec");
        log.info("CrewDutyFlightDetail Average time to Read 1 entries is " + ((maxObjects / TotalDuration) / 1000F) + " in sec");
        log.info(" ");
    }

    public static void readFlightOperatingCrewRecords(int numOfObjects) throws Exception {
        long TotalStartTime = System.currentTimeMillis();
        for (int k = 0; k < numOfObjects / BATCH_SIZE; k++) {
            long insertStartTime = System.currentTimeMillis();
            FlightOperatingCrew[] dataArray = gigaSpace.readMultiple(new FlightOperatingCrew(), BATCH_SIZE);
            List<FlightOperatingCrew> dataArrays = new ArrayList<FlightOperatingCrew>(Arrays.asList(dataArray));
            long endTime = System.currentTimeMillis();
            long duration = endTime - insertStartTime;
            log.info("FlightOperatingCrew Total time to Read " + dataArrays.size() + " entries is " + (duration / 1000F) + " in sec");
        }
        long TotalendTime = System.currentTimeMillis();
        long TotalDuration = TotalendTime - TotalStartTime;
        log.info("FlightOperatingCrew Total time to Read " + numOfObjects + " entries is " + (TotalDuration / 1000F) + " in sec");
        log.info("FlightOperatingCrew Average time to Read 1 entries is " + ((maxObjects / TotalDuration) / 1000F) + " in sec");
        log.info(" ");
    }

    public static void readCrewsequencePositionDetailRecords(int numOfObjects) throws Exception {
        long TotalStartTime = System.currentTimeMillis();
        for (int k = 0; k < numOfObjects / BATCH_SIZE; k++) {
            long insertStartTime = System.currentTimeMillis();
            CrewSequencePositionDetail[] dataArray = gigaSpace.readMultiple(new CrewSequencePositionDetail(), BATCH_SIZE);
            List<CrewSequencePositionDetail> dataArrays = new ArrayList<CrewSequencePositionDetail>(Arrays.asList(dataArray));
            long endTime = System.currentTimeMillis();
            long duration = endTime - insertStartTime;
            log.info("CrewsequencePositionDetail Total time to Read " + dataArrays.size() + " entries is " + (duration / 1000F) + " in sec");
        }
        long TotalendTime = System.currentTimeMillis();
        long TotalDuration = TotalendTime - TotalStartTime;
        log.info("CrewsequencePositionDetail Total time to Read " + numOfObjects + " entries is " + (TotalDuration / 1000F) + " in sec");
        log.info("CrewsequencePositionDetail Average time to Read 1 entries is " + ((maxObjects / TotalDuration) / 1000F) + " in sec");
        log.info(" ");
    }

    public static void readRemainingFlightDutyRecords(int numOfObjects) throws Exception {
        long TotalStartTime = System.currentTimeMillis();
        for (int k = 0; k < numOfObjects / BATCH_SIZE; k++) {
            long insertStartTime = System.currentTimeMillis();
            RemainingFlightDuty[] dataArray = gigaSpace.readMultiple(new RemainingFlightDuty(), BATCH_SIZE);
            List<RemainingFlightDuty> dataArrays = new ArrayList<RemainingFlightDuty>(Arrays.asList(dataArray));
            long endTime = System.currentTimeMillis();
            long duration = endTime - insertStartTime;
            log.info("RemainingFlightDuty Total time to Read " + dataArrays.size() + " entries is " + (duration / 1000F) + " in sec");
        }
        long TotalendTime = System.currentTimeMillis();
        long TotalDuration = TotalendTime - TotalStartTime;
        log.info("RemainingFlightDuty Total time to Read " + numOfObjects + " entries is " + (TotalDuration / 1000F) + " in sec");
        log.info("RemainingFlightDuty Average time to Read 1 entries is " + ((maxObjects / TotalDuration) / 1000F) + " in sec");
        log.info(" ");
    }

    public static void readStatusRecords(int numOfObjects) throws Exception {
        long TotalStartTime = System.currentTimeMillis();
        for (int k = 0; k < numOfObjects / BATCH_SIZE; k++) {
            long insertStartTime = System.currentTimeMillis();
            Status[] dataArray = gigaSpace.readMultiple(new Status(), BATCH_SIZE);
            List<Status> dataArrays = new ArrayList<Status>(Arrays.asList(dataArray));
            long endTime = System.currentTimeMillis();
            long duration = endTime - insertStartTime;
            log.info("Status Total time to Read " + dataArrays.size() + " entries is " + (duration / 1000F) + " in sec");
        }
        long TotalendTime = System.currentTimeMillis();
        long TotalDuration = TotalendTime - TotalStartTime;
        log.info("Status Total time to Read " + numOfObjects + " entries is " + (TotalDuration / 1000F) + " in sec");
        log.info("Status Average time to Read 1 entries is " + ((maxObjects / TotalDuration) / 1000F) + " in sec");
        log.info(" ");
    }

    public static void readCrewMemberFDPDataRecords(int numOfObjects) throws Exception {
        long TotalStartTime = System.currentTimeMillis();
        for (int k = 0; k < numOfObjects / BATCH_SIZE; k++) {
            long insertStartTime = System.currentTimeMillis();
            CrewMemberFDPData[] dataArray = gigaSpace.readMultiple(new CrewMemberFDPData(), BATCH_SIZE);
            List<CrewMemberFDPData> dataArrays = new ArrayList<CrewMemberFDPData>(Arrays.asList(dataArray));
            long endTime = System.currentTimeMillis();
            long duration = endTime - insertStartTime;
            log.info("CrewMemberFDPData Total time to Read " + dataArrays.size() + " entries is " + (duration / 1000F) + " in sec");
        }
        long TotalendTime = System.currentTimeMillis();
        long TotalDuration = TotalendTime - TotalStartTime;
        log.info("CrewMemberFDPData Total time to Read " + numOfObjects + " entries is " + (TotalDuration / 1000F) + " in sec");
        log.info("CrewMemberFDPData Average time to Read 1 entries is " + ((maxObjects / TotalDuration) / 1000F) + " in sec");
        log.info(" ");
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
            AAMultithreadedFeeder feeder = new AAMultithreadedFeeder();

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

            generateCrewDutyFlightDetailRecords(maxObjects);
            generateCrewsequencePositionDetailRecords(maxObjects);
            generateStatusRecords(maxObjects);
            generateCrewMemberFDPDataRecords(maxObjects);
            generateFlightOperatingCrewRecords(maxObjects);
            generateRemainingFlightDutyRecords(maxObjects);


//            readCrewDutyFlightDetailRecords(maxObjects);
//            readCrewsequencePositionDetailRecords(maxObjects);
//            readStatusRecords(maxObjects);
//            readCrewMemberFDPDataRecords(maxObjects);
//            readFlightOperatingCrewRecords(maxObjects);
//            readRemainingFlightDutyRecords(maxObjects);


        } catch (Throwable t) {
            t.printStackTrace();
            ;
        }
    }

}
