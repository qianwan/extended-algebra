package com.yahoo.algebra.matrix;

public final class ComplexMatrices {
    /**
     * <code>max(1, M)</code> provided as a convenience for 'leading dimension'
     * calculations.
     * 
     * @param n
     */
    static int ld(int n) {
        return Math.max(1, n);
    }

    /**
     * <code>max(1, max(M, N))</code> provided as a convenience for 'leading
     * dimension' calculations.
     * 
     * @param m
     * @param n
     */
    static int ld(int m, int n) {
        return Math.max(1, Math.max(m, n));
    }

    /**
     * Returns the number of non-zero entries in the given vector
     */
    public static int cardinality(ComplexVector x) {
        int nz = 0;
        for (ComplexVectorEntry e : x)
            if (e.get()[0] != 0 && e.get()[1] != 0)
                nz++;
        return nz;
    }

    /**
     * Returns the number of non-zero entries in the given matrix
     */
    public static int cardinality(ComplexMatrix A) {
        int nz = 0;
        for (ComplexMatrixEntry e : A)
            if (e.get()[0] != 0 || e.get()[1] != 0)
                nz++;
        return nz;
    }

    /**
     * Returns the I matrix
     */
    public static DenseComplexMatrix eye(int size) {
        DenseComplexMatrix e = new DenseComplexMatrix(size, size);
        e.zero();
        for (int i = 0; i < size; i++) {
            e.set(i, i, new double[] { 1, 0 });
        }
        return e;
    }
}
