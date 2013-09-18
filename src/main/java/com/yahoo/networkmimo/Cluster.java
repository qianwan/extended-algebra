package com.yahoo.networkmimo;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.yahoo.algebra.matrix.ComplexMatrix;
import com.yahoo.networkmimo.exception.ClusterNotReadyException;

public class Cluster extends Entity {
    private static final Logger logger = LoggerFactory.getLogger(Cluster.class);

    private Network network;

    private final Set<BaseStation> bss = Sets.newHashSet();

    private final Set<UE> ues = Sets.newHashSet();

    private final Set<Cluster> closure = Sets.newHashSet();

    public Cluster() {
        super();
        setType(Entity.Type.CLUSTER);
    }

    public Cluster(double x, double y) {
        super(x, y);
        setType(Entity.Type.CLUSTER);
    }

    public Cluster addBaseStation(BaseStation bs) {
        bss.add(bs);
        bs.setCluster(this);
        bs.setNetwork(network);
        if (network!=null) {
            network.addBaseStation(bs);
        }
        setNumAntennas(getNumAntennas() + bs.getNumAntennas());
        logger.debug("Add " + bs + " to " + this + " in " + network);
        return this;
    }

    public Cluster addUE(UE ue) {
        ues.add(ue);
        ue.setCluster(this);
        ue.setNetwork(network);
        if (network!=null) {
            network.addUE(ue);
        }
        logger.debug("Add " + ue + " to " + this + " in " + network);
        return this;
    }

    public Set<Cluster> getClusterClosure() {
        if (!closure.isEmpty()) return closure;

        if (network == null) {
            throw new ClusterNotReadyException("this cluster is not add to any network, so cannot get closure");
        }

        double closureDistance = network.getClosureDistance();
        for (Cluster cluster : network.getClusters()) {
            if (Utils.getEntityDistance(this, cluster) < closureDistance)
                closure.add(cluster);
        }
        return closure;
    }

    /**
     * @return the bss
     */
    public Set<BaseStation> getBSs() {
        return bss;
    }

    /**
     * @return the ues
     */
    public Set<UE> getUEs() {
        return ues;
    }

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    @Override
    public String toString() {
        return String.format("cluster@(%.4f,%.4f)", getXY()[0], getXY()[1]);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return false;
    }

    @Override
    public ComplexMatrix getMIMOChannel(Entity e) {
        logger.error("");
        throw new ClusterNotReadyException("Do not need to use channel from clusters to other entities");
    }
}
