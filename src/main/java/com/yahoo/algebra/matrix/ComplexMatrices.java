package com.yahoo.algebra.matrix;

import org.uncommons.maths.random.GaussianGenerator;
import org.uncommons.maths.random.MersenneTwisterRNG;

import com.yahoo.networkmimo.exception.ComplexMatrixNotSPDException;

public final class ComplexMatrices {
    private static final GaussianGenerator rng;

    static {
        // byte[] seed = new byte[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
        // 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
        rng = new GaussianGenerator(0, 1, new MersenneTwisterRNG());
    }

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

    /**
     * Generate a random complex matrix
     */
    public static ComplexMatrix random(ComplexMatrix A) {
        for (int i = 0; i < A.numRows(); i++) {
            for (int j = 0; j < A.numColumns(); j++) {
                A.set(i, j, new double[] { rng.nextValue(), rng.nextValue() });
            }
        }
        return A;
    }

    /**
     * Return the power of a matrix
     * @throws ComplexMatrixNotSPDException 
     */
    static public double getPower(ComplexMatrix A) throws ComplexMatrixNotSPDException {
        ComplexMatrix B = A.hermitianTranspose(new DenseComplexMatrix(A.numColumns(), A.numRows()));

        ComplexMatrix C = B.mult(A, new DenseComplexMatrix(B.numRows(), A.numColumns()));

        return C.trace()[0];
    }

    /**
     * @throws ComplexMatrixNotSPDException 
     * 
     */
    public static ComplexMatrix setPower(ComplexMatrix A, double power) throws ComplexMatrixNotSPDException {
        double oldPower = getPower(A);

        double[] alpha = new double[] { Math.sqrt(power / oldPower), 0 };
        for (int i = 0; i < A.numRows(); i++) {
            for (int j = 0; j < A.numColumns(); j++) {
                A.set(i, j, Complexes.mult(A.get(i, j), alpha));
            }
        }
        return A;
    }
}
