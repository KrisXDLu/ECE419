package server;

import common.KVMessage;
import ecs.ECSHashRing;
import ecs.ECSNode;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class manages a pool of Forwarders and update them when
 * hashRing updates
 */
public class KVServerForwarderManager {
    private static Logger logger = Logger.getRootLogger();
    private ECSNode self;
    private List<KVServerForwarder> forwarderList;

    public KVServerForwarderManager(String name, String host, Integer port) {
        this.forwarderList = new ArrayList<>();
        this.self = new ECSNode(name + "_forwarder", host, port);
    }

    /**
     * Update the forwarderList based on information in hashRing provided
     *
     * @param hashRing hashRing object
     * @throws IOException socket connection issue
     */
    public void update(ECSHashRing hashRing) throws IOException {
        ECSNode node = hashRing.getNodeByKey(self.getNodeHash());
        List<KVServerForwarder> newList = hashRing.getReplicationNodes(node).stream()
                .map(KVServerForwarder::new).collect(Collectors.toList());

        for (KVServerForwarder forwarder : this.forwarderList) {
            // Remove forwarder not longer active
            if (!newList.contains(forwarder)) {
                logger.info(self.getNodeName() + " disconnect from " + forwarder);
                forwarder.disconnect();
                this.forwarderList.remove(forwarder);
            }
        }

        for (KVServerForwarder forwarder : newList) {
            if (!this.forwarderList.contains(forwarder)) {
                logger.info(self.getNodeName() + " connects to " + forwarder.getName());
                forwarder.setPrompt(self.getNodeName() + " to " + forwarder.getName());
                forwarder.connect();
                this.forwarderList.add(forwarder);
            }
        }
    }

    public void forward(KVMessage message) throws IOException,
            KVServerForwarder.ForwardFailedException {
        for (KVServerForwarder forwarder : this.forwarderList) {
            forwarder.forward(message);
        }
    }

    public void clear() {
        for (KVServerForwarder forwarder : this.forwarderList) {
            forwarder.disconnect();
        }
        forwarderList.clear();
    }
}
