package com.yahoo.algebra.matrix;

public interface ComplexVectorEntry {
    /**
     * Returns the current index
     */
    int index();

    /**
     * Returns the value at the current index
     */
    double[] get();

    /**
     * Sets the value at the current index
     */
    void set(double value[]);

}
