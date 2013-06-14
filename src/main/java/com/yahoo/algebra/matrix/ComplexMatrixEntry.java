package com.yahoo.algebra.matrix;

/**
 * An entry of a complex matrix. Returned by the iterators over a complex matrix
 * structure
 */
public interface ComplexMatrixEntry {
    /**
     * Returns the current row index
     */
    int row();

    /**
     * Returns the current column index
     */
    int column();

    /**
     * Returns the value at the current index
     */
    double[] get();

    /**
     * Sets the value at the current index
     */
    void set(double[] value);
}
