package com.yahoo.networkmimo;

import java.util.Map;

import com.beust.jcommander.internal.Maps;
import com.yahoo.algebra.matrix.DenseComplexMatrix;

public class BaseStation extends Entity {
    private Map<UE, DenseComplexMatrix> txPrecodingMatrix = Maps.newHashMap();

    private Cluster cluster;

    public BaseStation(double x, double y, int numAntennas) {
        super(x, y, Entity.Type.BS, numAntennas);
    }

    public void setTxPrecodingMatrx(UE ue, DenseComplexMatrix v) {
        txPrecodingMatrix.put(ue, v);
    }

    public DenseComplexMatrix getTxPrecodingMatrix(UE ue) {
        return txPrecodingMatrix.get(ue);
    }

    /**
     * @return the cluster
     */
    public Cluster getCluster() {
        return cluster;
    }

    /**
     * @param cluster
     *            the cluster to set
     */
    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }
}
