package com.samples.feeder;

import com.samples.model.v1.Data;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.SpaceProxyConfigurer;

import java.util.logging.Logger;

public class Feeder {

    private static Logger log = Logger.getLogger(Feeder.class.getName());
    private GigaSpace gigaSpace;

    private static final String SPACE_NAME = "demo";

    public Feeder() {
        SpaceProxyConfigurer configurer = new SpaceProxyConfigurer(SPACE_NAME);
        gigaSpace = new GigaSpaceConfigurer(configurer).gigaSpace();
    }

    private void feed() {

        int count = 20;
        log.info(String.format("About to write %d items.", count));


        //int base = 10000;
        int base = 0;
        for(int i=0; i<count; i++) {
            Data data = new Data();
            data.setId(i);
            String s = new String(new char[i]).replace('\0', 'a');
            data.setMessage(s);
            data.setProcessed(false);
            gigaSpace.write(data);
        }
    }

    public static void main(String[] args) throws Exception{
        Feeder example = new Feeder();
        example.feed();
    }
}
