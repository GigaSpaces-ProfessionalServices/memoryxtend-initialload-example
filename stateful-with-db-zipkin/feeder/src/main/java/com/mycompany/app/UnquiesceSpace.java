package com.mycompany.app;

import com.gigaspaces.internal.server.space.IRemoteSpace;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.SpaceProxyConfigurer;

import java.rmi.RemoteException;
import java.util.List;

public class UnquiesceSpace {
    
    public static void main(String[] args) {
        String spaceName = args[0];
        SpaceProxyConfigurer spaceProxyConfigurer = new SpaceProxyConfigurer(spaceName);
        GigaSpace gs = new GigaSpaceConfigurer(spaceProxyConfigurer).gigaSpace();
        List<IRemoteSpace> allAvailableSpaces = gs.getSpace().getDirectProxy().getProxyRouter().getAllAvailableSpaces();
        System.out.println("Before unquiesce");
        for (IRemoteSpace remoteSpace : allAvailableSpaces) {
            try {
                if (remoteSpace.isActive()) { //primary Space
                    remoteSpace.unquiesce();
                    System.out.println("unquiesce space, remoteSpace: " + remoteSpace.getName());
                }
            } catch (RemoteException ex) {
                System.out.println("" + ex);
            }
        }
    }
}
