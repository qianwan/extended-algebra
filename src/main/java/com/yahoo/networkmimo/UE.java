package com.yahoo.networkmimo;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.uib.cipr.matrix.NotConvergedException;

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
        for (Cluster l : cluster.getClosureCluster()) {
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
    private void updateCVector(BaseStation q) {
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
        for (Cluster m : cluster.getClosureCluster()) {
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
    private double bisectionTarget(double mu, double theta, ComplexMatrix M, ComplexVector c) {
        return theta
                * ComplexMatrices.eye(M.numRows())
                        .scale(new double[] { lambda * theta / 2.0 + mu, 0 }).add(M).inverse()
                        .mult(c, new DenseComplexVector(M.numRows())).norm(Norm.Two);
    }

    /**
     * 
     * @param q
     *            The base station to optimize the tx precoding vector
     * @return Optimal Lagrangian multiplier
     */
    public double optimize(BaseStation q) {
        double multiplier = 0.0;
        double theta = 0.0;
        ComplexVector c = getCVector(q);
        if (c.norm(Norm.Two) < lambda) {
            q.getTxPreVector(this).zero();
            multiplier = 0.0;
        } else {
            double mLow = 0.0;
            double mHigh = getUpperBoundOfLagrangianMultiplier(q);
            double tLow = 0.0;
            double tHigh = getUpperBoundOfInverseOfTxPreVectorNorm(q);
            do {
                multiplier = (mLow + mHigh) / 2;
                do {
                    theta = (tLow + tHigh) / 2;
                    double target = bisectionTarget(multiplier, theta, network.getMMatrix(q, q),
                            getCVector(q));
                    if (target < 1)
                        tLow = theta;
                    else
                        tHigh = theta;
                } while (Math.abs(tHigh - tLow) < 1e-2);
                if (1 / theta / theta < q.getPowerAllocation(this))
                    mHigh = multiplier;
                else
                    mLow = multiplier;
            } while (Math.abs(mHigh - mLow) < 1e-2);
            ComplexVector v = ComplexMatrices.eye(q.getNumAntennas())
                    .scale(new double[] { lambda * theta / 2 + multiplier, 0 })
                    .add(network.getMMatrix(q, q)).inverse()
                    .mult(getCVector(q), new DenseComplexVector(q.getNumAntennas()));
            q.setTxPreVector(this, v);
        }

        return multiplier;
    }

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
                Set<Cluster> commonClusters = Sets.intersection(l.getClosureCluster(),
                        lPrim.getClosureCluster());
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
        logger.info("C matrix:\n" + C);
        logger.info("Local HvvH\n" + localHvvH);
        logger.info("Local Hv\n" + localHv);
        // L = C - localHvvH
        L.set(C).add(new double[] { -1, 0 }, localHvvH);

        ComplexMatrix Cinv = C.inverse();
        rxPreVector = Cinv.mult(localHv, new DenseComplexVector(Cinv.numRows()));
        mmseWeight = 1.0 / (1 - localHv.dot(rxPreVector)[0]);
        rate = Utils.log2(ComplexMatrices
                .eye(getNumAntennas())
                .add(localHv.mult(localHv.conjugate(new DenseComplexVector(localHv.size())),
                        new DenseComplexMatrix(localHv.size(), localHv.size())).mult(L.inverse()))
                .det2()) / 2.0;
        logger.info("Update " + this + ": MMSE weight: " + mmseWeight + "; Shannon rate: " + rate
                + "; rx precoding vector\n" + rxPreVector);
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
        return String.format("UE@(%.4f,%.4f)", getXY()[0], getXY()[1]);
    }
}
