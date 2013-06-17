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

    public void optimize() {
        initialize();
    }

    /**
     * Generate a feasible set of txPreMatrix, rxPreMatrix and user weight
     * @throws  
     */
    protected void initialize()  {
        for (Cluster cluster : clusters) {
            for (BaseStation bs : cluster.getBSs()) {
                bs.genRandomTxPrecodingMatrix();
            }
            cluster.initTxPrecodingMatrix();
            cluster.updateTxPrecodingMatrix();
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
        
    }
}
