package com.yahoo.networkmimo;

import java.util.ArrayList;
import java.util.List;

public class Network {
    private final List<Cluster> clusters;

    public Network() {
        clusters = new ArrayList<Cluster>();
    }

    public Network addCluster(Cluster cluster) {
        cluster.setNetwork(this);
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
        // TODO
        return this;
    }

    public Network reCluster() {
        // TODO
        return this;
    }

    public List<Cluster> getClusters() {
        return clusters;
    }

    public void optimize() {
        alloc();
        for (int i = 0; i < 10; ++i) {
            updateUERxPreMatrix();
            updateWeight();
            for (Cluster cluster : clusters) {
                cluster.calcJMatrix();
                for (UE ue : cluster.getUEs()) {
                    ue.calcDMatrix();
                }
            }
            for (Cluster cluster : clusters) {
                for (@SuppressWarnings("unused") BaseStation bs : cluster.getBSs()) {
                    // TODO
                }
            }
        }
    }

    /**
     * Generate a feasible set of txPreMatrix, rxPreMatrix and user weight
     * 
     * @throws
     */
    protected void alloc() {
        for (Cluster cluster : clusters) {
            cluster.alloc();
        }
    }

    /**
     * Calculate receive precoding matrix for every UE
     */
    protected void updateUERxPreMatrix() {
        for (Cluster cluster : clusters) {
            for (UE ue : cluster.getUEs()) {
                ue.calcRxPreMatrixAndRate();
            }
        }
    }

    /**
     * 
     */
    protected void updateWeight() {
        for (Cluster cluster : clusters) {
            for (UE ue : cluster.getUEs()) {
                ue.updateMMSEWeigh();
            }
        }
    }

    public void init() {
        for (Cluster cluster : clusters) {
            cluster.genRandomTxPreMatrix();
            for (Cluster c : clusters) {
                for (UE ue : c.getUEs()) {
                    cluster.genMIMOChannel(ue);
                }
            }
        }
    }
}
