package com.yahoo.networkmimo;

import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.MultiKeyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.yahoo.algebra.matrix.ComplexMatrix;
import com.yahoo.algebra.matrix.ComplexVector;
import com.yahoo.algebra.matrix.ComplexVector.Norm;
import com.yahoo.algebra.matrix.DenseComplexMatrix;
import com.yahoo.algebra.matrix.DenseComplexVector;

public class Network {
    private final static Logger logger = LoggerFactory.getLogger(Network.class);

    private final Set<Cluster> clusters = Sets.newHashSet();

    private final Set<BaseStation> bss = Sets.newHashSet();

    private final Set<UE> ues = Sets.newHashSet();

    private final MultiKeyMap MMatrixMap = new MultiKeyMap();

    private final Map<Cluster, ComplexMatrix> mmseMMatrixMap = Maps.newHashMap();

    private double closureDistance;

    private double sumRate;

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

    public void updateMmseMMatrixMap() {
        for (Cluster cluster : clusters) {
            ComplexMatrix M = new DenseComplexMatrix(cluster.getNumAntennas(),
                    cluster.getNumAntennas());
            M.zero();
            for (Cluster l : clusters) {
                for (UE i : l.getUEs()) {
                    ComplexMatrix H = l.getMIMOChannel(i);
                    ComplexVector Hu = H.hermitianTranspose(
                            new DenseComplexMatrix(H.numColumns(), H.numRows())).mult(
                            i.getRxPreVector(), new DenseComplexVector(H.numColumns()));
                    ComplexMatrix HuuH = Hu.mult(Hu.conjugate(new DenseComplexVector(Hu.size())),
                            new DenseComplexMatrix(Hu.size(), Hu.size()));
                    HuuH.scale(new double[] { i.getMMSEWeight(), 0 });
                    M.add(HuuH);
                }
            }
            mmseMMatrixMap.put(cluster, M);
        }
    }

    public ComplexMatrix getMmseMMatrix(Cluster cluster) {
        return mmseMMatrixMap.get(cluster);
    }

    public ComplexMatrix getMMatrix(BaseStation q, BaseStation p) {
        return (ComplexMatrix) MMatrixMap.get(q, p);
    }

    public double getClosureDistance() {
        return closureDistance;
    }

    public void iterateWithinUEs() {
        updateMMatrixMap();
        for (UE ue : ues) {
            ue.optimize();
        }
        for (BaseStation q : bss) {
            logger.debug("Optimization in " + q);
            logger.debug("Multipliers of " + q + ": " + q.getSubgradients());
            q.optimizePowerAllocation();
            logger.debug("Real power allocation of " + q + ": " + q.getRealPowerAllocations());
            logger.debug("New power allocation of " + q + ": " + q.getPowerAllocations());
        }
        for (UE ue : ues) {
            ue.updateVariables();
        }
        BaseStation.iteration++;
        logger.debug("Sum rate is " + getReadySumRate());
    }

    private void generateFeasibleInitialVariables() {
        for (BaseStation q : bss) {
            Cluster l = q.getCluster();
            int numUEs = l.getUEs().size() * q.getCluster().getClusterClosure().size();
            double powerBudget = q.getPowerBudget();
            q.clearPowerAllocation();
            for (Cluster c : q.getCluster().getClusterClosure()) {
                for (UE i : c.getUEs()) {
                    if (c == q.getCluster()) {
                        q.setPowerAllocation(i, powerBudget / numUEs);
                    }
                }
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
            for (Cluster c : q.getCluster().getClusterClosure()) {
                for (UE i : c.getUEs()) {
                    q.setSubgradient(i, 0.0);
                    q.setHessianDiagonal(i, 1.0);
                }
            }
        }
        for (Cluster cluster : clusters) {
            cluster.assembleMIMOChannel();
        }
        for (UE ue : ues) {
            ue.updateLambdaMap();
        }
        generateFeasibleInitialVariables();
        for (UE ue : ues) {
            ue.updateVariables();
        }
        updateMMatrixMap();
        BaseStation.iteration = 1;
        logger.debug("Sum rate is " + getReadySumRate());
    }

    public void refreshInitial() {
        for (BaseStation q : bss) {
            for (Cluster c : q.getCluster().getClusterClosure()) {
                for (UE i : c.getUEs()) {
                    q.setSubgradient(i, 0.0);
                    q.setHessianDiagonal(i, 1.0);
                }
            }
        }
        for (UE ue : ues) {
            ue.updateLambdaMap();
        }
        generateFeasibleInitialVariables();
        for (UE ue : ues) {
            ue.updateVariables();
        }
        updateMMatrixMap();
        BaseStation.iteration = 1;
        logger.debug("Sum rate is " + getReadySumRate());
    }

    public double getSumRate() {
        return sumRate;
    }

    public double updateSumRate() {
        sumRate = 0.0;
        for (UE ue : ues) {
            sumRate += ue.getRate();
        }
        return sumRate;
    }

    public double getReadySumRate() {
        double sumRate = 0.0;
        for (UE ue : ues) {
            sumRate += ue.getRate();
        }
        return sumRate;
    }

    public double objectiveValue() {
        double ret = getReadySumRate();
        for (UE ue : ues) {
            for (Cluster l : ue.getCluster().getClusterClosure()) {
                for (BaseStation q : l.getBSs()) {
                    ret -= ue.getLambda(q) * q.getTxPreVector(ue).norm(Norm.Two);
                }
            }
        }
        return ret;
    }

    public void optimizeFromUEs() {
        double prev = 0.0;
        double objectiveValue = objectiveValue();
        do {
            prev = objectiveValue();
            iterateWithinUEs();
            objectiveValue = objectiveValue();
            logger.debug("Objective value is " + objectiveValue);
        } while (Math.abs(prev - objectiveValue) > 1e-1);
        logger.info("Optimized sum rate is " + getReadySumRate());
    }

    public void optimizeFromBSs() {
        double prev = 0.0;
        double objectiveValue = objectiveValue();
        do {
            prev = objectiveValue;
            iterateWithinBSs();
            objectiveValue = objectiveValue();
            logger.debug("Objective value is " + objectiveValue);
        } while (Math.abs(prev - objectiveValue) > 1e-1);
        logger.info("Optimized sum rate is " + getReadySumRate());
    }

    private void iterateWithinBSs() {
        updateMMatrixMap();
        for (BaseStation bs : bss) {
            bs.optimize();
        }
        for (BaseStation q : bss) {
            logger.debug("Power reallocation in " + q);
            logger.debug("Multipliers of " + q + ": " + q.getSubgradients());
            q.optimizePowerAllocation();
            logger.debug("Real power allocation of " + q + ": " + q.getRealPowerAllocations());
            logger.debug("New power allocation of " + q + ": " + q.getPowerAllocations());
        }
        for (UE ue : ues) {
            ue.updateVariables();
        }
        BaseStation.iteration++;
        logger.debug("Sum rate is " + getReadySumRate());
    }

    public void optimizeWMMSE() {
        double prev = 0.0;
        double objectiveValue = objectiveValueWMMSE();
        do {
            prev = objectiveValue;
            iterateWMMSE();
            objectiveValue = objectiveValueWMMSE();
            logger.debug("Objective value is " + objectiveValue);
        } while (Math.abs(prev - objectiveValue) > 1e-1);
        logger.info("Optimized sum rate is " + getReadySumRate());
    }

    private double objectiveValueWMMSE() {
        return getReadySumRate();
    }

    private void iterateWMMSE() {
        updateMmseMMatrixMap();
        for (Cluster cluster : clusters) {
            double multiplier = cluster.searchMultiplier();
            cluster.iterateWMMSE(multiplier);
        }

        for (UE ue : ues) {
            ue.updateVariables();
        }
        BaseStation.iteration++;
        logger.debug("Sum rate is " + getReadySumRate());
    }

    public void optimizeAmongBSsUEs() {
        double prev = 0.0;
        double objectiveValue = objectiveValue();
        do {
            prev = objectiveValue;
            iterateAmongBSsUEs();
            objectiveValue = objectiveValue();
            logger.debug("Objective value is " + objectiveValue);
        } while (Math.abs(prev - objectiveValue) > 1e-1);
        logger.info("Optimized sum rate is " + getReadySumRate());
    }

    public void optimizePrimalProblem() {
        double prev = 0.0;
        double objectiveValue = objectiveValue();
        logger.debug("Objective value is " + objectiveValue);
        while (Math.abs(prev - objectiveValue) > 1e-2) {
            for (BaseStation q : bss) {
                logger.debug("Power allocation before optimization of " + q + ": "
                        + q.getPowerAllocations());
                logger.debug("Subgradients of " + q + ": " + q.getSubgradients());
                q.optimizePowerAllocation();
                logger.debug("Power allocation after optimization of " + q + ": "
                        + q.getPowerAllocations());
                // q.updateTxPreVectors();
            }
            prev = objectiveValue;
            iteratePrimalProblem();
            BaseStation.iteration++;
            objectiveValue = objectiveValue();
            if (objectiveValue <= prev) {
                BaseStation.iteration--;
                break;
            } else {
                updateSumRate();
                System.out.println("Objective value is " + objectiveValue);
            }
        }
        System.out.println("Sum rate: " + getSumRate());
        logger.info("Sum rate: " + getSumRate());
        System.out.println("Number of iterations: " + (BaseStation.iteration - 1));
    }

    /**
     * The stopping criteria is that capacity increment is less than 0.1
     */
    private void iteratePrimalProblem() {
        optimizeSubproblem();
    }

    /**
     * The stopping criteria is that all subproblems satisfies the first-order
     * optimality
     */
    @SuppressWarnings("unused")
    private void iteratePrimalProblemTest() {
        boolean subproblemsConverged = true;
        final int maxCount = 20;
        int count = 0;
        do {
            if (count++ > maxCount)
                break;
            subproblemsConverged = true;
            iterateSubproblem();
            updateMMatrixMap();
            for (UE ue : ues) {
                for (Cluster l : ue.getCluster().getClusterClosure()) {
                    for (BaseStation q : l.getBSs()) {
                        subproblemsConverged &= ue.checkSubproblemConverged(q);
                    }
                }
            }
        } while (!subproblemsConverged);
    }

    private void optimizeSubproblem() {
        boolean subproblemsConverged = true;
        final int maxCount = 20;
        int count = 0;
        do {
            if (count++ > maxCount)
                break;
            subproblemsConverged = true;
            iterateSubproblem();
            for (UE ue : ues) {
                for (Cluster l : ue.getCluster().getClusterClosure()) {
                    for (BaseStation q : l.getBSs()) {
                        subproblemsConverged &= ue.checkSubproblemConverged(q);
                    }
                }
            }
        } while (!subproblemsConverged);
    }

    private void iterateSubproblem() {
        for (UE ue : ues) {
            for (Cluster l : ue.getCluster().getClusterClosure()) {
                for (BaseStation q : l.getBSs()) {
                    q.setSubgradient(ue, -ue.blockCoordinateDescent(q));
                }
            }
        }
        for (UE ue : ues) {
            ue.updateVariables();
        }
        updateMMatrixMap();
        for (UE ue : ues) {
            ue.updateCVectorMap();
        }
    }

    private void iterateAmongBSsUEs() {
        updateMMatrixMap();
        // initial iteration
        for (UE ue : ues) {
            for (Cluster l : ue.getCluster().getClusterClosure()) {
                for (BaseStation q : l.getBSs()) {
                    q.setSubgradient(ue, -ue.blockCoordinateDescent(q));
                }
            }
        }
        // descent iterations
        boolean descentDone = true;
        do {
            descentDone = true;
            for (UE ue : ues) {
                for (Cluster l : ue.getCluster().getClusterClosure()) {
                    for (BaseStation q : l.getBSs()) {
                        descentDone &= ue.blockCoordinateDescentWrapper(q);
                    }
                }
            }
        } while (!descentDone);

        for (BaseStation q : bss) {
            logger.debug("Power reallocation in " + q);
            logger.debug("Multipliers of " + q + ": " + q.getSubgradients());
            q.optimizePowerAllocation();
            q.updateTxPreVectors();
            logger.debug("Real power allocation of " + q + ": " + q.getRealPowerAllocations());
            logger.debug("New power allocation of " + q + ": " + q.getPowerAllocations());
        }
        for (UE ue : ues) {
            ue.updateVariables();
        }
        BaseStation.iteration++;
        logger.debug("Sum rate is " + getReadySumRate());
    }

    @Override
    public String toString() {
        return String.format("NETWORK");
    }

    public void brownianMotion(double r) {
        for (Cluster cluster : clusters) {
            for (BaseStation bs : cluster.getBSs()) {
                boolean valid = false;
                double x;
                double y;
                do {
                    valid = true;
                    x = (Cluster.rng.nextValue() - 0.5) * 2 * r;
                    y = (Cluster.rng.nextValue() - 0.5) * 2 * r;
                    double mirrorX = Math.abs(x);
                    double mirrorY = Math.abs(y);
                    if ((mirrorY > r * Math.sin(Math.PI / 3))) {
                        valid = false;
                    } else if ((Math.sqrt(3) * mirrorX + mirrorY) > (r * Math.sqrt(3))) {
                        valid = false;
                    }
                } while (!valid);
                bs.setXY(cluster.getXY()[0] + x, cluster.getXY()[1] + y);
            }
            for (UE ue : cluster.getUEs()) {
                boolean valid = false;
                double x;
                double y;
                do {
                    valid = true;
                    x = (Cluster.rng.nextValue() - 0.5) * 2 * r;
                    y = (Cluster.rng.nextValue() - 0.5) * 2 * r;
                    double mirrorX = Math.abs(x);
                    double mirrorY = Math.abs(y);
                    if ((mirrorY > r * Math.sin(Math.PI / 3))) {
                        valid = false;
                    } else if ((Math.sqrt(3) * mirrorX + mirrorY) > (r * Math.sqrt(3))) {
                        valid = false;
                    }
                } while (!valid);
                ue.setXY(cluster.getXY()[0] + x, cluster.getXY()[1] + y);
            }
        }
    }
}
