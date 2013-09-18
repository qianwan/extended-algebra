package com.yahoo.networkmimo;

import java.util.Map;
import java.util.Set;

import no.uib.cipr.matrix.NotConvergedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.yahoo.algebra.matrix.ComplexMatrices;
import com.yahoo.algebra.matrix.ComplexMatrix;
import com.yahoo.algebra.matrix.ComplexVector;
import com.yahoo.algebra.matrix.ComplexVector.Norm;
import com.yahoo.algebra.matrix.DenseComplexMatrix;
import com.yahoo.algebra.matrix.DenseComplexVector;

public class UE extends Entity {
    private static final Logger logger = LoggerFactory.getLogger(UE.class);
    /**
     * Receiving precoding matrix
     */
    private ComplexVector rxPreVector;

    private Cluster cluster;

    private Network network;

    private Map<BaseStation, ComplexVector> cVectorMap = Maps.newHashMap();

    /**
     * sparsity penalty
     */
    private double lambda;

    /**
     * Weight matrix for MMSE
     */
    private double mmseWeight;

    /**
     * Shannon rate
     */
    private double rate;

    public static final double N0 = 1;

    private String name = null;

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
    public UE(double x, double y, int numAntennas, double lambda) {
        super(x, y, Entity.Type.UE, numAntennas);
        this.lambda = lambda;
    }

    public UE(double x, double y, int numAntennas, double lambda, String name) {
        super(x, y, Entity.Type.UE, numAntennas);
        this.lambda = lambda;
        this.name = name;
    }

    /**
     * @return the rxPrecodingMatrix
     */
    public ComplexVector getRxPreVector() {
        return rxPreVector;
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

    public boolean isToBeServedBy(BaseStation q) {
        ComplexVector cVector = cVectorMap.get(q);
        if (cVector == null || cVector.norm(Norm.Two) <= lambda / 2)
            return false;
        else
            return true;
    }

    public ComplexVector getCVector(BaseStation q) {
        return (ComplexVector) cVectorMap.get(q);
    }

    public void updateCVectorMap() {
        for (Cluster l : cluster.getClusterClosure()) {
            for (BaseStation q : l.getBSs()) {
                updateCVector(q);
            }
        }
    }

    /**
     * 
     * @param q
     *            target Base Station
     * @return The irrelevant part of partial derivative over Base Station
     *         <code>q</code>, <code>c<sub>ik</sub><sup>ql</sup></code>
     */
    private ComplexVector updateCVector(BaseStation q) {
        Cluster l = q.getCluster();
        ComplexVector cVector = new DenseComplexVector(q.getNumAntennas());
        cVector.zero();

        for (BaseStation p : l.getBSs()) {
            if (p == q) {
                continue;
            } else {
                ComplexVector vp = p.getTxPreVector(this);
                ComplexMatrix Mqp = network.getMMatrix(q, p);
                cVector.add(Mqp.mult(vp, new DenseComplexVector(Mqp.numRows())));
            }
        }
        ComplexMatrix localH = q.getMIMOChannel(this);
        cVector.add(localH
                .hermitianTranspose(new DenseComplexMatrix(localH.numColumns(), localH.numRows()))
                .mult(rxPreVector, new DenseComplexVector(localH.numColumns()))
                .scale(new double[] { -mmseWeight, 0 }));
        cVector.scale(new double[] { 2, 0 });
        for (Cluster m : cluster.getClusterClosure()) {
            if (m == l) {
                continue;
            } else {
                for (BaseStation p : m.getBSs()) {
                    ComplexMatrix Mlmqp = network.getMMatrix(q, p);
                    ComplexMatrix Mmlpq = network.getMMatrix(p, q);
                    ComplexVector vp = p.getTxPreVector(this);
                    cVector.add(Mlmqp.mult(vp, new DenseComplexVector(Mlmqp.numRows())));
                    cVector.add(Mmlpq.hermitianTranspose(
                            new DenseComplexMatrix(Mmlpq.numColumns(), Mmlpq.numRows())).mult(vp,
                            new DenseComplexVector(Mmlpq.numColumns())));
                }
            }
        }
        cVector.scale(new double[] { -0.5, 0 });
        cVectorMap.put(q, cVector);
        return cVector;
    }

    private double getUpperBoundOfLagrangianMultiplier(BaseStation q) {
        double muH = 0.0;
        double factor = Math.pow(q.getPowerBudget() / q.getNumberOfUEsToBeServed(), -0.5);
        for (UE ue : q.getUEsToBeServed()) {
            double tmp = factor * ue.getCVector(q).norm(Norm.Two);
            if (muH < tmp)
                muH = tmp;
        }
        return muH;
    }

    private double getUpperBoundOfInverseOfTxPreVectorNorm(BaseStation q) {
        double thetaH = 0.0;
        double muH = getUpperBoundOfLagrangianMultiplier(q);
        try {
            thetaH = (ComplexMatrices.spectralRadius(network.getMMatrix(q, q)) + muH)
                    / (getCVector(q).norm(Norm.Two) - lambda / 2);
        } catch (NotConvergedException e) {
            logger.error("spectral radius calculation error");
        }
        return thetaH;
    }

    /**
     * Target function for bisection method. Partial derivative is
     * <code>2*(M-c)</code>
     * 
     * @param mu
     *            Lagrangian multiplier
     * @param theta
     *            inverse of norm of tx precoding vector
     * @param M
     *            M matrix
     * @param c
     *            c vector
     * @return optimal Lagrangian multiplier
     */
    public double bisectionTarget(double mu, double theta, ComplexMatrix M, ComplexVector c) {
        return theta
                * ComplexMatrices.eye(M.numRows())
                        .scale(new double[] { lambda * theta / 2.0 + mu, 0 }).add(M).inverse()
                        .mult(c, new DenseComplexVector(M.numRows())).norm(Norm.Two);
    }

    /**
     * Use the block coordinate descent method to optimize the subproblem
     */
    public void optimize() {
        Set<Cluster> clusters = cluster.getClusterClosure();
        boolean flag = true;
        final int maxCount = 25;
        int count = 0;
        logger.debug("Optimize tx vector to " + this);
        do {
            if (count++ > maxCount)
                break;
            for (Cluster l : clusters) {
                for (BaseStation q : l.getBSs()) {
                    q.setSubgradient(this, -blockCoordinateDescent(q));

                }
            }
            updateCVectorMap();
            flag = false;
            for (Cluster l : clusters) {
                for (BaseStation q : l.getBSs()) {
                    ComplexVector c = getCVector(q);
                    ComplexMatrix M = network.getMMatrix(q, q);
                    if (c.norm(Norm.Two) <= lambda / 2
                            && q.getTxPreVector(this).norm(Norm.Two) > 0.01) {
                        flag = true;
                        break;
                    } else {
                        ComplexVector left = new DenseComplexVector(q.getNumAntennas());
                        left.set(q.getTxPreVector(this));
                        left.scale(new double[] { -q.getSubgradient(this), 0 });
                        left.add(M.mult(q.getTxPreVector(this),
                                new DenseComplexVector(q.getNumAntennas())));
                        left.add(new double[] { -1, 0 }, c);
                        left.scale(new double[] { -2, 0 });

                        ComplexVector right = new DenseComplexVector(q.getNumAntennas());
                        right.set(q.getTxPreVector(this));
                        right.scale(new double[] { lambda / q.getTxPreVector(this).norm(Norm.Two),
                                0.0 });
                        left.add(new double[] { -1, 0 }, right);
                        if (left.norm(Norm.Two) > 0.01) {
                            flag = true;
                            break;
                        }
                    }
                }
            }
        } while (flag);
        logger.debug("Optimize tx vector to " + this + " done!");
    }

    /**
     * The block variable is <code>v<sub>ik</sub><sup>ql</sup></code>
     * 
     * @param q
     *            Base station
     * @return Lagrangian multiplier
     */
    public double blockCoordinateDescent(BaseStation q) {
        ComplexVector c = updateCVector(q);
        logger.debug("Optimize tx vector from " + q + " to " + this);
        double multiplier = 0.0;
        double theta = 0.0;
        if (c.norm(Norm.Two) <= lambda / 2) {
            q.getTxPreVector(this).zero();
            multiplier = 0.0;
            logger.debug("Sparsity is ensured between " + q + " and " + this);
        } else {
            ComplexMatrix M = network.getMMatrix(q, q);
            theta = 1 / Math.sqrt(q.getPowerAllocation(this));
            double targetValue = bisectionTarget(0, theta, M, c);
            if (targetValue > 1.0) {
                double mLow = 0.0;
                double mHigh = getUpperBoundOfLagrangianMultiplier(q);
                while (bisectionTarget(mHigh, theta, M, c) >= 1.0) {
                    mHigh *= 2;
                }
                do {
                    multiplier = (mLow + mHigh) / 2;
                    targetValue = bisectionTarget(multiplier, theta, M, c);
                    if (targetValue > 1)
                        mLow = multiplier;
                    else
                        mHigh = multiplier;
                } while (Math.abs(targetValue - 1) > 1e-6);
            } else if (targetValue < 1.0) {
                double tLow = theta;
                double tHigh = getUpperBoundOfInverseOfTxPreVectorNorm(q);
                multiplier = 0.0;
                while (bisectionTarget(multiplier, tHigh, M, c) < 1.0) {
                    tHigh *= 2.0;
                }
                if (Double.isInfinite(tHigh)) {
                    theta = tHigh;
                } else {
                    do {
                        theta = (tLow + tHigh) / 2;
                        targetValue = bisectionTarget(multiplier, theta, M, c);
                        if (targetValue > 1)
                            tHigh = theta;
                        else
                            tLow = theta;
                    } while (Math.abs(targetValue - 1) > 1e-6);
                }
            }
            ComplexVector v = null;
            if (Double.isInfinite(theta)) {
                v = new DenseComplexVector(q.getNumAntennas());
                v.zero();
            } else
                v = ComplexMatrices.eye(q.getNumAntennas())
                        .scale(new double[] { lambda * theta / 2 + multiplier, 0 }).add(M)
                        .inverse().mult(c, new DenseComplexVector(q.getNumAntennas()));
            logger.debug("Use block coordinate descent, get multipler: " + multiplier + "; theta: "
                    + theta + "; tx vector: " + v);
            q.setTxPreVector(this, v);
        }
        return multiplier;
    }

    // private double objectiveValue() {
    // double ret = 0.0;
    // for (Cluster l : cluster.getClusterClosure()) {
    // for (Cluster m : cluster.getClusterClosure()) {
    // for (BaseStation q : l.getBSs()) {
    // for (BaseStation p : m.getBSs()) {
    // double dot = q.getTxPreVector(this).dot(
    // network.getMMatrix(q, p).mult(p.getTxPreVector(this),
    // new DenseComplexVector(q.getNumAntennas())))[0];
    // ret += dot;
    // }
    // }
    // }
    // }
    // ComplexVector tmp = new DenseComplexVector(getNumAntennas());
    // tmp.zero();
    // for (Cluster l : cluster.getClusterClosure()) {
    // for (BaseStation q : l.getBSs()) {
    // tmp.add(q.getMIMOChannel(this).mult(q.getTxPreVector(this),
    // new DenseComplexVector(getNumAntennas())));
    // ret += lambda * q.getTxPreVector(this).norm(Norm.Two);
    // }
    // }
    // ret -= 2 * mmseWeight * rxPreVector.dot(tmp)[0];
    // return ret;
    // }

    /**
     * Calculate rx precoding matrix for
     */
    public void updateVariables() {
        ComplexMatrix C = new DenseComplexMatrix(getNumAntennas(), getNumAntennas());
        C.zero();
        ComplexMatrix L = new DenseComplexMatrix(getNumAntennas(), getNumAntennas());
        L.zero();
        ComplexMatrix localHvvH = new DenseComplexMatrix(getNumAntennas(), getNumAntennas());
        localHvvH.zero();
        ComplexVector localHv = new DenseComplexVector(getNumAntennas());
        localHv.zero();

        for (Cluster l : network.getClusters()) {
            for (Cluster lPrim : network.getClusters()) {
                Set<Cluster> commonClusters = Sets.intersection(l.getClusterClosure(),
                        lPrim.getClusterClosure());
                for (Cluster m : commonClusters) {
                    for (UE j : m.getUEs()) {
                        ComplexMatrix HvvH = null;
                        ComplexVector HliVlj = new DenseComplexVector(getNumAntennas());
                        HliVlj.zero();
                        for (BaseStation q : l.getBSs()) {
                            HliVlj.add(q.getMIMOChannel(this).mult(q.getTxPreVector(j),
                                    new DenseComplexVector(HliVlj.size())));
                        }
                        if (l == lPrim) {
                            HvvH = HliVlj.mult(
                                    HliVlj.conjugate(new DenseComplexVector(HliVlj.size())),
                                    new DenseComplexMatrix(HliVlj.size(), HliVlj.size()));
                            if (j == this) {
                                localHvvH.add(HvvH);
                                localHv.add(HliVlj);
                            }
                        } else {
                            ComplexVector Hl_iVl_j = new DenseComplexVector(getNumAntennas());
                            Hl_iVl_j.zero();
                            for (BaseStation q : lPrim.getBSs()) {
                                Hl_iVl_j.add(q.getMIMOChannel(this).mult(q.getTxPreVector(j),
                                        new DenseComplexVector(Hl_iVl_j.size())));
                            }
                            HvvH = HliVlj.mult(
                                    Hl_iVl_j.conjugate(new DenseComplexVector(Hl_iVl_j.size())),
                                    new DenseComplexMatrix(HliVlj.size(), HliVlj.size()));
                        }
                        C.add(HvvH);
                    }
                }
            }
        }
        C.add(new double[] { N0, 0 }, ComplexMatrices.eye(C.numRows()));
        logger.debug("C matrix: " + C);
        logger.debug("Local HvvH: " + localHvvH);
        logger.debug("Local Hv: " + localHv);
        // L = C - localHvvH
        L.set(C).add(new double[] { -1, 0 }, localHvvH);
        logger.debug("L matrix: " + L);

        ComplexMatrix Cinv = C.inverse();
        logger.debug("Cinv: " + Cinv);
        logger.debug("C * Cinv: " + C.mult(Cinv));

        rxPreVector = Cinv.mult(localHv, new DenseComplexVector(Cinv.numRows()));
        mmseWeight = 1.0 / (1 - localHv.dot(rxPreVector)[0]);
        rate = Utils.log2(ComplexMatrices
                .eye(getNumAntennas())
                .add(localHv.mult(localHv.conjugate(new DenseComplexVector(localHv.size())),
                        new DenseComplexMatrix(localHv.size(), localHv.size())).mult(L.inverse()))
                .det2()) / 2.0;
        logger.debug("Update " + this + ": MMSE weight: " + mmseWeight + "; Shannon rate: " + rate
                + "; rx precoding vector: " + rxPreVector);
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
    public double getMMSEWeight() {
        return mmseWeight;
    }

    /**
     * @return the rate
     */
    public double getRate() {
        return rate;
    }

    public void setLambda(double lambda) {
        this.lambda = lambda;
    }

    @Override
    public String toString() {
        return String.format("UE#%s@(%.4f,%.4f)", name, getXY()[0], getXY()[1]);
    }

    public double getLambda() {
        return lambda;
    }
}
