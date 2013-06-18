package com.yahoo.networkmimo;

import java.util.ArrayList;
import java.util.List;

public class Network {
    private final List<Cluster> clusters;

    private final List<BaseStation> bss;

    private final List<UE> ues;

    public Network() {
        clusters = new ArrayList<Cluster>();
        bss = new ArrayList<BaseStation>();
        ues = new ArrayList<UE>();
    }

    public Network addCluster(Cluster cluster) {
        cluster.setNetwork(this);
        clusters.add(cluster);
        for (BaseStation bs : cluster.getBSs()) {
            bs.setNetwork(this);
            bss.add(bs);
        }
        for (UE ue : cluster.getUEs()) {
            ue.setNetwork(this);
            ues.add(ue);
        }
        return this;
    }

    public Network addBaseStation(BaseStation bs) {
        bss.add(bs);
        if (bs.getCluster()==null) {
            // TODO add this bs to a cluster
        }
        return this;
    }

    public Network addUE(UE ue) {
        ues.add(ue);
        if (ue.getCluster()==null) {
            // TODO add this ue to a cluster
        }
        return this;
    }

    public Network reCluster() {
        // TODO
        return this;
    }

    public List<Cluster> getClusters() {
        return clusters;
    }

    public List<BaseStation> getBSs() {
        return bss;
    }

    public List<UE> getUEs() {
        return ues;
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
     * alloc txPreMatrix, rxPreMatrix and user weight
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
