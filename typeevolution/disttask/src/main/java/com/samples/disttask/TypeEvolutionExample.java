package com.samples.disttask;

import com.gigaspaces.async.AsyncFuture;
import com.samples.model.v1.Data;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.SpaceProxyConfigurer;

import java.util.logging.Logger;

public class TypeEvolutionExample {

    private static Logger log = Logger.getLogger(TypeEvolutionExample.class.getName());
    private GigaSpace gigaSpace;

    private static final String SPACE_NAME = "demo";


    public TypeEvolutionExample() {
        // create remote proxy
        SpaceProxyConfigurer configurer = new SpaceProxyConfigurer(SPACE_NAME);
        gigaSpace = new GigaSpaceConfigurer(configurer).gigaSpace();
    }

    public void distributedTaskExample() throws Exception {
        log.info("Begin distributed task");

        com.samples.model.v1.Data template = new com.samples.model.v1.Data();
        AsyncFuture<Integer> future = gigaSpace.execute(new MyDistributedTask(template, com.samples.model.v2.Data.class));
        Integer result = future.get();

        if (result == null || result == 0) {
            log.info("No results were returned.");
        } else {
            String s = String.format("The number of %s entries processed: %d ", template.getClass().getName(), result);
            log.info(s);
        }
    }

    public static void main(String[] args) throws Exception{

        TypeEvolutionExample example = new TypeEvolutionExample();
        example.distributedTaskExample();
    }

}
