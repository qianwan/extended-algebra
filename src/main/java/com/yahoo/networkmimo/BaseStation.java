package com.yahoo.networkmimo;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.yahoo.algebra.matrix.ComplexVector;
import com.yahoo.algebra.matrix.ComplexVectors;
import com.yahoo.algebra.matrix.DenseComplexVector;

public class BaseStation extends Entity {
    private static final Logger logger = LoggerFactory.getLogger(BaseStation.class);

    private Map<UE, ComplexVector> txPreVector = Maps.newHashMap();

    private Cluster cluster;

    private Network network;

    private double powerBudget;

    private Map<UE, Double> powerAllocation = Maps.newHashMap();

    private Map<UE, Double> subgradient = Maps.newHashMap();

    private String name = null;

    public static int iteration;

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
        txPreVector.put(ue, v);
    }

    public ComplexVector getTxPreVector(UE ue) {
        ComplexVector v = txPreVector.get(ue);
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
        subgradient.put(ue, v);
    }

    public Map<UE, Double> getSubgradients() {
        return subgradient;
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
            txPreVector.put(alloc.getKey(), ComplexVectors.setPower(v, alloc.getValue()));
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
        return String.format("BaseStation#%s@(%.4f,%.4f)", name, getXY()[0], getXY()[1]);
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
        UE[] ueList = new UE[subgradient.size()];
        int index = 0;
        for (UE ue : subgradient.keySet()) {
            ueList[index++] = ue;
        }
        double[] direction = new double[ueList.length];
        for (int i = 0; i < direction.length; i++) {
            direction[i] = powerAllocation.get(ueList[i]) - subgradient.get(ueList[i]);
        }
        double relaxL = 0.0;
        double relaxH = 100;
        double totalPower = 0.0;
        double[] projection = new double[ueList.length];
        do {
            double relax = (relaxL + relaxH) / 2;
            totalPower = 0.0;
            for (int i = 0; i < projection.length; i++) {
                projection[i] = (direction[i] <= relax) ? 0 : direction[i] - relax;
                totalPower += projection[i];
            }
            if (totalPower > powerBudget)
                relaxL = relax;
            else
                relaxH = relax;
        } while (Math.abs(totalPower - powerBudget) > 1e-2);

        for (int i = 0; i < ueList.length; i++) {
            double orig = powerAllocation.get(ueList[i]);
            powerAllocation.put(ueList[i], orig + 1 * (projection[i] - orig));
        }
    }

    public Map<UE, Double> getPowerAllocations() {
        return this.powerAllocation;
    }

    public double getRealPowerAllocation(UE ue) {
        return ComplexVectors.getPower(txPreVector.get(ue));
    }

    public Map<UE, Double> getRealPowerAllocations() {
        Map<UE, Double> realPowerAllocations = Maps.newHashMap();
        for (Map.Entry<UE, ComplexVector> entry : txPreVector.entrySet()) {
            realPowerAllocations.put(entry.getKey(), ComplexVectors.getPower(entry.getValue()));
        }
        return realPowerAllocations;
    }

    public double getSubgradient(UE ue) {
        return subgradient.get(ue);
    }
}
