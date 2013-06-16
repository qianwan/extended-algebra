package com.yahoo.networkmimo;

import com.yahoo.algebra.matrix.DenseComplexMatrix;

public class UE extends Entity {
    /**
     * Receiving precoding matrix
     */
    private DenseComplexMatrix rxPrecodingMatrix = null;

    private int numStreams;

    private Cluster cluster;

    public UE(double x, double y, int numAntennas) {
        super(x, y, Entity.Type.UE, numAntennas);
    }

    public UE(double x, double y, int numAntennas, int numStreams) {
        super(x, y, Entity.Type.UE, numAntennas);
        this.numStreams = numStreams;
    }

    /**
     * @return the numStreams
     */
    public int getNumStreams() {
        return numStreams;
    }

    /**
     * @param numStreams
     *            the numStreams to set
     */
    public void setNumStreams(int numStreams) {
        this.numStreams = numStreams;
    }

    /**
     * @return the rxPrecodingMatrix
     */
    public DenseComplexMatrix getRxPrecodingMatrix() {
        return rxPrecodingMatrix;
    }

    /**
     * @param rxPrecodingMatrix
     *            the rxPrecodingMatrix to set
     */
    public void setRxPrecodingMatrix(DenseComplexMatrix rxPrecodingMatrix) {
        this.rxPrecodingMatrix = rxPrecodingMatrix;
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
