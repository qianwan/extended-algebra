package com.yahoo.networkmimo;

import java.util.ArrayList;
import java.util.List;

public class Network {
    private final List<Cluster> clusters;

    public Network() {
        clusters = new ArrayList<Cluster>();
    }

    public Network addCluster(Cluster cluster) {
        clusters.add(cluster);
        for (BaseStation bs : cluster.getBSs()) {
            bs.setNetwork(this);
        }
        for (UE ue : cluster.getUEs()) {
            ue.setNetwork(this);
        }
        return this;
    }

    public Network addBaseStation(BaseStation bs) {
        //TODO
        return this;
    }

    public Network reCluster() {
        //TODO
        return this;
    }

    public List<Cluster> getClusters() {
        return clusters;
    }
}
