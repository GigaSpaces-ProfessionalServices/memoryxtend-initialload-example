package com.samples.disttask;

import com.gigaspaces.client.iterator.SpaceIterator;
import com.gigaspaces.client.iterator.SpaceIteratorConfiguration;
import com.gigaspaces.client.iterator.SpaceIteratorType;
import com.samples.model.SpaceTypeEvolutionAdapter;
import com.samples.model.SpaceTypeEvolutionAdapterImpl;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.executor.Task;
import org.openspaces.core.executor.TaskGigaSpace;

import java.lang.reflect.Constructor;

public class MyTask implements Task<Integer> {

    @TaskGigaSpace
    transient GigaSpace gigaSpace;

    // the class to process
    Object template;
    String destClassName;

    MyTask(Object template, String destClassName) {
        this.template = template;
        this.destClassName = destClassName;
    }

    // returns the number of objects processed
    @Override
    public Integer execute() throws Exception {
        int entriesProcessed = 0;

        Class<?> clazz = Class.forName(destClassName);
        // get no argument constructor
        Constructor<?> ctor = clazz.getConstructor();

        SpaceIteratorConfiguration spaceIteratorConfiguration = new SpaceIteratorConfiguration()
                .setIteratorType(SpaceIteratorType.CURSOR)
                .setBatchSize(100);

        SpaceIterator spaceIterator = gigaSpace.iterator(template, spaceIteratorConfiguration);
        while(spaceIterator.hasNext()) {
            Object srcObject = spaceIterator.next();
            Object destObject = ctor.newInstance(new Object[]{});
            SpaceTypeEvolutionAdapter adapter = new SpaceTypeEvolutionAdapterImpl();
            adapter.convertType(srcObject, destObject);
            gigaSpace.write(destObject);
            entriesProcessed ++;
        }
        return entriesProcessed;
    }

}
