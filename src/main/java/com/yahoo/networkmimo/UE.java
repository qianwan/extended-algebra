package com.yahoo.networkmimo;

import com.yahoo.algebra.matrix.ComplexMatrices;
import com.yahoo.algebra.matrix.ComplexMatrix;
import com.yahoo.algebra.matrix.DenseComplexMatrix;
import com.yahoo.networkmimo.exception.ComplexMatrixNotSPDException;

public class UE extends Entity {
    /**
     * Receiving precoding matrix
     */
    private final ComplexMatrix rxPrecodingMatrix;

    private int numStreams;

    private Cluster cluster;

    private Network network;

    // public UE(double x, double y, int numAntennas) {
    // super(x, y, Entity.Type.UE, numAntennas);
    // }

    public UE(double x, double y, int numAntennas, int numStreams) {
        super(x, y, Entity.Type.UE, numAntennas);
        this.numStreams = numStreams;
        rxPrecodingMatrix = new DenseComplexMatrix(numStreams, numAntennas);
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
    public void calcRxPreMatrix() {
        ComplexMatrix A = new DenseComplexMatrix(getNumAntennas(), getNumAntennas());
        A.zero();

        for (Cluster cluster : network.getClusters()) {
            ComplexMatrix Hikl = cluster.getMIMOChannel(this);
            for (UE ue : cluster.getUEs()) {
                ComplexMatrix Vjl = cluster.getTxPrecodingMatrix(ue);
                ComplexMatrix HV = Hikl.mult(Vjl, new DenseComplexMatrix(Hikl.numRows(), Vjl.numColumns()));
                ComplexMatrix HVVH = null;
                try {
                    HVVH = HV.mult(
                            HV.hermitianTranspose(new DenseComplexMatrix(HV.numColumns(), HV.numRows())),
                            new DenseComplexMatrix(HV.numRows(), HV.numRows()));
                } catch (ComplexMatrixNotSPDException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                A.add(HVVH);
            }
        }
        A.add(ComplexMatrices.eye(getNumAntennas()));
        A = A.inverse();
        ComplexMatrix Hkik = cluster.getMIMOChannel(this);
        ComplexMatrix C_1H = A.mult(Hkik, new DenseComplexMatrix(A.numRows(), Hkik.numColumns()));
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
}
