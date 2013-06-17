package com.yahoo.networkmimo;

import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.uncommons.maths.random.GaussianGenerator;
import org.uncommons.maths.random.MersenneTwisterRNG;

import com.beust.jcommander.internal.Maps;
import com.yahoo.algebra.matrix.ComplexMatrix;
import com.yahoo.algebra.matrix.ComplexMatrixEntry;
import com.yahoo.algebra.matrix.DenseComplexMatrix;

public abstract class Entity implements MIMOChannel {
    Logger logger = Logger.getLogger(Entity.class);

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

    Type t = Type.UNKNOWN;

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
        return this.numAntennas;
    }

    public Type getType() {
        return this.t;
    }

    public void setType(Type t) {
        this.t = t;
    }

    public ComplexMatrix generateMIMOChannel(Entity e) {
        Assert.assertFalse(e.getType() == this.getType());
        DenseComplexMatrix H = new DenseComplexMatrix(e.getNumAntennas(), this.getNumAntennas());
        double[] p = e.getXY();
        double distance = Math.sqrt(Math.pow(p[0] - x, 2) + Math.pow(p[1] - y, 2));
        double channelGain = this.getChannelGain(distance, 2);
        double sigma = Math.sqrt(channelGain / 2);
        for (ComplexMatrixEntry entry : H) {
            entry.set(new double[] { rng.nextValue() * sigma, rng.nextValue() * sigma });
        }
        mimoChannels.put(e, H);
        return H;
    }

    public ComplexMatrix getMIMOChannel(Entity e) {
        ComplexMatrix H = mimoChannels.get(e);
        if (H == null) {
            H = generateMIMOChannel(e);
            mimoChannels.put(e, H);
        }
        return H;
    }

    public void setMIMOChannel(Entity e, ComplexMatrix H) {
        Assert.assertFalse(e.getType() == this.getType());
        if (H.numRows() != e.getNumAntennas() || H.numColumns() != this.getNumAntennas()) {
            logger.error("MIMO channel not compatible with number of antennas of entities",
                    new Exception());
        }
        mimoChannels.put(e, H);
    }

    private double getChannelGain(double distance, int mode) {
        double gainDb;
        if (mode == 1) {
            if (distance < 50) {
                gainDb = 122 + 38 * Math.log10(50.0 / 1000);
            } else {
                gainDb = 122 + 38 * Math.log10(distance / 1000);
            }
        } else {
            if (distance < 50) {
                gainDb = 128.1 + 37.6 * Math.log10(50.0 / 1000);
            } else {
                gainDb = 128.1 + 37.6 * Math.log10(distance / 1000);
            }
        }
        double gainTmp = Math.pow(10.0, -gainDb / 10);
        double shadow = Math.pow(10.0, 8 * rng.nextValue() / 10);
        return gainTmp * shadow;
    }
}
