package com.yahoo.algebra.matrix;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.EVD;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.NotConvergedException;

import org.jblas.NativeBlas;
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
     * 
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
    public static ComplexMatrix setPower(ComplexMatrix A, double power)
            throws ComplexMatrixNotSPDException {
        double oldPower = getPower(A);

        double[] alpha = new double[] { Math.sqrt(power / oldPower), 0 };
        for (int i = 0; i < A.numRows(); i++) {
            for (int j = 0; j < A.numColumns(); j++) {
                A.set(i, j, Complexes.mult(A.get(i, j), alpha));
            }
        }
        return A;
    }

    /**
     * @throws NotConvergedException
     * 
     */
    public static double spectralRadius(ComplexMatrix A) throws NotConvergedException {
        if (!A.isSquare()) {
            throw new ComplexMatrixNotSPDException("eigenvalue decomposition is for squre matrix");
        }
        Matrix B = new DenseMatrix(A.numRows() * 2, A.numColumns() * 2);
        for (int i = 0; i < A.numRows(); i++) {
            for (int j = 0; j < A.numColumns(); j++) {
                B.set(i, j, A.get(i, j)[0]);
                B.set(i, j + A.numColumns(), -A.get(i, j)[1]);
                B.set(i + A.numRows(), j, A.get(i, j)[1]);
                B.set(i + A.numRows(), j + A.numColumns(), A.get(i, j)[0]);
            }
        }
        EVD evd = new EVD(B.numRows());
        evd.factor((DenseMatrix) B);
        double rho = 0.0;
        double[] Wr = evd.getRealEigenvalues();
        double[] Wi = evd.getImaginaryEigenvalues();
        for (int i = 0; i < Wr.length; i++) {
            double tmp = Math.sqrt(Wr[i] * Wr[i] + Wi[i] * Wi[i]);
            if (rho < tmp)
                rho = tmp;
        }
        return rho;
    }

    /**
     * <code>A=V*Lambda*V<sup>H</sup></code>
     * 
     * @param A
     *            target matrix
     * @param V
     *            eigen vectors
     * @param lambda
     *            eigenvalues
     * @throws NotConvergedException
     */
    public static void eig(ComplexMatrix A, ComplexMatrix V, ComplexVector lambda)
            throws NotConvergedException {
        if (!A.isSquare()) {
            throw new ComplexMatrixNotSPDException("eigenvalue decomposition is for squre matrix");
        }
        if (A.numColumns()==1 && A.numRows()==1) {
            V.set(0, 0, new double[]{1, 0});
            lambda.set(0, A.get(0, 0));
            return;
        }
        double[] data = new double[A.numRows() * A.numColumns() * 2];
        for (int i = 0; i < A.numRows(); i++) {
            for (int j = 0; j < A.numColumns(); j++) {
                int offset = (j * V.numColumns() + i) * 2;
                data[offset] = A.get(i, j)[0];
                data[offset + 1] = A.get(i, j)[1];
            }
        }
        double[] w = new double[A.numRows() * 2];
        double[] vl = new double[data.length];
        double[] vr = new double[data.length];
        double[] work = new double[data.length];
        int row = A.numRows();
        int info = NativeBlas.zgeev('N', 'V', row, data, 0, row, w, 0, vl, 0, row, vr, 0, row,
                work, 0);
        if (info > 0)
            throw new ComplexMatrixNotSPDException("Eigenvalues have not converged.");
        for (int i = 0; i < lambda.size(); i++) {
            lambda.set(i, new double[] { w[i * 2], w[i * 2 + 1] });
        }
        for (int i = 0; i < V.numRows(); i++) {
            for (int j = 0; j < V.numColumns(); j++) {
                int offset = (j * V.numColumns() + i) * 2;
                V.set(i, j, new double[] { vr[offset], vr[offset + 1] });
            }
        }
    }

    public static ComplexMatrix diag(ComplexVector v) {
        ComplexMatrix A = new DenseComplexMatrix(v.size(), v.size());
        A.zero();
        for (int i = 0; i < v.size(); i++)
            A.set(i, i, v.get(i));
        return A;
    }
}
