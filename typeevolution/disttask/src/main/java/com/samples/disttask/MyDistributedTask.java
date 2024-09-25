package com.samples.disttask;

import com.gigaspaces.async.AsyncResult;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.executor.DistributedTask;

import java.util.*;
import java.util.logging.Logger;

public class MyDistributedTask extends MyTask implements DistributedTask<Integer, Integer> {

    private static Logger log = Logger.getLogger(com.samples.disttask.MyDistributedTask.class.getName());

    GigaSpace gigaSpace;
    MyDistributedTask(Object template, Class clazz) {
        super(template, clazz);
    }

    @Override
    public Integer reduce(List<AsyncResult<Integer>> asyncResults) throws Exception {

        int entriesProcessed = 0;
        for (AsyncResult<Integer> result : asyncResults) {
            if (result.getException() != null) {
                throw result.getException();
            }
            entriesProcessed += result.getResult();

        }

        // drop old type
        // gigaSpace.clear(template)
        return entriesProcessed;
    }

}
