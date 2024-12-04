package com.space;

import com.gigaspaces.lrmi.LRMIInvocationContext;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.SpaceContext;
import com.j_spaces.core.filters.FilterOperationCodes;
import com.j_spaces.core.filters.ISpaceFilter;
import com.j_spaces.core.filters.entry.ISpaceFilterEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class MyFilter implements ISpaceFilter {
    private final Logger logger= LoggerFactory.getLogger(MyFilter.class.getName());
    HashMap<Integer, String> hashMap = new HashMap<>();

    @Override
    public void init(IJSpace ijSpace, String s, String s1, int i) throws RuntimeException {
        hashMap.put(FilterOperationCodes.AFTER_WRITE,"AFTER_WRITE");
        hashMap.put(FilterOperationCodes.AFTER_UPDATE,"AFTER_UPDATE");
        hashMap.put(FilterOperationCodes.AFTER_READ_MULTIPLE,"AFTER_READ_MULTIPLE");
        hashMap.put(FilterOperationCodes.AFTER_TAKE_MULTIPLE,"AFTER_TAKE_MULTIPLE");
        hashMap.put(FilterOperationCodes.AFTER_NOTIFY_TRIGGER,"AFTER_NOTIFY_TRIGGER");
        hashMap.put(FilterOperationCodes.AFTER_ALL_NOTIFY_TRIGGER,"AFTER_ALL_NOTIFY_TRIGGER");
        hashMap.put(FilterOperationCodes.AFTER_EXECUTE,"AFTER_EXECUTE");
        hashMap.put(FilterOperationCodes.AFTER_READ,"AFTER_READ");
        hashMap.put(FilterOperationCodes.AFTER_TAKE,"AFTER_TAKE");
        hashMap.put(FilterOperationCodes.AFTER_CHANGE,"AFTER_CHANGE");
        hashMap.put(FilterOperationCodes.AFTER_REMOVE,"AFTER_REMOVE");

        hashMap.put(FilterOperationCodes.BEFORE_TAKE,"BEFORE_TAKE");
        hashMap.put(FilterOperationCodes.BEFORE_TAKE_MULTIPLE,"BEFORE_TAKE_MULTIPLE");
        hashMap.put(FilterOperationCodes.BEFORE_WRITE,"BEFORE_WRITE");
        hashMap.put(FilterOperationCodes.BEFORE_CHANGE,"BEFORE_CHANGE");
        hashMap.put(FilterOperationCodes.BEFORE_AUTHENTICATION,"BEFORE_AUTHENTICATION");
        hashMap.put(FilterOperationCodes.BEFORE_ALL_NOTIFY_TRIGGER,"BEFORE_ALL_NOTIFY_TRIGGER");
        hashMap.put(FilterOperationCodes.BEFORE_CLEAN_SPACE,"BEFORE_CLEAN_SPACE");
        hashMap.put(FilterOperationCodes.BEFORE_EXECUTE,"BEFORE_EXECUTE");
        hashMap.put(FilterOperationCodes.BEFORE_GETADMIN,"BEFORE_GETADMIN");
        hashMap.put(FilterOperationCodes.BEFORE_REMOVE,"BEFORE_REMOVE");
        hashMap.put(FilterOperationCodes.BEFORE_UPDATE,"BEFORE_UPDATE");
        hashMap.put(FilterOperationCodes.BEFORE_READ_MULTIPLE,"BEFORE_READ_MULTIPLE");
        hashMap.put(FilterOperationCodes.BEFORE_NOTIFY,"BEFORE_NOTIFY");
        hashMap.put(FilterOperationCodes.BEFORE_NOTIFY_TRIGGER,"BEFORE_NOTIFY_TRIGGER");
        hashMap.put(FilterOperationCodes.BEFORE_READ,"BEFORE_READ");
    }

    @Override
    public void process(SpaceContext spaceContext, ISpaceFilterEntry iSpaceFilterEntry, int operactionCode) throws RuntimeException {
//            if (operactionCode == FilterOperationCodes.BEFORE_TAKE || operactionCode == FilterOperationCodes.BEFORE_TAKE_MULTIPLE) {
                 printLog(iSpaceFilterEntry.getClassName());
//            }
            printLog(operactionCode);
    }

    @Override
    public void process(SpaceContext spaceContext, ISpaceFilterEntry[] iSpaceFilterEntries, int operactionCode) throws RuntimeException {
//            if (operactionCode == FilterOperationCodes.BEFORE_TAKE || operactionCode == FilterOperationCodes.BEFORE_TAKE_MULTIPLE) {
                if (iSpaceFilterEntries != null && iSpaceFilterEntries.length > 0){
                    printLog(String.valueOf(iSpaceFilterEntries[0].getClassName()));
                }
//            }
            printLog(operactionCode);
    }


    protected void printLog(int operactionCode) throws RuntimeException {
        String operation = hashMap.get(operactionCode);
        try {
            String hostName = "embedded host";
            int port = 0;
            try {
                if (LRMIInvocationContext.getEndpointAddress() != null) {
                    hostName = LRMIInvocationContext.getEndpointAddress().getHostName();
                    port = LRMIInvocationContext.getEndpointAddress().getPort();
                }
            } catch (Exception e){
                logger.error("Something wrong in LRMIInvocationContext.getEndpointAddress");
            }
            throw new Exception(operation + " operation is preformed, end point host:"+ hostName + ":" + port);
        }
        catch (Exception e){
            logger.error("======" + operation + "=====", e);
        }
    }


    protected void printLog(String typeName) throws RuntimeException {
        logger.info("*************************** Type *************************** -> "+ typeName);
    }

    @Override
    public void close() throws RuntimeException {

    }
}