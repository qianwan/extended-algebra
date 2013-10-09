package com.yahoo.networkmimo;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

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

    private Map<BaseStation, Double> lambdaMap = Maps.newHashMap();

    public static final double epsilon = 1e-6;

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
    public UE(double x, double y, int numAntennas) {
        super(x, y, Entity.Type.UE, numAntennas);
    }

    public UE(double x, double y, int numAntennas, String name) {
        super(x, y, Entity.Type.UE, numAntennas);
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
        if (cVector == null || cVector.norm(Norm.Two) <= lambdaMap.get(q) / 2)
            return false;
        else
            return true;
    }

    public ComplexVector getCVector(BaseStation q) {
        if (!cluster.getClusterClosure().contains(q.getCluster()))
            return null;
        ComplexVector c = (ComplexVector) cVectorMap.get(q);
        if (c != null)
            return c;
        else
            return updateCVector(q);
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
    public ComplexVector updateCVector(BaseStation q) {
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

    public double getUpperBoundOfLagrangianMultiplier(BaseStation q) {
        double muH = 0.0;
        double factor = Math.pow(q.getPowerBudget() / q.getNumberOfUEsToBeServed(), -0.5);
        for (UE ue : q.getUEsToBeServed()) {
            double tmp = factor * ue.updateCVector(q).norm(Norm.Two);
            if (muH < tmp)
                muH = tmp;
        }
        return muH;
    }

    public double getUpperBoundOfInverseOfTxPreVectorNorm(BaseStation q, double miuHigh) {
        double thetaH = 0.0;
        try {
            thetaH = (ComplexMatrices.spectralRadius(network.getMMatrix(q, q)) + miuHigh)
                    / (getCVector(q).norm(Norm.Two) - lambdaMap.get(q) / 2);
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
     * @param lambda
     *            sparsity penalty
     * @param M
     *            M matrix
     * @param c
     *            c vector
     * @return optimal Lagrangian multiplier
     */
    public static double bisectionTarget(double mu, double theta, double lambda, ComplexMatrix M,
            ComplexVector c) {
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
        logger.debug("Optimize tx vector to " + this);
        do {
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
                    if (c.norm(Norm.Two) <= lambdaMap.get(q) / 2
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
                        right.scale(new double[] {
                                lambdaMap.get(q) / q.getTxPreVector(this).norm(Norm.Two), 0.0 });
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

    public double blockCoordinateDescentWMMSE(BaseStation q) {
        logger.debug("WMMSE BCD");
        ComplexVector c = updateCVector(q);
        ComplexMatrix M = network.getMMatrix(q, q);
        ComplexVector v = M.inverse().mult(c, new DenseComplexVector(M.numColumns()));
        double multiplier = 0.0;
        if (Math.pow(v.norm(Norm.Two), 2) > q.getPowerAllocation(this)) {
            double miuLow = 0;
            double miuHigh = 1;
            v = ComplexMatrices.eye(q.getNumAntennas()).scale(new double[] { miuHigh, 0 }).add(M)
                    .inverse().mult(c, new DenseComplexVector(q.getNumAntennas()));
            while (Math.pow(v.norm(Norm.Two), 2) > q.getPowerAllocation(this)) {
                miuHigh *= 2;
                v = ComplexMatrices.eye(q.getNumAntennas()).scale(new double[] { miuHigh, 0 })
                        .add(M).inverse().mult(c, new DenseComplexVector(q.getNumAntennas()));
            }
            System.out.println(q.getPowerAllocation(this));
            v = ComplexMatrices.eye(q.getNumAntennas()).scale(new double[] { miuLow, 0 }).add(M)
                    .inverse().mult(c, new DenseComplexVector(q.getNumAntennas()));
            System.out.println(Math.pow(v.norm(Norm.Two), 2));
            v = ComplexMatrices.eye(q.getNumAntennas()).scale(new double[] { miuHigh, 0 }).add(M)
                    .inverse().mult(c, new DenseComplexVector(q.getNumAntennas()));
            System.out.println(Math.pow(v.norm(Norm.Two), 2));
            do {
                multiplier = (miuLow + miuHigh) / 2;
                logger.debug("stuck b");
                v = ComplexMatrices.eye(q.getNumAntennas()).scale(new double[] { multiplier, 0 })
                        .add(M).inverse().mult(c, new DenseComplexVector(q.getNumAntennas()));
                if (Math.pow(v.norm(Norm.Two), 2) > q.getPowerAllocation(this))
                    miuLow = multiplier;
                else if (Math.pow(v.norm(Norm.Two), 2) < q.getPowerAllocation(this))
                    miuHigh = multiplier;
            } while (Math.abs(Math.pow(v.norm(Norm.Two), 2) - q.getPowerAllocation(this)) > 1e-3);
        }
        q.setTxPreVector(this, v);
        return multiplier;
    }

    public double blockCoordinateDescentTest(BaseStation q) {
        ComplexVector c = updateCVector(q);
        double multiplier = 0.0;
        double lambda = lambdaMap.get(q);
        logger.debug("Optimize tx vector from " + q + " to " + this + " with lambda " + lambda);
        if (c.norm(Norm.Two) <= lambda / 2) {
            q.getTxPreVector(this).zero();
            multiplier = 0.0;
        } else {
            ComplexMatrix M = network.getMMatrix(q, q);
            double miuLow = 0.0;
            double miuHigh = getUpperBoundOfLagrangianMultiplier(q);
            double theta = 0.0;
            double targetValue = 0.0;
            do {
                multiplier = (miuLow + miuHigh) / 2;
                double thetaLow = 0;
                double thetaHigh = getUpperBoundOfInverseOfTxPreVectorNorm(q, miuHigh);
                do {
                    theta = (thetaLow + thetaHigh) / 2;
                    targetValue = bisectionTarget(multiplier, theta, lambda, M, c);
                    if (targetValue > 1)
                        thetaHigh = theta;
                    else
                        thetaLow = theta;
                } while (abs(thetaLow - thetaHigh) > 1e-6);
                if (1 / theta / theta < q.getPowerAllocation(this))
                    miuHigh = multiplier;
                else
                    miuLow = multiplier;
            } while (abs(miuLow - miuHigh) > 1e-6);
            ComplexVector v = ComplexMatrices.eye(q.getNumAntennas())
                    .scale(new double[] { lambda * theta / 2 + multiplier, 0 }).add(M).inverse()
                    .mult(c, new DenseComplexVector(q.getNumAntennas()));
            logger.debug("Use block coordinate descent test, get multipler: " + multiplier
                    + "; theta: " + theta + "; tx vector: " + v);
            q.setTxPreVector(this, v);
        }
        return multiplier;
    }

    /**
     * A first-order optimality condition criteria wrapper
     * 
     * @param q
     *            Base station
     * @return whether this subproblem satisfies the stopping criteria
     */
    public boolean blockCoordinateDescentWrapper(BaseStation q) {
        logger.debug("In BCD wrapper");
        ComplexVector origV = q.getTxPreVector(this);
        double origMultiplier = -q.getSubgradient(this);
        double multiplier = blockCoordinateDescent(q);
        q.setSubgradient(this, -multiplier);
        if (origV.add(new double[] { -1, 0 }, q.getTxPreVector(this)).norm(Norm.Two) < 1e-3
                && Math.abs(origMultiplier - multiplier) < 1e-3)
            return true;
        return false;
    }

    public boolean checkSubproblemConverged(BaseStation q) {
        ComplexMatrix M = network.getMMatrix(q, q);
        ComplexVector c = getCVector(q);
        double normC = c.norm(Norm.Two);
        double lambda = lambdaMap.get(q);
        ComplexVector v = q.getTxPreVector(this);
        double multiplier = -q.getSubgradient(this);
        double normV = v.norm(Norm.Two);
        if (normC <= lambda / 2) {
            if (multiplier == 0 && normV * normV <= epsilon)
                return true;
        } else {
            double theta = 1 / normV;
            ComplexVector updatedV = ComplexMatrices.eye(q.getNumAntennas())
                    .scale(new double[] { lambda * theta / 2 + multiplier, 0 }).add(M).inverse()
                    .mult(c, new DenseComplexVector(q.getNumAntennas()));
            updatedV.add(new double[] { -1, 0 }, v);
            if (updatedV.norm(Norm.Two) < 1e-3
                    && abs(multiplier * (q.getPowerAllocation(this) - normV * normV)) < 1e-3) {
                return true;
            }
        }
        return false;
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
        ComplexMatrix M = network.getMMatrix(q, q);
        logger.debug("Optimize tx vector from " + q + " to " + this);
        double multiplier = 0.0;
        double theta = 0.0;
        double lambda = lambdaMap.get(q);
        logger.debug("Norm of c vector is " + c.norm(Norm.Two));
        ComplexVector v = null;
        if (c.norm(Norm.Two) <= lambda / 2) {
            v = q.getTxPreVector(this);
            v.zero();
            q.setQuasiTxVector(this, new DenseComplexVector(q.getNumAntennas()).zero());
            multiplier = 0.0;
            theta = 1 / multiplier;
            q.setHessianDiagonal(this, 1);
            logger.debug("Sparsity is ensured between " + q + " and " + this);
        } else {
            double P = q.getPowerAllocation(this);
            theta = 1 / sqrt(P);
            double targetValue = bisectionTarget(0, theta, lambda, M, c);
            if (targetValue > 1.0) {
                double miuLow = 0.0;
                double miuHigh = c.norm(Norm.Two) / sqrt(P);
                do {
                    multiplier = (miuLow + miuHigh) / 2;
                    targetValue = bisectionTarget(multiplier, theta, lambda, M, c);
                    if (targetValue > 1)
                        miuLow = multiplier;
                    else if (targetValue < 1)
                        miuHigh = multiplier;
                    else
                        break;
                } while (Math.abs((miuLow - miuHigh) / miuHigh) > 1e-8);
                logger.debug("Multiplier " + multiplier + ", bisection target value "
                        + bisectionTarget(multiplier, theta, lambda, M, c));
            } else if (targetValue < 1.0) {
                double tLow = theta;
                double miuHigh = c.norm(Norm.Two) / sqrt(P);
                double tHigh = 0;
                try {
                    tHigh = (ComplexMatrices.spectralRadius(M) + miuHigh)
                            / (c.norm(Norm.Two) - lambda / 2);
                } catch (NotConvergedException e) {
                    throw new RuntimeException(
                            "Failed when calculating spectral radius of M matrix");
                }
                if (tHigh < tLow) {
                    throw new RuntimeException("Something is wrong here");
                }
                multiplier = 0.0;
                do {
                    theta = (tLow + tHigh) / 2;
                    targetValue = bisectionTarget(multiplier, theta, lambda, M, c);
                    if (targetValue > 1)
                        tHigh = theta;
                    else if (targetValue < 1)
                        tLow = theta;
                    else
                        break;
                } while (Math.abs((tHigh - tLow) / tHigh) > 1e-8);
                logger.debug("Theta is " + theta + ", bisection target value is "
                        + bisectionTarget(multiplier, theta, lambda, M, c));
            }
            v = ComplexMatrices.eye(q.getNumAntennas())
                    .scale(new double[] { lambda * theta / 2 + multiplier, 0 }).add(M).inverse()
                    .mult(c, new DenseComplexVector(q.getNumAntennas()));
            q.setTxPreVector(this, v);
            q.setQuasiTxVector(this, v.copy());
            if (multiplier == 0.0) {
                q.setHessianDiagonal(this, 1.0);
            } else {
                double delta = P * 0.05;
                P += delta;
                theta = 1 / sqrt(P);
                targetValue = bisectionTarget(0, theta, lambda, M, c);
                double miu = 0.0;
                if (targetValue > 1.0) {
                    double miuLow = 0.0;
                    double miuHigh = c.norm(Norm.Two) / sqrt(P);
                    do {
                        miu = (miuLow + miuHigh) / 2;
                        targetValue = bisectionTarget(miu, theta, lambda, M, c);
                        if (targetValue > 1)
                            miuLow = miu;
                        else if (targetValue < 1)
                            miuHigh = miu;
                        else
                            break;
                    } while (Math.abs((miuLow - miuHigh) / miuHigh) > 1e-8);
                    q.setHessianDiagonal(this, (-miu + multiplier) / delta);
                } else {
                    q.setHessianDiagonal(this, 1.0);
                }
            }
        }
        logger.debug("BCD, multipler: " + multiplier + "; hessian " + q.getHessianDiagonal(this)
                + "; theta: " + theta + "; txVector: " + v + "; power: "
                + q.getPowerAllocation(this) + "; MMatrix: " + M + "; cVector: " + c);
        // if (singularity) {
        // ComplexVector v = q.getTxPreVector(this);
        // v.zero();
        // v = q.getQuasiTxVector(this);
        // double delta = lambda * v.norm(Norm.Two);
        // delta += v.dot(M.mult(v, new DenseComplexVector(M.numRows())))[0];
        // double tmp = 0.0;
        // for (Cluster l : cluster.getClusterClosure()) {
        // for (BaseStation p : l.getBSs()) {
        // if (q == p)
        // continue;
        // else {
        // tmp += v.dot(network.getMMatrix(q, p).mult(p.getTxPreVector(this),
        // new DenseComplexVector(p.getNumAntennas())))[0];
        // }
        // }
        // }
        // delta += tmp * 2;
        // tmp = 2
        // * mmseWeight
        // * rxPreVector.dot(q.getMIMOChannel(this).mult(v,
        // new DenseComplexVector(getNumAntennas())))[0];
        // delta -= tmp;
        // multiplier = delta / deltaP;
        // multiplier = multiplier <= 0 ? 0 : multiplier;
        // logger.debug("Calculated derivative is " + multiplier);
        // } else {
        // logger.debug("Multiplier derivative is " + multiplier);
        // }
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
        ComplexMatrix localHvvH = null;
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
        for (Cluster l : cluster.getClusterClosure()) {
            for (BaseStation q : l.getBSs()) {
                ComplexMatrix H = q.getMIMOChannel(this);
                ComplexVector v = q.getTxPreVector(this);
                localHv.add(H.mult(v, new DenseComplexVector(H.numRows())));
            }
        }
        localHvvH = localHv.mult(localHv.conjugate(new DenseComplexVector(localHv.size())),
                new DenseComplexMatrix(localHv.size(), localHv.size()));
        L.set(C).add(new double[] { -1, 0 }, localHvvH);
        ComplexMatrix Cinv = C.inverse();

        rxPreVector = Cinv.mult(localHv, new DenseComplexVector(Cinv.numRows()));
        mmseWeight = 1.0 / (1 - localHv.dot(rxPreVector)[0]);
        rate = Utils.log2(ComplexMatrices.eye(getNumAntennas()).add(localHvvH.mult(L.inverse()))
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

    @Override
    public String toString() {
        return String.format("UE#%s@%s", name, cluster == null ? null : cluster.getName());
    }

    public ComplexMatrix calculateCMatrix1() {
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
        return C;
    }

    public ComplexMatrix calculateCMatrix2() {
        ComplexMatrix C = new DenseComplexMatrix(getNumAntennas(), getNumAntennas());
        C.zero();
        for (Cluster l : network.getClusters()) {
            for (UE j : l.getUEs()) {
                for (Cluster l1 : l.getClusterClosure()) {
                    for (BaseStation q1 : l1.getBSs()) {
                        for (Cluster l2 : l.getClusterClosure()) {
                            for (BaseStation q2 : l2.getBSs()) {
                                ComplexVector Hv = q1.getMIMOChannel(this).mult(
                                        q1.getTxPreVector(j),
                                        new DenseComplexVector(getNumAntennas()));
                                ComplexVector Hv_ = q2.getMIMOChannel(this).mult(
                                        q2.getTxPreVector(j),
                                        new DenseComplexVector(getNumAntennas()));
                                C.add(Hv.mult(Hv_.conjugate(new DenseComplexVector(Hv_.size())),
                                        new DenseComplexMatrix(Hv.size(), Hv.size())));
                            }
                        }
                    }
                }
            }
        }
        C.add(new double[] { N0, 0 }, ComplexMatrices.eye(C.numRows()));
        return C;
    }

    public double updateLambda(BaseStation q) {
        if (!cluster.getClusterClosure().contains(q.getCluster())) {
            logger.error("Non-adjacent BS-UE pair should not be optimized");
            return Double.MAX_VALUE;
        }
        // ComplexMatrix H = q.getMIMOChannel(this);
        // double rxPower = H.mult(
        // H.hermitianTranspose(new DenseComplexMatrix(H.numColumns(),
        // H.numRows()))).trace()[0];
        /**
         *  0dB, 0.5
         *  5dB, 0.28117066259517454
         * 10dB, 0.15811388300841894
         * 15dB, 0.08891397050194613
         * 20dB, 0.05
         * 25dB, 0.028117066259517452
         * 30dB, 0.015811388300841896
         */
        double lambda = 0.5;
        lambdaMap.put(q, lambda);
        // lambdaMap.put(q, 0.2);
        return lambda;
    }

    public void updateLambdaMap() {
        for (Cluster l : getCluster().getClusterClosure()) {
            for (BaseStation q : l.getBSs()) {
                updateLambda(q);
            }
        }
    }

    public double getLambda(BaseStation q) {
        if (!lambdaMap.containsKey(q))
            return Double.MAX_VALUE;
        return lambdaMap.get(q);
    }

    public Map<BaseStation, Double> getLambdaMap() {
        return lambdaMap;
    }
}
