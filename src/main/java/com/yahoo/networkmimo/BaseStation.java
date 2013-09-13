package com.yahoo.networkmimo;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.linear.LinearConstraint;
import org.apache.commons.math.optimization.linear.LinearObjectiveFunction;
import org.apache.commons.math.optimization.linear.Relationship;
import org.apache.commons.math.optimization.linear.SimplexSolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
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
        for (Cluster l : cluster.getClosureCluster()) {
            for (UE ue : l.getUEs()) {
                if (ue.isToBeServedBy(this))
                    num++;
            }
        }
        return num;
    }

    public Set<UE> getUEsToBeServed() {
        Set<UE> servedUEs = Sets.newHashSet();
        for (Cluster l : cluster.getClosureCluster()) {
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
                    + alloc.getKey() + "\n" + v);
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
        return String.format("BaseStation@(%.4f,%.4f)", getXY()[0], getXY()[1]);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * Optimize the power allocation
     */
    public void optimizePowerAllocation() {
        UE[] ueList = (UE[]) subgradient.keySet().toArray();
        double[] coefs = new double[ueList.length];
        double constantTerm = 0.0;
        for (int i = 0; i < ueList.length; i++) {
            coefs[i] = subgradient.get(ueList[i]);
            constantTerm = constantTerm - coefs[i] * powerAllocation.get(ueList[i]);
        }

        LinearObjectiveFunction f = new LinearObjectiveFunction(coefs, constantTerm);
        double[] powerSumCoefs = new double[ueList.length];
        for (int i = 0; i < powerSumCoefs.length; i++)
            powerSumCoefs[i] = 1.0;
        List<LinearConstraint> constraints = Lists.newArrayList();
        constraints.add(new LinearConstraint(powerSumCoefs, Relationship.LEQ, powerBudget));
        SimplexSolver simplex = new SimplexSolver();
        try {
            RealPointValuePair pair = simplex.optimize(f, constraints, GoalType.MINIMIZE, true);
            if (pair.getValue() > 0) {
                logger.error("Not a feasible direction");
            }
            double[] newAllocation = pair.getPointRef();
            for (int i = 0; i < ueList.length; i++) {
                powerAllocation.put(ueList[i], newAllocation[i]);
            }
        } catch (OptimizationException e) {
            logger.error("Power allocation failed at " + this);
        }
    }
}
