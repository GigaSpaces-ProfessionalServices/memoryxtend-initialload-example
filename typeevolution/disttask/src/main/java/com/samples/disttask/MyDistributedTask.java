package com.samples.disttask;

import com.gigaspaces.async.AsyncResult;

import com.gigaspaces.client.iterator.SpaceIterator;
import com.gigaspaces.client.iterator.SpaceIteratorConfiguration;
import com.gigaspaces.client.iterator.SpaceIteratorType;
import com.samples.model.SpaceTypeEvolutionAdapter;
import com.samples.model.SpaceTypeEvolutionAdapterImpl;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.executor.DistributedTask;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.logging.Logger;

public class MyDistributedTask extends MyTask implements DistributedTask<Integer, Integer> {

    private static Logger log = Logger.getLogger(com.samples.disttask.MyDistributedTask.class.getName());

    GigaSpace gigaSpace;
    MyDistributedTask(Object template, String destClassName) {
        super(template, destClassName);
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
