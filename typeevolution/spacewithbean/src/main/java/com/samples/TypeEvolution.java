package com.samples;

import com.gigaspaces.client.iterator.SpaceIterator;
import com.gigaspaces.client.iterator.SpaceIteratorConfiguration;
import com.gigaspaces.client.iterator.SpaceIteratorType;
import com.samples.model.SpaceTypeEvolutionAdapter;
import com.samples.model.SpaceTypeEvolutionAdapterImpl;
import org.openspaces.core.GigaSpace;

import javax.annotation.Resource;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.logging.Logger;


public class TypeEvolution {


    private static Logger log = Logger.getLogger(com.samples.TypeEvolution.class.getName());

    private boolean enabled;

    private List<String> srcClassList;

    private List<String> destClassList;

    private List<String> adapterClassList;

    @Resource
    private GigaSpace gigaSpace;

    public TypeEvolution(boolean enabled, List srcClassList, List destClassList, List adapterClassList) {
        this.enabled = enabled;
        this.srcClassList = srcClassList;
        this.destClassList = destClassList;
        this.adapterClassList = adapterClassList;
    }

    public void postConstruct() throws Exception {
        boolean process = true;
        if (enabled == false) {
            log.info("This bean is not enabled.");
            return;
        }
        if( srcClassList == null || srcClassList.size() == 0 ) {
            log.warning("The source class list cannot be null or empty.");
            process = false;
        }
        if( destClassList == null || destClassList.size() == 0 ) {
            log.warning("The destination class list cannot be null or empty.");
            process = false;
        }
        if( adapterClassList == null || adapterClassList.size() == 0 ) {
            log.warning("The adapter class list cannot be null or empty.");
            process = false;
        }

        if (srcClassList.size() != destClassList.size() && srcClassList.size() != adapterClassList.size()) {
            log.warning(String.format("The size of the source class list is :%d. The size of the destination class list is :%d. The size of the adapter class list is: %d.",
                    srcClassList.size(), destClassList.size(), adapterClassList.size()));
            log.warning(String.format("The sizes of the source class list, destination class list, adapter class list should be equal"));
            process = false;
        }
        if (process == false) {
            return;
        }

        for (int i=0; i < srcClassList.size(); i++) {
            processClass(srcClassList.get(i), destClassList.get(i), adapterClassList.get(i));
        }

    }

    private void processClass(String srcClassName, String destClassName, String adapterClassName) throws Exception {
        Class<?> srcClazz = Class.forName(srcClassName);
        // get no argument constructor
        Constructor<?> srcCtor = srcClazz.getConstructor();
        Object template = srcCtor.newInstance(new Object[]{});

        Class<?> destClazz = Class.forName(destClassName);
        // get no argument constructor
        Constructor<?> destCtor = destClazz.getConstructor();

        Class<?> adapterClazz = Class.forName(adapterClassName);
        // get no argument constructor
        Constructor<?> adapterCtor = adapterClazz.getConstructor();
        SpaceTypeEvolutionAdapter adapter = (SpaceTypeEvolutionAdapter) adapterCtor.newInstance(new Object[]{});

        // set up iterator
        SpaceIteratorConfiguration spaceIteratorConfiguration = new SpaceIteratorConfiguration()
                .setIteratorType(SpaceIteratorType.CURSOR)
                .setBatchSize(100);

        log.info(String.format("Processing from %s to %s", srcClassName, destClassName));

        int entriesProcessed = 0;
        SpaceIterator spaceIterator = gigaSpace.iterator(template, spaceIteratorConfiguration);
        while(spaceIterator.hasNext()) {
            Object srcObject = spaceIterator.next();
            Object destObject = destCtor.newInstance(new Object[]{});
            adapter.convertType(srcObject, destObject);
            gigaSpace.write(destObject);
            gigaSpace.clear(srcObject);
            entriesProcessed ++;
        }
        log.info(String.format("The number of entries processed is: " + entriesProcessed));
    }

}
