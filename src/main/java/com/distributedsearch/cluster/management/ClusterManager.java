package com.distributedsearch.cluster.management;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

public class ClusterManager implements Watcher {

    private ZooKeeper zookeeper;

    private static final String ZOOKEEPER_ADDRESS = "localhost:2181";

    private static final int ZOOKEEPER_SERVER_TIMEOUT = 3000;

    public void registerNodeWithClusterEnableDiscovery(int serverPort)
            throws IOException, InterruptedException, KeeperException {
        connectToZookeper();

        ServiceRegistry serviceRegistry = new ServiceRegistry(zookeeper);
        OnElectionCallback onElectionCallback = new OnElectionAction(serviceRegistry, serverPort);

        LeaderElection leaderElection = new LeaderElection(zookeeper, onElectionCallback);
        leaderElection.discoverLeader();
        run();
        close();
        System.out.println("Disconnected from zookeeper, exiting application...");
    }

    private void connectToZookeper() throws IOException {
        this.zookeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, ZOOKEEPER_SERVER_TIMEOUT, this);
    }

    private void run() throws InterruptedException {
        synchronized (zookeeper) {
            zookeeper.wait();
        }
    }

    private void close() throws InterruptedException {
        zookeeper.close();
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()) {
            case None:
                if (watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    System.out.println("Successfully connected to zookeeper");
                } else {
                    synchronized (zookeeper) {
                        System.out.println("Disconnected from zookeeper event");
                        zookeeper.notifyAll();
                    }
                }
                break;
        }
    }
}
