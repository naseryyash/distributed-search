package com.distributedsearch.cluster.management;

import org.apache.zookeeper.KeeperException;

import java.net.Inet4Address;
import java.net.UnknownHostException;

public class OnElectionAction implements OnElectionCallback {

    private final ServiceRegistry serviceRegistry;

    private final int port;

    public OnElectionAction(ServiceRegistry serviceRegistry, int port) {
        this.serviceRegistry = serviceRegistry;
        this.port = port;
    }

    @Override
    public void onElectedToBeLeader() {
        try {
            serviceRegistry.unregisterFromCluster();
            serviceRegistry.registerForUpdates();
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWorker() {
        try {
            if (!serviceRegistry.isNodeRegisteredWithCluster()) {
                String currentServerAddress = String.format("http://%s:%d",
                        Inet4Address.getLocalHost().getHostAddress(),
                        port);

                serviceRegistry.registerToCluster(currentServerAddress);
            }
        } catch (UnknownHostException | InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }
}
