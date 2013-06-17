package com.yahoo.networkmimo;

import com.yahoo.algebra.matrix.ComplexMatrices;
import com.yahoo.algebra.matrix.ComplexMatrix;
import com.yahoo.algebra.matrix.DenseComplexMatrix;

public class UE extends Entity {
    /**
     * Receiving precoding matrix
     */
    private final ComplexMatrix rxPrecodingMatrix;

    private int numStreams;

    private Cluster cluster;

    private Network network;

    /**
     * Weight matrix for MMSE
     */
    private final ComplexMatrix mmseWeight;

    /**
     * Shannon rate
     */
    private double rate;

    // public UE(double x, double y, int numAntennas) {
    // super(x, y, Entity.Type.UE, numAntennas);
    // }

    public UE(double x, double y, int numAntennas, int numStreams) {
        super(x, y, Entity.Type.UE, numAntennas);
        this.numStreams = numStreams;
        rxPrecodingMatrix = new DenseComplexMatrix(numStreams, numAntennas);
        mmseWeight = new DenseComplexMatrix(numStreams, numStreams);
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
    public ComplexMatrix getRxPrecodingMatrix() {
        return rxPrecodingMatrix;
    }

    /**
     * @param rxPrecodingMatrix
     *            the rxPrecodingMatrix to set
     */
    public void setRxPrecodingMatrix(ComplexMatrix rxPrecodingMatrix) {
        this.rxPrecodingMatrix.set(rxPrecodingMatrix);
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

    /**
     * Calculate rx precoding matrix for
     */
    public void calcRxPreMatrixAndRate() {
        ComplexMatrix C = new DenseComplexMatrix(getNumAntennas(), getNumAntennas());
        C.zero();

        for (Cluster cluster : network.getClusters()) {
            ComplexMatrix Hikl = cluster.getMIMOChannel(this);
            for (UE ue : cluster.getUEs()) {
                ComplexMatrix Vjl = cluster.getTxPrecodingMatrix(ue);
                ComplexMatrix HV = Hikl.mult(Vjl, new DenseComplexMatrix(Hikl.numRows(), Vjl.numColumns()));
                ComplexMatrix HVVH = HV.mult(
                        HV.hermitianTranspose(new DenseComplexMatrix(HV.numColumns(), HV.numRows())),
                        new DenseComplexMatrix(HV.numRows(), HV.numRows()));
                C.add(HVVH);
            }
        }

        ComplexMatrix A = new DenseComplexMatrix(C.numRows(), C.numColumns());
        A.set(C);
        ComplexMatrix HikVik = cluster.getMIMOChannel(this).mult(cluster.getTxPrecodingMatrix(this));
        ComplexMatrix HikVikT = HikVik.hermitianTranspose(new DenseComplexMatrix(HikVik.numColumns(), HikVik.numRows()));
        ComplexMatrix HVVHik = HikVik.mult(HikVikT);
        A.add(new double[]{-1, 0}, HVVHik);
        setRate(0.5 * Math.log(HVVHik.mult(A.inverse()).add(ComplexMatrices.eye(getNumAntennas())).det2()));

        C.add(ComplexMatrices.eye(getNumAntennas()));
        C = C.inverse();
        ComplexMatrix Hkik = cluster.getMIMOChannel(this);
        ComplexMatrix C_1H = C.mult(Hkik, new DenseComplexMatrix(C.numRows(), Hkik.numColumns()));
        ComplexMatrix Vik = cluster.getTxPrecodingMatrix(this);
        ComplexMatrix Uik = C_1H.mult(Vik, new DenseComplexMatrix(C_1H.numRows(), Vik.numColumns()));
        rxPrecodingMatrix.set(Uik);
    }

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    /**
     * @return the mmseWeight
     */
    public ComplexMatrix getMMSEWeight() {
        return mmseWeight;
    }

    /**
     * @param mmseWeight the mmseWeight to set
     */
    public void setMMSEWeight(ComplexMatrix mmseWeight) {
        this.mmseWeight.set(mmseWeight);
    }

    /**
     * @return the rate
     */
    public double getRate() {
        return rate;
    }

    /**
     * @param rate the rate to set
     */
    public void setRate(double rate) {
        this.rate = rate;
    }
}
