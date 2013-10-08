package com.yahoo.networkmimo;

import static com.yahoo.networkmimo.UE.bisectionTarget;
import static java.lang.Math.sqrt;
import static java.lang.Math.pow;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.yahoo.algebra.matrix.ComplexMatrices;
import com.yahoo.algebra.matrix.ComplexMatrix;
import com.yahoo.algebra.matrix.ComplexVector;
import com.yahoo.algebra.matrix.ComplexVector.Norm;
import com.yahoo.algebra.matrix.ComplexVectors;
import com.yahoo.algebra.matrix.DenseComplexVector;

public class BaseStation extends Entity {
    private static final Logger logger = LoggerFactory.getLogger(BaseStation.class);

    private Map<UE, ComplexVector> txPreVectors = Maps.newHashMap();

    private Cluster cluster;

    private Network network;

    private double powerBudget;

    private Map<UE, Double> powerAllocation = Maps.newHashMap();

    private Map<UE, Double> subgradients = Maps.newHashMap();

    private Map<UE, Double> hessianDiagonals = Maps.newHashMap();

    private String name = null;

    private Map<UE, ComplexVector> quasiTxVectors = Maps.newHashMap();

    public static int iteration = 1;

    public BaseStation(double x, double y, int numAntennas) {
        super(x, y, Entity.Type.BS, numAntennas);
    }

    /**
     * 
     * @param x
     *            x-axis
     * @param y
     *            y-axis
     * @param numAntennas
     *            number of antennas
     * @param powerBudget
     *            maximum transmit power
     * @param lambda
     *            penalty parameter for sparse beamforming
     */
    public BaseStation(double x, double y, int numAntennas, double powerBudget) {
        super(x, y, Entity.Type.BS, numAntennas);
        setPowerBudget(powerBudget);
    }

    public BaseStation(double x, double y, int numAntennas, double powerBudget, String name) {
        super(x, y, Entity.Type.BS, numAntennas);
        setPowerBudget(powerBudget);
        this.name = name;
    }

    public void setTxPreVector(UE ue, ComplexVector v) {
        txPreVectors.put(ue, v);
    }

    public ComplexVector getTxPreVector(UE ue) {
        ComplexVector v = txPreVectors.get(ue);
        if (v == null) {
            v = new DenseComplexVector(getNumAntennas());
            v.zero();
        }
        return v;
    }

    public int getNumberOfUEsToBeServed() {
        int num = 0;
        for (Cluster l : cluster.getClusterClosure()) {
            for (UE ue : l.getUEs()) {
                if (ue.isToBeServedBy(this))
                    num++;
            }
        }
        return num;
    }

    public Set<UE> getUEsToBeServed() {
        Set<UE> servedUEs = Sets.newHashSet();
        for (Cluster l : cluster.getClusterClosure()) {
            for (UE ue : l.getUEs()) {
                if (ue.isToBeServedBy(this))
                    servedUEs.add(ue);
            }
        }
        return servedUEs;
    }

    public double getPowerAllocation(UE ue) {
        // TODO what if the BS of UE-BS pair that needs optimization has no
        // power allocation for UE?
        if (!powerAllocation.containsKey(ue)) {
            powerAllocation.put(ue, 0.0);
            return 0.0;
        }
        return powerAllocation.get(ue);
    }

    public void setPowerAllocation(UE ue, double powerAlloc) {
        powerAllocation.put(ue, powerAlloc);
    }

    public void clearPowerAllocation() {
        powerAllocation.clear();
    }

    public void setSubgradient(UE ue, double v) {
        subgradients.put(ue, v);
    }

    public Map<UE, Double> getSubgradients() {
        return subgradients;
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

    public void generateRandomTxPreVector() {
        for (Map.Entry<UE, Double> alloc : powerAllocation.entrySet()) {
            ComplexVector v = ComplexVectors.random(new DenseComplexVector(getNumAntennas()));
            txPreVectors.put(alloc.getKey(), ComplexVectors.setPower(v, alloc.getValue()));
            logger.debug("Generate random tx precoding vector from " + this + " to "
                    + alloc.getKey() + ": " + v);
        }
    }

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    @Override
    public String toString() {
        return String.format("BS#%s@%s", name, cluster == null ? null : cluster.getName());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        else
            return false;
    }

    /**
     * Optimize the power allocation
     */
    public void optimizePowerAllocation() {
        UE[] ueList = new UE[subgradients.size()];
        int index = 0;
        for (UE ue : subgradients.keySet()) {
            ueList[index++] = ue;
        }
        double[] direction = new double[ueList.length];
        double origPower = 0.0;
        double dirPower = 0.0;
        double relaxH = 0.0;
        double gnorm = 0.0;
        for (int i = 0; i < ueList.length; i++) {
            gnorm += pow(subgradients.get(ueList[i]), 2);
        }
        gnorm = sqrt(gnorm);
        for (int i = 0; i < direction.length; i++) {
            double subgradient = subgradients.get(ueList[i]);
            double alloc = powerAllocation.get(ueList[i]);
            double hessian = hessianDiagonals.get(ueList[i]);
            hessian = 1 / alloc;
            if (gnorm == 0)
                gnorm = 1;
            direction[i] = alloc - subgradient / hessian / 23;
            if (direction[i] > relaxH)
                relaxH = direction[i];
            dirPower += direction[i];
            origPower += powerAllocation.get(ueList[i]);
        }
        logger.debug("orig power " + origPower + ", dir power " + dirPower);
        double relaxL = 0.0;
        double totalPower = 0.0;
        double relax = 0.0;
        double[] projection = new double[ueList.length];
        do {
            relax = (relaxL + relaxH) / 2;
            totalPower = 0.0;
            for (int i = 0; i < projection.length; i++) {
                projection[i] = (direction[i] - relax <= UE.epsilon) ? UE.epsilon : direction[i]
                        - relax;
                totalPower += projection[i];
            }
            if (totalPower > powerBudget)
                relaxL = relax;
            else if (totalPower < powerBudget)
                relaxH = relax;
            else
                break;
        } while (Math.abs(totalPower - powerBudget) / powerBudget > 1e-6);

        logger.debug("Power relax is " + relax);
        for (int i = 0; i < ueList.length; i++) {
            double orig = powerAllocation.get(ueList[i]);
            powerAllocation.put(ueList[i], orig + 1.0 / sqrt(1) * (projection[i] - orig));
        }
    }

    public void optimize() {
        logger.debug("Optimization within " + this);
        for (Cluster k : cluster.getClusterClosure()) {
            for (UE i : k.getUEs()) {
                i.updateCVectorMap();
                logger.debug("Optimization between " + this + " and " + i);
                subgradients.put(i, -blockCoordinateDescent(i));
            }
        }
    }

    public void updateTxPreVectors() {
        // for (Cluster l : cluster.getClusterClosure()) {
        // for (UE ue : l.getUEs()) {
        // if (getPowerAllocation(ue) > 0) {
        // if (getTxPreVector(ue).norm(Norm.Two) <= Double.MIN_VALUE) {
        // logger.warn("Allocate power to unserved UE " + ue + " from " + this);
        // } else {
        // ComplexVectors.setPower(getTxPreVector(ue), getPowerAllocation(ue));
        // }
        // }
        // }
        // }
        for (Map.Entry<UE, ComplexVector> entry : quasiTxVectors.entrySet()) {
            UE ue = entry.getKey();
            ComplexVector v = entry.getValue();
            if (v.norm(Norm.Two) > 0.0) {
                ComplexVectors.setPower(v, powerAllocation.get(ue));
                txPreVectors.put(ue, v);
            }
        }
        quasiTxVectors.clear();
    }

    public double blockCoordinateDescent(UE ue) {
        ComplexVector c = ue.getCVector(this);
        ComplexMatrix M = network.getMMatrix(this, this);
        double lambda = ue.getLambda(this);
        double multiplier = 0.0;
        if (c.norm(Norm.Two) <= ue.getLambda(this) / 2) {
            getTxPreVector(ue).zero();
            logger.debug("sparsity ensured between " + this + " and " + ue);
            multiplier = 0.0;
        } else {
            double theta = 1 / Math.sqrt(getPowerAllocation(ue));
            double targetValue = 0.0;
            if (bisectionTarget(0, theta, lambda, M, c) >= 1) {
                double miuLow = 0.0;
                double miuHigh = 1;
                do {
                    miuHigh *= 2;
                } while (bisectionTarget(miuHigh, theta, lambda, M, c) >= 1);
                logger.debug("Bisection target >= 1");
                do {
                    multiplier = (miuLow + miuHigh) / 2;
                    targetValue = bisectionTarget(multiplier, theta, lambda, M, c);
                    if (targetValue > 1)
                        miuLow = multiplier;
                    else
                        miuHigh = multiplier;
                } while (Math.abs(targetValue - 1) > 1e-6);
            } else {
                double tLow = theta;
                double tHigh = theta;
                do {
                    tHigh *= 2;
                } while (bisectionTarget(0, tHigh, lambda, M, c) < 1);
                logger.debug("Bisection target < 1");
                do {
                    theta = (tLow + tHigh) / 2;
                    targetValue = bisectionTarget(0, theta, lambda, M, c);
                    if (targetValue > 1)
                        tHigh = theta;
                    else
                        tLow = theta;
                } while (Math.abs(targetValue - 1) > 1e-6);
                multiplier = 0.0;
            }
            ComplexVector v = null;
            if (Double.isInfinite(theta)) {
                v = new DenseComplexVector(getNumAntennas());
                v.zero();
            } else
                v = ComplexMatrices.eye(getNumAntennas())
                        .scale(new double[] { lambda * theta / 2 + multiplier, 0 }).add(M)
                        .inverse().mult(c, new DenseComplexVector(getNumAntennas()));
            logger.debug("Use block coordinate descent, get multipler: " + multiplier + "; theta: "
                    + theta + "; tx vector: " + v);
            txPreVectors.put(ue, v);
        }
        return multiplier;
    }

    public Map<UE, Double> getPowerAllocations() {
        return this.powerAllocation;
    }

    public double getRealPowerAllocation(UE ue) {
        return ComplexVectors.getPower(txPreVectors.get(ue));
    }

    public Map<UE, Double> getRealPowerAllocations() {
        Map<UE, Double> realPowerAllocations = Maps.newHashMap();
        for (Map.Entry<UE, ComplexVector> entry : txPreVectors.entrySet()) {
            realPowerAllocations.put(entry.getKey(), ComplexVectors.getPower(entry.getValue()));
        }
        return realPowerAllocations;
    }

    public double getSubgradient(UE ue) {
        return subgradients.get(ue);
    }

    public void setQuasiTxVector(UE ue, ComplexVector v) {
        quasiTxVectors.put(ue, v);
    }

    public ComplexVector getQuasiTxVector(UE ue) {
        return quasiTxVectors.get(ue);
    }

    public void setHessianDiagonal(UE ue, double d) {
        hessianDiagonals.put(ue, d);
    }

    public double getHessianDiagonal(UE ue) {
        return hessianDiagonals.get(ue);
    }

    public Map<UE, ComplexVector> getTxPreVectors() {
        return txPreVectors;
    }
}
