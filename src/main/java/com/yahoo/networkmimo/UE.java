package com.yahoo.networkmimo;

import com.yahoo.algebra.matrix.ComplexMatrices;
import com.yahoo.algebra.matrix.ComplexMatrix;
import com.yahoo.algebra.matrix.DenseComplexMatrix;

public class UE extends Entity {
    /**
     * Receiving precoding matrix
     */
    private final ComplexMatrix rxPreMatrix;

    private int numStreams;

    private Cluster cluster;

    private Network network;

    private ComplexMatrix dMatrix;

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

    /**
     * 
     * @param x
     *            x-axis
     * @param y
     *            y-axis
     * @param numAntennas
     *            number of antennas
     * @param numStreams
     *            number of data streams
     */
    public UE(double x, double y, int numAntennas, int numStreams) {
        super(x, y, Entity.Type.UE, numAntennas);
        this.numStreams = numStreams;
        rxPreMatrix = new DenseComplexMatrix(numAntennas, numStreams);
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
    public ComplexMatrix getRxPreMatrix() {
        return rxPreMatrix;
    }

    /**
     * @param rxPrecodingMatrix
     *            the rxPrecodingMatrix to set
     */
    public void setRxPrecodingMatrix(ComplexMatrix rxPrecodingMatrix) {
        this.rxPreMatrix.set(rxPrecodingMatrix);
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
                ComplexMatrix Vjl = cluster.getTxPreMatrix(ue);
                ComplexMatrix HV = Hikl.mult(Vjl,
                        new DenseComplexMatrix(Hikl.numRows(), Vjl.numColumns()));
                ComplexMatrix HVVH = HV.mult(HV.hermitianTranspose(new DenseComplexMatrix(HV
                        .numColumns(), HV.numRows())),
                        new DenseComplexMatrix(HV.numRows(), HV.numRows()));
                C.add(HVVH);
            }
        }

        ComplexMatrix A = new DenseComplexMatrix(C.numRows(), C.numColumns());
        A.set(C);
        ComplexMatrix HikVik = cluster.getMIMOChannel(this).mult(cluster.getTxPreMatrix(this));
        ComplexMatrix HikVikT = HikVik.hermitianTranspose(new DenseComplexMatrix(HikVik
                .numColumns(), HikVik.numRows()));
        ComplexMatrix HVVHik = HikVik.mult(HikVikT);
        A.add(new double[] { -1, 0 }, HVVHik);

        setRate(0.5
                * Math.log(HVVHik.mult(A.inverse()).add(ComplexMatrices.eye(getNumAntennas()))
                        .det2()) / Math.log(2.0));

        C.add(ComplexMatrices.eye(getNumAntennas()));
        C = C.inverse();
        ComplexMatrix Hkik = cluster.getMIMOChannel(this);
        ComplexMatrix C_1H = C.mult(Hkik, new DenseComplexMatrix(C.numRows(), Hkik.numColumns()));
        ComplexMatrix Vik = cluster.getTxPreMatrix(this);
        ComplexMatrix Uik = C_1H
                .mult(Vik, new DenseComplexMatrix(C_1H.numRows(), Vik.numColumns()));
        rxPreMatrix.set(Uik);
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

    public ComplexMatrix updateMMSEWeigh() {
        ComplexMatrix U_ = rxPreMatrix.hermitianTranspose();
        ComplexMatrix uHv = U_.mult(cluster.getMIMOChannel(this))
                .mult(cluster.getTxPreMatrix(this));
        mmseWeight.set(ComplexMatrices.eye(numStreams).add(new double[] { -1, 0 }, uHv).inverse()
                .scale(new double[] { 1.0 / rate, 0 }));
        return mmseWeight;
    }

    /**
     * @param mmseWeight
     *            the mmseWeight to set
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
     * @param rate
     *            the rate to set
     */
    public void setRate(double rate) {
        this.rate = rate;
    }

    public ComplexMatrix calcDMatrix() {
        dMatrix.set(cluster.getMIMOChannel(this).hermitianTranspose().mult(rxPreMatrix)
                .scale(mmseWeight.get(0, 0)));
        return dMatrix;
    }

    public void alloc() {
        dMatrix = new DenseComplexMatrix(cluster.getNumAntennas(), numStreams);
    }
}
