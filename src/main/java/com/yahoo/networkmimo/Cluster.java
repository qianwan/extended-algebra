package com.yahoo.networkmimo;

import java.util.List;
import java.util.Map;
import java.util.Set;

import no.uib.cipr.matrix.NotConvergedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncommons.maths.random.ContinuousUniformGenerator;
import org.uncommons.maths.random.MersenneTwisterRNG;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.yahoo.algebra.matrix.ComplexMatrices;
import com.yahoo.algebra.matrix.ComplexMatrix;
import com.yahoo.algebra.matrix.ComplexVector;
import com.yahoo.algebra.matrix.DenseComplexMatrix;
import com.yahoo.algebra.matrix.DenseComplexVector;
import com.yahoo.networkmimo.exception.ClusterNotReadyException;

public class Cluster extends Entity {
    private static final Logger logger = LoggerFactory.getLogger(Cluster.class);

    private Network network;

    private final Set<BaseStation> bss = Sets.newHashSet();

    private final List<BaseStation> bsList = Lists.newArrayList();

    private final Set<UE> ues = Sets.newHashSet();

    private final Set<Cluster> closure = Sets.newHashSet();

    private String name;

    public final static ContinuousUniformGenerator rng = new ContinuousUniformGenerator(0, 1,
            new MersenneTwisterRNG());

    private Map<UE, ComplexVector> txPreVectors = Maps.newHashMap();

    public Cluster() {
        super();
        setType(Entity.Type.CLUSTER);
    }

    public Cluster(double x, double y) {
        super(x, y);
        setType(Entity.Type.CLUSTER);
    }

    public Cluster(double x, double y, String name) {
        super(x, y);
        setType(Entity.Type.CLUSTER);
        this.name = name;
    }

    public Cluster addBaseStation(BaseStation bs) {
        bss.add(bs);
        bs.setCluster(this);
        bs.setNetwork(network);
        if (network != null) {
            network.addBaseStation(bs);
        }
        setNumAntennas(getNumAntennas() + bs.getNumAntennas());
        logger.debug("Add " + bs + " to " + this + " in " + network);
        return this;
    }

    public Cluster addUE(UE ue) {
        ues.add(ue);
        ue.setCluster(this);
        ue.setNetwork(network);
        if (network != null) {
            network.addUE(ue);
        }
        logger.debug("Add " + ue + " to " + this + " in " + network);
        return this;
    }

    public Set<Cluster> getClusterClosure() {
        if (!closure.isEmpty())
            return closure;

        if (network == null) {
            throw new ClusterNotReadyException(
                    "this cluster is not add to any network, so cannot get closure");
        }

        double closureDistance = network.getClosureDistance();
        for (Cluster cluster : network.getClusters()) {
            if (Utils.getEntityDistance(this, cluster) < closureDistance)
                closure.add(cluster);
        }
        return closure;
    }

    /**
     * @return the bss
     */
    public Set<BaseStation> getBSs() {
        return bss;
    }

    /**
     * @return the ues
     */
    public Set<UE> getUEs() {
        return ues;
    }

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    @Override
    public String toString() {
        return String.format("Cluster#%s", name);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        return false;
    }

    @Override
    public ComplexMatrix getMIMOChannel(Entity e) {
        return mimoChannels.get(e);
    }

    /**
     * 
     * @param numAntennas number of antennas
     * @param power power budget
     * @param num number of base stations
     * @param r outer radius
     */
    public void generateRandomBSs(int numAntennas, double power, int num, double r) {
        for (int i = 0; i < num; i++) {
            boolean valid = false;
            double xx;
            double yy;
            do {
                valid = true;
                xx = (rng.nextValue() - 0.5) * 2 * r;
                yy = (rng.nextValue() - 0.5) * 2 * r;
                double mirrorX = Math.abs(xx);
                double mirrorY = Math.abs(yy);
                if ((mirrorY > r * Math.sin(Math.PI / 3))) {
                    valid = false;
                } else if ((Math.sqrt(3) * mirrorX + mirrorY) > (r * Math.sqrt(3))) {
                    valid = false;
                }
            } while (!valid);
            addBaseStation(new BaseStation(getXY()[0] + xx, getXY()[1] + yy, numAntennas, power,
                    Integer.toString(i + 1)));
        }
    }

    /**
     * 
     * @param numAntennas number of antennas
     * @param num number of user equipment
     * @param r outer radius
     */
    public void generateRandomUEs(int numAntennas, int num, double r) {
        for (int i = 0; i < num; i++) {
            boolean valid = false;
            double xx;
            double yy;
            do {
                valid = true;
                xx = (rng.nextValue() - 0.5) * 2 * r;
                yy = (rng.nextValue() - 0.5) * 2 * r;
                double mirrorX = Math.abs(xx);
                double mirrorY = Math.abs(yy);
                if (mirrorY > r * Math.sin(Math.PI / 3)) {
                    valid = false;
                } else if ((Math.sqrt(3) * mirrorX + mirrorY) > (r * Math.sqrt(3))) {
                    valid = false;
                }
            } while (!valid);
            addUE(new UE(getXY()[0] + xx, getXY()[1] + yy, numAntennas, Integer.toString(i + 1)));
        }
    }

    public void assembleMIMOChannel() {
        bsList.clear();
        bsList.addAll(bss);
        for (UE ue : network.getUEs()) {
            ComplexMatrix H = new DenseComplexMatrix(ue.getNumAntennas(), getNumAntennas());
            int offset = 0;
            for (BaseStation bs : bsList) {
                ComplexMatrix Hq = bs.getMIMOChannel(ue);
                for (int i = 0; i < Hq.numRows(); i++) {
                    for (int j = 0; j < Hq.numColumns(); j++) {
                        H.set(i, j + offset, Hq.get(i, j));
                    }
                }
                offset += Hq.numColumns();
            }
            mimoChannels.put(ue, H);
        }
    }

    public ComplexVector getTxPreVector(UE ue) {
        return txPreVectors.get(ue);
    }

    public void setTxPreVector(UE ue, ComplexVector v) {
        txPreVectors.put(ue, v);
    }

    public double searchMultiplier() {
        ComplexMatrix M = network.getMmseMMatrix(this);
        ComplexMatrix D = new DenseComplexMatrix(M.numRows(), M.numColumns());
        ComplexVector lambda = new DenseComplexVector(M.numRows());
        try {
            ComplexMatrices.eig(M, D, lambda);
        } catch (NotConvergedException e) {
            logger.error("Eigen decomposition not converge");
            throw new RuntimeException("Eigen decomposition not converge");
        }
        ComplexMatrix tmp = new DenseComplexMatrix(M.numRows(), M.numColumns());
        tmp.zero();
        for (UE ue : ues) {
            ComplexMatrix H = getMIMOChannel(ue);
            ComplexVector Hu = H.hermitianTranspose(
                    new DenseComplexMatrix(H.numColumns(), H.numRows())).mult(ue.getRxPreVector(),
                    new DenseComplexVector(H.numColumns()));
            ComplexMatrix HuuH = Hu.mult(Hu.conjugate(new DenseComplexVector(Hu.size())),
                    new DenseComplexMatrix(Hu.size(), Hu.size()));
            tmp.add(HuuH.scale(new double[] { ue.getMMSEWeight() * ue.getMMSEWeight(), 0 }));
        }
        ComplexMatrix phi = D
                .hermitianTranspose(new DenseComplexMatrix(D.numColumns(), D.numRows())).mult(tmp)
                .mult(D);
        double powerBudget = 0.0;
        for (BaseStation bs : bss)
            powerBudget += bs.getPowerBudget();
        double miuLow = 0.0;
        double miuHigh = 1.0;
        double multiplier = 0.0;
        while (bisectionTarget(phi, lambda, miuHigh) >= powerBudget) {
            miuHigh *= 2;
        }
        double targetValue = 0.0;
        do {
            multiplier = (miuLow + miuHigh) / 2;
            targetValue = bisectionTarget(phi, lambda, multiplier);
            if (targetValue > powerBudget)
                miuLow = multiplier;
            else if (targetValue < powerBudget)
                miuHigh = multiplier;
        } while (Math.abs(miuLow - miuHigh) > 1e-3);
        return multiplier;
    }

    public static double bisectionTarget(ComplexMatrix phi, ComplexVector lambda, double miu) {
        double ret = 0.0;
        for (int i = 0; i < phi.numRows(); i++) {
            ret += phi.get(i, i)[0] / Math.pow(lambda.get(i)[0] + miu, 2);
        }
        return ret;
    }

    public void iterateWMMSE(double multiplier) {
        ComplexMatrix tmp = ComplexMatrices.eye(getNumAntennas())
                .scale(new double[] { multiplier, 0 }).add(network.getMmseMMatrix(this)).inverse();
        for (UE ue : ues) {
            ComplexMatrix H = getMIMOChannel(ue);
            ComplexVector Hu = H.hermitianTranspose(
                    new DenseComplexMatrix(H.numColumns(), H.numRows())).mult(ue.getRxPreVector(),
                    new DenseComplexVector(H.numColumns()));
            Hu.scale(new double[] { ue.getMMSEWeight(), 0 });
            ComplexVector V = tmp.mult(Hu, new DenseComplexVector(tmp.numRows()));
            txPreVectors.put(ue, V);
            int offset = 0;
            for (BaseStation bs : bsList) {
                ComplexVector v = bs.getTxPreVector(ue);
                for (int i = 0; i < v.size(); i++) {
                    v.set(i, V.get(i + offset));
                }
                offset += bs.getNumAntennas();
            }
        }
    }

    public String getName() {
        return name;
    }
}
