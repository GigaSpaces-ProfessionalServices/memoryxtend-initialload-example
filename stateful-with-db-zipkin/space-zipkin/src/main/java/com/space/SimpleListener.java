package com.space;

import org.openspaces.core.GigaSpace;
import org.openspaces.events.EventDriven;
import org.openspaces.events.SpaceDataEventListener;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.notify.Notify;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;

public class SimpleListener {
    private final Logger logger= LoggerFactory.getLogger(SimpleListener.class.getName());

    @SpaceDataEvent
    public void eventListener(Object event) {
        logger.info("lease is expired for object of type : "+event.getClass().getName());
    }
}