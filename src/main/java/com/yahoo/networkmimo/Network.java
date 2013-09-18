package com.yahoo.networkmimo;

import java.util.Set;

import org.apache.commons.collections.map.MultiKeyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.yahoo.algebra.matrix.ComplexMatrix;
import com.yahoo.algebra.matrix.ComplexVector;
import com.yahoo.algebra.matrix.DenseComplexMatrix;
import com.yahoo.algebra.matrix.DenseComplexVector;
import com.yahoo.algebra.matrix.ComplexVector.Norm;

public class Network {
    private final static Logger logger = LoggerFactory.getLogger(Network.class);

    private final Set<Cluster> clusters = Sets.newHashSet();

    private final Set<BaseStation> bss = Sets.newHashSet();

    private final Set<UE> ues = Sets.newHashSet();

    private final MultiKeyMap MMatrixMap = new MultiKeyMap();

    private double closureDistance;

    public Network(double dist) {
        closureDistance = dist;
    }

    public Network addCluster(Cluster cluster) {
        cluster.setNetwork(this);
        clusters.add(cluster);
        for (BaseStation bs : cluster.getBSs()) {
            bs.setNetwork(this);
            bss.add(bs);
        }
        for (UE ue : cluster.getUEs()) {
            ue.setNetwork(this);
            ues.add(ue);
        }
        logger.debug("Add " + cluster + " to " + this);
        return this;
    }

    public Network addBaseStation(BaseStation bs) {
        bss.add(bs);
        bs.setNetwork(this);
        if (bs.getCluster() == null) {
            double dist = 1e16;
            Cluster cluster = null;
            for (Cluster c : clusters) {
                double tmp = Utils.getEntityDistance(bs, c);
                if (tmp < dist) {
                    dist = tmp;
                    cluster = c;
                }
            }
            bs.setCluster(cluster);
            cluster.addBaseStation(bs);
        }
        return this;
    }

    public Network addUE(UE ue) {
        ues.add(ue);
        ue.setNetwork(this);
        if (ue.getCluster() == null) {
            double dist = 1e16;
            Cluster cluster = null;
            for (Cluster c : clusters) {
                double tmp = Utils.getEntityDistance(ue, c);
                if (tmp < dist) {
                    dist = tmp;
                    cluster = c;
                }
            }
            ue.setCluster(cluster);
            cluster.addUE(ue);
        }
        return this;
    }

    public void recluster() {
        // TODO reassign base station and UE to clusters
    }

    public Set<Cluster> getClusters() {
        return clusters;
    }

    public Set<BaseStation> getBSs() {
        return bss;
    }

    public Set<UE> getUEs() {
        return ues;
    }

    public void updateMMatrixMap() {
        for (BaseStation q : bss) {
            for (BaseStation p : bss) {
                ComplexMatrix Mqp = new DenseComplexMatrix(q.getNumAntennas(), p.getNumAntennas());
                Mqp.zero();
                for (UE ue : getUEs()) {
                    double w = ue.getMMSEWeight();
                    ComplexMatrix Hjmq = q.getMIMOChannel(ue);
                    ComplexVector ujm = ue.getRxPreVector();
                    ComplexMatrix Hjmp_ = p.getMIMOChannel(ue);
                    ComplexMatrix HuuH = Hjmq
                            .hermitianTranspose(
                                    new DenseComplexMatrix(Hjmq.numColumns(), Hjmq.numRows()))
                            .mult(ujm, new DenseComplexVector(Hjmq.numColumns()))
                            .mult(ujm.conjugate(new DenseComplexVector(ujm.size())),
                                    new DenseComplexMatrix(Hjmq.numColumns(), ujm.size()))
                            .mult(Hjmp_);
                    Mqp.add(HuuH.scale(new double[] { w, 0 }));
                }
                MMatrixMap.put(q, p, Mqp);
            }
        }
    }

    public ComplexMatrix getMMatrix(BaseStation q, BaseStation p) {
        return (ComplexMatrix) MMatrixMap.get(q, p);
    }

    public double getClosureDistance() {
        return closureDistance;
    }

    public void iterate() {
        updateMMatrixMap();
        for (UE ue : ues) {
            ue.optimize();
        }
        for (UE ue : ues) {
            ue.updateVariables();
        }
        for (BaseStation q : bss) {
            logger.debug("Optimization in " + q);
            q.optimizePowerAllocation();
            logger.debug("Multipliers of " + q + ": " + q.getSubgradients());
            logger.debug("Real power allocation of " + q + ": " + q.getRealPowerAllocations());
            logger.debug("New power allocation of " + q + ": " + q.getPowerAllocations());
        }
        BaseStation.iteration++;
        logger.debug("Sum rate is " + getSumRate());
    }

    private void generateFeasibleInitialVariables() {
        for (BaseStation q : bss) {
            Cluster l = q.getCluster();
            int numUEs = l.getUEs().size();
            double powerBudget = q.getPowerBudget();
            q.clearPowerAllocation();
            for (UE i : l.getUEs()) {
                q.setPowerAllocation(i, powerBudget / numUEs);
            }
            logger.debug("Initial average power allocation within the located cluster from " + q
                    + ": " + powerBudget / numUEs);
            q.generateRandomTxPreVector();
        }
    }

    public void refresh() {
        logger.debug("Network refresh");
        for (BaseStation q : bss) {
            for (UE i : ues) {
                q.genenerateMIMOChannel(i);
            }
        }
        generateFeasibleInitialVariables();
        for (UE ue : ues) {
            ue.updateVariables();
        }
        logger.debug("Sum rate is " + getSumRate());
    }

    public double getSumRate() {
        double sumRate = 0.0;
        for (UE ue : ues) {
            sumRate += ue.getRate();
        }
        return sumRate;
    }

    public double objectiveValue() {
        double ret = getSumRate();
        for (UE ue : ues) {
            for (Cluster l : ue.getCluster().getClusterClosure()) {
                for (BaseStation q : l.getBSs()) {
                    ret -= ue.getLambda() * q.getTxPreVector(ue).norm(Norm.Two);
                }
            }
        }
        return ret;
    }

    public void optimize() {
        double prev = 0.0;
        double objectiveValue = objectiveValue();
        final int maxCount = 20;
        int count = 0;
        do {
            if (count++ > maxCount)
                break;
            prev = objectiveValue();
            iterate();
            objectiveValue = objectiveValue();
            logger.debug("Objective value is " + objectiveValue);
        } while (Math.abs(prev - objectiveValue) > 1e-1);
        logger.info("Optimized sum rate is " + getSumRate());
    }

    @Override
    public String toString() {
        return String.format("NETWORK");
    }
}
