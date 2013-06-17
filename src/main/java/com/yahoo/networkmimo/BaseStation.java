package com.yahoo.networkmimo;

import java.util.List;
import java.util.Map;

import com.beust.jcommander.internal.Maps;
import com.yahoo.algebra.matrix.ComplexMatrices;
import com.yahoo.algebra.matrix.ComplexMatrix;
import com.yahoo.algebra.matrix.DenseComplexMatrix;
import com.yahoo.networkmimo.exception.NetworkNotReadyException;

public class BaseStation extends Entity {
    private Map<UE, ComplexMatrix> txPrecodingMatrix = Maps.newHashMap();

    private Cluster cluster;

    private Network network;

    private double powerBudget;

    /**
     * penalty parameter for sparsity
     */
    private double lambda;

    public BaseStation(double x, double y, int numAntennas) {
        super(x, y, Entity.Type.BS, numAntennas);
    }

    public BaseStation(double x, double y, int numAntennas, double powerBudget, double lambda) {
        super(x, y, Entity.Type.BS, numAntennas);
        setPowerBudget(powerBudget);
        setLambda(lambda);
    }

    public void setTxPrecodingMatrx(UE ue, DenseComplexMatrix v) {
        txPrecodingMatrix.put(ue, v);
    }

    public ComplexMatrix getTxPrecodingMatrix(UE ue) {
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

    /**
     * @return the powerBudget
     */
    public double getPowerBudget() {
        return powerBudget;
    }

    /**
     * @param powerBudget
     *            the powerBudget to set
     */
    public void setPowerBudget(double powerBudget) {
        this.powerBudget = powerBudget;
    }

    /**
     * @return the lambda
     */
    public double getLambda() {
        return lambda;
    }

    /**
     * @param lambda
     *            the lambda to set
     */
    public void setLambda(double lambda) {
        this.lambda = lambda;
    }

    public void genRandomTxPrecodingMatrix() {
        Cluster cluster = getCluster();
        if (cluster == null) {
            throw new NetworkNotReadyException("This basestation is not added to any cluster");
        }
        List<UE> ues = cluster.getUEs();
        if (ues.isEmpty()) {
            throw new NetworkNotReadyException("No UE is added to the cluster where this BS belong");
        }
        double powerPerUE = powerBudget / ues.size();
        for (UE ue : ues) {
            ComplexMatrix v = ComplexMatrices.random(new DenseComplexMatrix(getNumAntennas(), ue
                    .getNumStreams()));
            txPrecodingMatrix.put(ue, ComplexMatrices.setPower(v, powerPerUE));
        }
    }

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }
}
