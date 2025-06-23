package com.distributedsearch;

import com.distributedsearch.cluster.management.ClusterManager;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;

public class DistributedSearchApplication {

    private static final int DEFAULT_SERVER_PORT = 8080;

    public static void main(String[] args) {
        ClusterManager clusterManager = new ClusterManager();
        try {
            int serverPort = args.length == 1 ? Integer.parseInt(args[0]): DEFAULT_SERVER_PORT;
            clusterManager.registerNodeWithClusterEnableDiscovery(serverPort);
        } catch (IOException | InterruptedException | KeeperException e) {
            throw new RuntimeException(e);
        }
    }

}
