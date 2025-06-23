package com.distributedsearch.cluster.management;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceRegistry implements Watcher {

    private static final String REGISTRY_ZNODE = "/service_registry";

    private final ZooKeeper zooKeeper;

    private String currentZnode;

    private List<String> allServiceAddresses;

    public ServiceRegistry(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
        createServiceRegistryZnode();
    }

    public void registerToCluster(String metadata) throws InterruptedException, KeeperException {
        currentZnode = zooKeeper.create(REGISTRY_ZNODE + "/n_",
                metadata.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("Registered to service registry");
    }

    public boolean isNodeRegisteredWithCluster() {
        return (null != currentZnode);
    }

    public void registerForUpdates() {
        try {
            updateAddresses();
        } catch (InterruptedException | KeeperException ignored) {
        }
    }

    public synchronized List<String> getAllServiceAddresses() throws InterruptedException, KeeperException {
        if (null == allServiceAddresses) {
            updateAddresses();
        }
        return this.allServiceAddresses;
    }

    /**
     * In case a worker node gracefully shuts down or suddenly becomes
     * a leader where it does not need to communicate with itself (
     * In our architecture, a leader node needs to 'discover' a worker
     * node and assign it some work. That is what we mean by 'communicate'),
     * we have to delete the node.
     *
     * @throws InterruptedException {@inheritDoc}
     * @throws KeeperException {@inheritDoc}
     */
    public void unregisterFromCluster() throws InterruptedException, KeeperException {
        if (null != currentZnode && null != zooKeeper.exists(currentZnode, false)) {
            zooKeeper.delete(currentZnode, -1);
        }
    }

    private synchronized void updateAddresses() throws InterruptedException, KeeperException {
        List<String> workerZnodes = zooKeeper.getChildren(REGISTRY_ZNODE, this);

        List<String> addresses = new ArrayList<>(workerZnodes.size());

        for (String workerZnode: workerZnodes) {
            String workerZnodeFullPath = REGISTRY_ZNODE + "/" + workerZnode;
            Stat stat = zooKeeper.exists(workerZnodeFullPath, this);
            if (null == stat) {
                continue;
            }

            byte[] data = zooKeeper.getData(workerZnodeFullPath, false, stat);
            addresses.add(new String(data));
        }

        this.allServiceAddresses = Collections.unmodifiableList(addresses);
        System.out.println("The cluster addresses are: " + allServiceAddresses);
    }

    /**
     * Zookeeper ensures that only one call to create a
     * znode on a particular path succeeds, so no need to
     * handle any race conditions here.
     */
    private void createServiceRegistryZnode() {
        try {
            if (null == zooKeeper.exists(REGISTRY_ZNODE, false)) {
                zooKeeper.create(REGISTRY_ZNODE, new byte[]{},
                        ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        try {
            updateAddresses();
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }
}
