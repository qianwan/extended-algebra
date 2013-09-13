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
        logger.info("Add " + cluster + " to " + this);
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
                Cluster l = q.getCluster();
                Cluster lPrim = p.getCluster();
                ComplexMatrix Mqp = new DenseComplexMatrix(q.getNumAntennas(), p.getNumAntennas());
                Mqp.zero();
                for (UE ue : getUEs()) {
                    double wjm = ue.getMMSEWeight();
                    ComplexMatrix Hjml = l.getMIMOChannel(ue);
                    ComplexVector ujm = ue.getRxPreVector();
                    ComplexMatrix Hjml_ = lPrim.getMIMOChannel(ue);
                    ComplexMatrix HuuH = Hjml
                            .hermitianTranspose(
                                    new DenseComplexMatrix(Hjml.numColumns(), Hjml.numRows()))
                            .mult(ujm, new DenseComplexVector(Hjml.numColumns()))
                            .mult(ujm.conjugate(new DenseComplexVector(ujm.size())),
                                    new DenseComplexMatrix(Hjml.numColumns(), ujm.size()))
                            .mult(Hjml_);
                    Mqp.add(HuuH.scale(new double[] { wjm, 0 }));
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
            ue.updateCVectorMap();
        }
        for (UE ue : ues) {
            for (Cluster l : ue.getCluster().getClosureCluster()) {
                for (BaseStation q : l.getBSs()) {
                    q.setSubgradient(ue, -ue.optimize(q));
                }
            }
        }
        for (UE ue : ues) {
            ue.updateVariables();
        }
        for (BaseStation q : bss) {
            q.optimizePowerAllocation();
        }
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
            logger.debug("Initial average power allocation from " + q + ": " + powerBudget / numUEs);
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
    }

    @Override
    public String toString() {
        return String.format("NETWORK");
    }
}
