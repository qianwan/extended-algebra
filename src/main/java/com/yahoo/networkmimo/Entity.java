package com.yahoo.networkmimo;

import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Assert;

import com.beust.jcommander.internal.Maps;
import com.yahoo.algebra.matrix.DenseComplexMatrix;

public abstract class Entity implements MIMOChannel {
	Logger logger = Logger.getLogger(Entity.class);

	private double x;

	private double y;

	private int numAntennas;

	protected Map<Entity, DenseComplexMatrix> mimoChannels = Maps.newHashMap();

	public enum Type {
		BS, UE, UNKNOWN
	};

	Type t = Type.UNKNOWN;

	public Entity() {
		this(0, 0, Type.UNKNOWN, 1);
	}

	public Entity(double x, double y) {
		this(x, y, Type.UNKNOWN, 1);
	}

	public Entity(double x, double y, Type t) {
		this(x, y, t, 1);
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

	public DenseComplexMatrix generateMIMOChannel(Entity e) {
		Assert.assertFalse(e.getType() == this.getType());
		DenseComplexMatrix H = new DenseComplexMatrix(e.getNumAntennas(),
				this.getNumAntennas());
		// TODO rayleigh channel

		mimoChannels.put(e, H);
		return H;
	}

	public DenseComplexMatrix getMIMOChannel(Entity e) {
		DenseComplexMatrix H = mimoChannels.get(e);
		if (H == null) {
			H = this.generateMIMOChannel(e);
			mimoChannels.put(e, H);
		}
		return H;
	}

	public void setMIMOChannel(Entity e, DenseComplexMatrix H) {
		Assert.assertFalse(e.getType() == this.getType());
		if (H.numRows() != e.getNumAntennas()
				|| H.numColumns() != this.getNumAntennas()) {
			logger.error(
					"MIMO channel not compatible with number of antennas of entities",
					new Exception());
		}
		mimoChannels.put(e, H);
	}
}
