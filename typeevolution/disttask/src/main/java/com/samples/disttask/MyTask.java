package com.samples.disttask;

import com.gigaspaces.client.iterator.SpaceIterator;
import com.gigaspaces.client.iterator.SpaceIteratorConfiguration;
import com.gigaspaces.client.iterator.SpaceIteratorType;
import com.samples.model.SpaceTypeEvolutionAdapter;
import com.samples.model.v1.Data;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.executor.Task;
import org.openspaces.core.executor.TaskGigaSpace;

import java.util.ArrayList;
import java.util.List;

public class MyTask implements Task<Integer> {

    @TaskGigaSpace
    transient GigaSpace gigaSpace;

    // the class to process
    Object template;
    Class clazz;

    MyTask(Object template, Class clazz) {
        this.template = template;
        this.clazz = clazz;
    }

    // returns the number of objects processed
    @Override
    public Integer execute() throws Exception {
        int entriesProcessed = 0;

        SpaceIteratorConfiguration spaceIteratorConfiguration = new SpaceIteratorConfiguration()
                .setIteratorType(SpaceIteratorType.CURSOR)
                .setBatchSize(100);

        SpaceIterator spaceIterator = gigaSpace.iterator(template, spaceIteratorConfiguration);
        while(spaceIterator.hasNext()) {
            SpaceTypeEvolutionAdapter adapter = (SpaceTypeEvolutionAdapter) spaceIterator.next();
            adapter.convertType(clazz);
            entriesProcessed ++;
        }
        return entriesProcessed;
    }

}
