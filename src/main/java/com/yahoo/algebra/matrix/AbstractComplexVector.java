package com.yahoo.algebra.matrix;

import java.io.Serializable;
import java.util.Formatter;
import java.util.Iterator;

@SuppressWarnings("serial")
public abstract class AbstractComplexVector implements ComplexVector, Serializable {
    /**
     * Size of the ComplexVector
     */
    protected int size;

    /**
     * Constructor for AbstractComplexComplexVector.
     * 
     * @param size
     *            Size of the ComplexVector
     */
    protected AbstractComplexVector(int size) {
        if (size < 0)
            throw new IllegalArgumentException("ComplexVector size cannot be negative");
        this.size = size;
    }

    /**
     * Constructor for AbstractComplexComplexVector, same size as x
     * 
     * @param x
     *            ComplexVector to get the size from
     */
    protected AbstractComplexVector(ComplexVector x) {
        this.size = x.size();
    }

    public int size() {
        return size;
    }

    public void set(int index, double value[]) {
        throw new UnsupportedOperationException();
    }

    public void add(int index, double value[]) {
        double[] adder = get(index);
        set(index, new double[] { value[0] + adder[0], value[1] + adder[1] });
    }

    public double[] get(int index) {
        throw new UnsupportedOperationException();
    }

    public ComplexVector copy() {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks the index
     */
    protected void check(int index) {
        if (index < 0)
            throw new IndexOutOfBoundsException("index is negative (" + index + ")");
        if (index >= size)
            throw new IndexOutOfBoundsException("index >= size (" + index + " >= " + size + ")");
    }

    public ComplexVector zero() {
        for (ComplexVectorEntry e : this)
            e.set(new double[] { 0, 0 });
        return this;
    }

    public ComplexVector scale(double alpha[]) {
        if (alpha[0] == 0 && alpha[1] == 0)
            return zero();
        else if (alpha[0] == 1 && alpha[0] == 0)
            return this;

        for (ComplexVectorEntry e : this)
            e.set(Complexes.mult(e.get(), alpha));

        return this;
    }

    public ComplexVector set(ComplexVector y) {
        return set(new double[] { 1, 0 }, y);
    }

    public ComplexVector set(double alpha[], ComplexVector y) {
        checkSize(y);

        if (alpha[0] == 0 && alpha[1] == 0)
            return zero();

        zero();
        for (ComplexVectorEntry e : y)
            set(e.index(), Complexes.mult(alpha, e.get()));

        return this;
    }

    public ComplexVector add(ComplexVector y) {
        return add(new double[] { 1, 0 }, y);
    }

    public ComplexVector add(double alpha[], ComplexVector y) {
        checkSize(y);

        if (alpha[0] == 0 && alpha[1] == 0)
            return this;

        for (ComplexVectorEntry e : y)
            add(e.index(), Complexes.mult(alpha, e.get()));

        return this;
    }

    public double[] dot(ComplexVector y) {
        checkSize(y);

        double[] ret = new double[] { 0, 0 };
        for (ComplexVectorEntry e : this) {
            ret = Complexes
                    .add(ret, Complexes.mult(e.get(), Complexes.conjugate(y.get(e.index()))));
        }
        return ret;
    }

    /**
     * Checks for conformant sizes
     */
    protected void checkSize(ComplexVector y) {
        if (size != y.size())
            throw new IndexOutOfBoundsException("x.size != y.size (" + size + " != " + y.size()
                    + ")");
    }

    public double norm(Norm type) {
        if (type == Norm.One)
            return norm1();
        else if (type == Norm.Two)
            return norm2();
        else if (type == Norm.TwoRobust)
            return norm2_robust();
        else
            // Infinity
            return normInf();
    }

    protected double norm1() {
        double sum = 0;
        for (ComplexVectorEntry e : this)
            sum += Complexes.abs(e.get());
        return sum;
    }

    protected double norm2() {
        double norm = 0;
        for (ComplexVectorEntry e : this)
            norm += Complexes.abs2(e.get());
        return Math.sqrt(norm);
    }

    protected double norm2_robust() {
        double scale = 0, ssq = 1;
        for (ComplexVectorEntry e : this) {
            double xval[] = e.get();
            if (xval[0] != 0 && xval[1] != 0) {
                double absxi = Complexes.abs(xval);
                if (scale < absxi) {
                    ssq = 1 + ssq * Math.pow(scale / absxi, 2);
                    scale = absxi;
                } else
                    ssq = ssq + Math.pow(absxi / scale, 2);
            }
        }
        return scale * Math.sqrt(ssq);
    }

    protected double normInf() {
        double max = 0;
        for (ComplexVectorEntry e : this)
            max = Math.max(Complexes.abs(e.get()), max);
        return max;
    }

    public Iterator<ComplexVectorEntry> iterator() {
        return new RefComplexVectorIterator();
    }

    @Override
    public String toString() {
        // Output into coordinate format. Indices start from 1 instead of 0
        @SuppressWarnings("resource")
        Formatter out = new Formatter();

        out.format("%10d %19d\n", size, ComplexMatrices.cardinality(this));

        for (ComplexVectorEntry e : this)
            if (e.get()[0] != 0 && e.get()[1] != 0)
                out.format("%10d % .12e\n", e.index() + 1, e.get());

        return out.toString();
    }

    /**
     * Iterator over a general ComplexVector
     */
    private class RefComplexVectorIterator implements Iterator<ComplexVectorEntry> {

        private int index;

        private final RefComplexVectorEntry entry = new RefComplexVectorEntry();

        public boolean hasNext() {
            return index < size;
        }

        public ComplexVectorEntry next() {
            entry.update(index);

            index++;

            return entry;
        }

        public void remove() {
            entry.set(new double[] { 0, 0 });
        }

    }

    /**
     * ComplexVector entry backed by the ComplexVector. May be reused for higher
     * performance
     */
    private class RefComplexVectorEntry implements ComplexVectorEntry {

        private int index;

        /**
         * Updates the entry
         */
        public void update(int index) {
            this.index = index;
        }

        public int index() {
            return index;
        }

        public double[] get() {
            return AbstractComplexVector.this.get(index);
        }

        public void set(double value[]) {
            AbstractComplexVector.this.set(index, value);
        }

    }
}
