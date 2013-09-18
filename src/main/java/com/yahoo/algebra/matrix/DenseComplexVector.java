package com.yahoo.algebra.matrix;

import java.io.Serializable;
import java.util.Arrays;

public class DenseComplexVector extends AbstractComplexVector implements Serializable {

    /** just the private data */
    private static final long serialVersionUID = 3336086910170309635L;

    /**
     * Vector data
     */
    private final double[] data;

    public DenseComplexVector(int size) {
        super(size);
        data = new double[size * 2];
    }

    /**
     * Constructor for DenseVector
     * 
     * @param x
     *            Copies contents from this vector. A deep copy is made
     */
    public DenseComplexVector(DenseComplexVector x) {
        this(x, true);
    }

    /**
     * Constructor for DenseVector
     * 
     * @param x
     *            Copies contents from this vector
     * @param deep
     *            True for a deep copy. For a shallow copy, <code>x</code> must
     *            be a <code>DenseVector</code>
     */

    public DenseComplexVector(ComplexVector x, boolean deep) {
        super(x);

        if (deep) {
            data = new double[size];
            set(x);
        } else
            data = ((DenseComplexVector) x).getData();
    }

    @Override
    public void set(int index, double value[]) {
        data[index] = value[0];
        data[size + index] = value[1];
    }

    @Override
    public double[] get(int index) {
        return new double[] { data[index], data[size + index] };
    }

    @Override
    public ComplexVector copy() {
        return new DenseComplexVector(this);
    }

    @Override
    public DenseComplexVector zero() {
        Arrays.fill(data, 0);
        return this;
    }

    @Override
    public DenseComplexVector scale(double alpha[]) {
        for (int i = 0; i < size; ++i) {
            double re = data[i] * alpha[0] - data[i + size] * alpha[1];
            double im = data[i] * alpha[1] + data[i + size] * alpha[0];
            data[i] = re;
            data[i + size] = im;
        }
        return this;
    }

    @Override
    public ComplexVector set(ComplexVector y) {
        if (!(y instanceof DenseComplexVector))
            return super.set(y);

        checkSize(y);

        double[] yd = ((DenseComplexVector) y).getData();
        System.arraycopy(yd, 0, data, 0, data.length);

        return this;
    }

    @Override
    public ComplexVector set(double alpha[], ComplexVector y) {
        if (!(y instanceof DenseComplexVector))
            return super.set(alpha, y);

        checkSize(y);

        if (alpha[0] == 0 && alpha[1] == 0)
            return zero();

        double[] yd = ((DenseComplexVector) y).getData();

        for (int i = 0; i < size; ++i) {
            data[i] = alpha[0] * yd[i] - alpha[1] * yd[i + size];
            data[i + size] = alpha[1] * yd[i] + alpha[0] * yd[i + size];
        }

        return this;
    }

    @Override
    public ComplexVector add(ComplexVector y) {
        if (!(y instanceof DenseComplexVector))
            return super.add(y);

        checkSize(y);

        double[] yd = ((DenseComplexVector) y).getData();

        for (int i = 0; i < yd.length; i++) {
            data[i] += yd[i];
        }

        return this;
    }

    @Override
    public ComplexVector add(double alpha[], ComplexVector y) {
        if (!(y instanceof DenseComplexVector))
            return super.add(alpha, y);

        checkSize(y);

        if (alpha[0] == 0 && alpha[1] == 0)
            return this;

        double[] yd = ((DenseComplexVector) y).getData();

        for (int i = 0; i < size; i++) {
            double re = alpha[0] * yd[i] - alpha[1] * yd[i + size];
            double im = alpha[0] * yd[i + size] + alpha[1] * yd[i];
            data[i] += re;
            data[i + size] += im;
        }

        return this;
    }

    @Override
    public double[] dot(ComplexVector y) {
        if (!(y instanceof DenseComplexVector))
            return super.dot(y);

        checkSize(y);

        double[] yd = ((DenseComplexVector) y).getData();

        double[] dot = new double[] { 0, 0 };
        for (int i = 0; i < size; ++i) {
            dot[0] += data[i] * yd[i] + data[i + size] * yd[i + size];
            dot[1] += -data[i + size] * yd[i] + data[i] * yd[i + size];
        }
        return dot;
    }

    @Override
    protected double norm1() {
        double sum = 0;
        for (int i = 0; i < size; ++i)
            sum += Math.sqrt(data[i] * data[i] + data[i + size] * data[i + size]);
        return sum;
    }

    @Override
    protected double norm2() {
        double norm = 0;
        for (int i = 0; i < size; ++i)
            norm += data[i] * data[i] + data[i + size] * data[i + size];
        return Math.sqrt(norm);
    }

    @Override
    protected double norm2_robust() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected double normInf() {
        double max = 0;
        for (int i = 0; i < size; ++i)
            max = Math.max(Math.sqrt(data[i] * data[i] + data[i + size] * data[i + size]), max);
        return max;
    }

    /**
     * Returns the internal vector contents. The array indices correspond to the
     * vector indices
     */
    public double[] getData() {
        return data;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < size; i++) {
            sb.append(String.format("%e", get(i)[0]));
            double imag = get(i)[1];
            if (i != size - 1)
                sb.append(String.format("%+ej;", imag));
            else
                sb.append(String.format("%+ej", imag));
        }
        sb.append("]");
        return sb.toString().trim();
    }
}
