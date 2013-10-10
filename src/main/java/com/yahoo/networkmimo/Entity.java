package com.yahoo.networkmimo;

import static java.lang.Math.sqrt;

import java.util.Map;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncommons.maths.random.GaussianGenerator;
import org.uncommons.maths.random.MersenneTwisterRNG;

import com.google.common.collect.Maps;
import com.yahoo.algebra.matrix.ComplexMatrix;
import com.yahoo.algebra.matrix.ComplexMatrixEntry;
import com.yahoo.algebra.matrix.DenseComplexMatrix;

public abstract class Entity implements MIMOChannel {
    private static final Logger logger = LoggerFactory.getLogger(Entity.class);

    protected static final GaussianGenerator rng;

    static {
        // byte[] seed = new byte[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
        // 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
        rng = new GaussianGenerator(0, 1, new MersenneTwisterRNG());
    }

    private double x;

    private double y;

    private int numAntennas;

    protected final Map<Entity, ComplexMatrix> mimoChannels = Maps.newHashMap();

    public enum Type {
        BS, CLUSTER, UE, UNKNOWN
    };

    private Type t = Type.UNKNOWN;

    public Entity() {
        this(0, 0, Type.UNKNOWN, 0);
    }

    public Entity(double x, double y) {
        this(x, y, Type.UNKNOWN, 0);
    }

    public Entity(double x, double y, Type t) {
        this(x, y, t, 0);
    }

    public Entity(double x, double y, Type t, int numAntennas) {
        setXY(x, y);
        this.t = t;
        this.numAntennas = numAntennas;
    }

    public void setXY(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double[] getXY() {
        return new double[] { x, y };
    }

    public void setNumAntennas(int numAntennas) {
        this.numAntennas = numAntennas;
    }

    public int getNumAntennas() {
        return numAntennas;
    }

    public Type getType() {
        return this.t;
    }

    public void setType(Type t) {
        this.t = t;
    }

    /**
     * return a ComplexMatrix, dimension: e.getNumAntennas x this.getNumAntennas
     */
    public ComplexMatrix genenerateMIMOChannel(Entity e) {
        Assert.assertFalse(e.getType() == this.getType());
        DenseComplexMatrix H = new DenseComplexMatrix(e.getNumAntennas(), this.getNumAntennas());
        double distance = Utils.getEntityDistance(this, e);
        double sigma = getChannelGain(distance, "IWMMSE");

        for (ComplexMatrixEntry entry : H) {
            entry.set(new double[] { rng.nextValue() * sigma, rng.nextValue() * sigma });
        }
        mimoChannels.put(e, H);
        logger.debug("MIMO channel coefficient between " + this + " and " + e.toString() + ": " + H);
        return H;
    }

    public ComplexMatrix getMIMOChannel(Entity e) {
        return mimoChannels.get(e);
    }

    private double getChannelGain(double distance, String mode) {
        if (mode.equalsIgnoreCase("S-WMMSE")) {
            double L = Math.pow(10, rng.nextValue() * 8 / 10.0);
            double sigma2 = Math.pow(200.0 / distance, 3) * L;
            return Math.sqrt(sigma2);
        } else if (mode.equalsIgnoreCase("IWMMSE")) {
            return 1 / sqrt(2);
        }
        return 0.0;
    }
}
