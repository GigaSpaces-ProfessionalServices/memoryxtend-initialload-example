package com.space;

import com.gigaspaces.admin.quiesce.QuiesceToken;
import com.gigaspaces.admin.quiesce.QuiesceTokenFactory;
import com.gigaspaces.internal.server.space.IRemoteSpace;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.space.mode.PrePrimary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.List;

public class PreprimaryBean {

    private final Logger logger = LoggerFactory.getLogger(PreprimaryBean.class.getName());

    GigaSpace gs;

    public GigaSpace getGs() {
        return gs;
    }

    public void setGs(GigaSpace gs) {
        this.gs = gs;
    }

    @PrePrimary
    public void quiesceSpace() {
        final QuiesceToken token = QuiesceTokenFactory.createUUIDToken();
        List<IRemoteSpace> allAvailableSpaces = gs.getSpace().getDirectProxy().getProxyRouter().getAllAvailableSpaces();
        //Assert.assertTrue(allAvailableSpaces.size() > 0);
        String description = "quiesce space";
        logger.info("Before quiesce");
        System.out.println("Before quiesce");
        for (IRemoteSpace remoteSpace : allAvailableSpaces) {
            try {
                    remoteSpace.quiesce(description, token);
                    logger.info("quiesce space, description:" + description + ", token: " + token + ", remoteSpace: " + remoteSpace.getName());
                    System.out.println("quiesce space, description:" + description + ", token: " + token + ", remoteSpace: " + remoteSpace.getName());
            } catch (RemoteException ex) {
                logger.error("RemoteException ", ex);
            }
        }
    }
}
