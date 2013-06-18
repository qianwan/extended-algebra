package com.yahoo.algebra.matrix;

import com.yahoo.networkmimo.exception.ComplexMatrixNotSPDException;

import no.uib.cipr.matrix.MatrixNotSPDException;
import no.uib.cipr.matrix.MatrixSingularException;

public interface ComplexMatrix extends Iterable<ComplexMatrixEntry> {

    /**
     * Number of rows in the matrix
     */
    int numRows();

    /**
     * Number of columns in the matrix
     */
    int numColumns();

    /**
     * Returns true if the matrix is square
     */
    boolean isSquare();

    /**
     * <code>A(row,column) = value</code>
     */
    void set(int row, int column, double value[]);

    /**
     * <code>A(row,column) += value</code>
     */
    void add(int row, int column, double value[]);

    /**
     * Returns <code>A(row,column)</code>
     */
    double[] get(int row, int column);

    /**
     * Creates a deep copy of the matrix
     * 
     * @return A
     */
    ComplexMatrix copy();

    /**
     * Zeros all the entries in the matrix, while preserving any underlying
     * structure. Useful for general, unstructured matrices.
     * 
     * @return A
     */
    ComplexMatrix zero();

    /**
     * <code>y = A*x</code>
     * 
     * @param x
     *            Vector of size <code>A.numColumns()</code>
     * @param y
     *            Vector of size <code>A.numRows()</code>
     * @return y
     */
    ComplexVector mult(ComplexVector x, ComplexVector y);

    /**
     * <code>y = alpha*A*x</code>
     * 
     * @param x
     *            Vector of size <code>A.numColumns()</code>
     * @param y
     *            Vector of size <code>A.numRows()</code>
     * @return y
     */
    ComplexVector mult(double alpha[], ComplexVector x, ComplexVector y);

    /**
     * <code>y = A*x + y</code>
     * 
     * @param x
     *            Vector of size <code>A.numColumns()</code>
     * @param y
     *            Vector of size <code>A.numRows()</code>
     * @return y
     */
    ComplexVector multAdd(ComplexVector x, ComplexVector y);

    /**
     * <code>y = alpha*A*x + y</code>
     * 
     * @param x
     *            Vector of size <code>A.numColumns()</code>
     * @param y
     *            Vector of size <code>A.numRows()</code>
     * @return y
     */
    ComplexVector multAdd(double alpha[], ComplexVector x, ComplexVector y);

    /**
     * <code>y = A<sup>T</sup>*x</code>
     * 
     * @param x
     *            Vector of size <code>A.numRows()</code>
     * @param y
     *            Vector of size <code>A.numColumns()</code>
     * @return y
     */
    ComplexVector transMult(ComplexVector x, ComplexVector y);

    /**
     * <code>y = alpha*A<sup>T</sup>*x</code>
     * 
     * @param x
     *            Vector of size <code>A.numRows()</code>
     * @param y
     *            Vector of size <code>A.numColumns()</code>
     * @return y
     */
    ComplexVector transMult(double alpha[], ComplexVector x, ComplexVector y);

    /**
     * <code>y = A<sup>T</sup>*x + y</code>
     * 
     * @param x
     *            Vector of size <code>A.numRows()</code>
     * @param y
     *            Vector of size <code>A.numColumns()</code>
     * @return y
     */
    ComplexVector transMultAdd(ComplexVector x, ComplexVector y);

    /**
     * <code>y = alpha*A<sup>T</sup>*x + y</code>
     * 
     * @param x
     *            Vector of size <code>A.numRows()</code>
     * @param y
     *            Vector of size <code>A.numColumns()</code>
     * @return y
     */
    ComplexVector transMultAdd(double alpha[], ComplexVector x, ComplexVector y);

    /**
     * <code>x = A\b</code>. Not all matrices support this operation, those that
     * do not throw <code>UnsupportedOperationException</code>. Note that it is
     * often more efficient to use a matrix decomposition and its associated
     * solver
     * 
     * @param b
     *            Vector of size <code>A.numRows()</code>
     * @param x
     *            Vector of size <code>A.numColumns()</code>
     * @return x
     * @throws MatrixSingularException
     *             If the matrix is singular
     * @throws MatrixNotSPDException
     *             If the solver assumes that the matrix is symmetrical,
     *             positive definite, but that that property does not hold
     */
    ComplexVector solve(ComplexVector b, ComplexVector x) throws MatrixSingularException,
            MatrixNotSPDException;

    /**
     * <code>x = A<sup>T</sup>\b</code>. Not all matrices support this
     * operation, those that do not throw
     * <code>UnsupportedOperationException</code>. Note that it is often more
     * efficient to use a matrix decomposition and its associated solver
     * 
     * @param b
     *            Vector of size <code>A.numColumns()</code>
     * @param x
     *            Vector of size <code>A.numRows()</code>
     * @return x
     * @throws MatrixSingularException
     *             If the matrix is singular
     * @throws MatrixNotSPDException
     *             If the solver assumes that the matrix is symmetrical,
     *             positive definite, but that that property does not hold
     */
    ComplexVector transSolve(ComplexVector b, ComplexVector x) throws MatrixSingularException,
            MatrixNotSPDException;

    /**
     * <code>A = x*x<sup>T</sup> + A</code>. The matrix must be square, and the
     * vector of the same length
     * 
     * @return A
     */
    ComplexMatrix rank1(ComplexVector x);

    /**
     * <code>A = alpha*x*x<sup>T</sup> + A</code>. The matrix must be square,
     * and the vector of the same length
     * 
     * @return A
     */
    ComplexMatrix rank1(double alpha[], ComplexVector x);

    /**
     * <code>A = x*y<sup>T</sup> + A</code>. The matrix must be square, and the
     * vectors of the same length
     * 
     * @return A
     */
    ComplexMatrix rank1(ComplexVector x, ComplexVector y);

    /**
     * <code>A = alpha*x*y<sup>T</sup> + A</code>. The matrix must be square,
     * and the vectors of the same length
     * 
     * @return A
     */
    ComplexMatrix rank1(double alpha[], ComplexVector x, ComplexVector y);

    /**
     * <code>A = x*y<sup>T</sup> + y*x<sup>T</sup> + A</code>. The matrix must
     * be square, and the vectors of the same length
     * 
     * @return A
     */
    ComplexMatrix rank2(ComplexVector x, ComplexVector y);

    /**
     * <code>A = alpha*x*y<sup>T</sup> + alpha*y*x<sup>T</sup> + A</code>. The
     * matrix must be square, and the vectors of the same length
     * 
     * @return A
     */
    ComplexMatrix rank2(double alpha[], ComplexVector x, ComplexVector y);

    /**
     * chained multiplying
     */
    ComplexMatrix mult(ComplexMatrix B);

    /**
     * <code>C = A*B</code>
     * 
     * @param B
     *            Matrix such that <code>B.numRows() == A.numColumns()</code>
     *            and <code>B.numColumns() == C.numColumns()</code>
     * @param C
     *            Matrix such that <code>C.numRows() == A.numRows()</code> and
     *            <code>B.numColumns() == C.numColumns()</code>
     * @return C
     */
    ComplexMatrix mult(ComplexMatrix B, ComplexMatrix C);

    /**
     * <code>C = alpha*A*B</code>
     * 
     * @param B
     *            Matrix such that <code>B.numRows() == A.numColumns()</code>
     *            and <code>B.numColumns() == C.numColumns()</code>
     * @param C
     *            Matrix such that <code>C.numRows() == A.numRows()</code> and
     *            <code>B.numColumns() == C.numColumns()</code>
     * @return C
     */
    ComplexMatrix mult(double alpha[], ComplexMatrix B, ComplexMatrix C);

    /**
     * <code>C = A*B + C</code>
     * 
     * @param B
     *            Matrix such that <code>B.numRows() == A.numColumns()</code>
     *            and <code>B.numColumns() == C.numColumns()</code>
     * @param C
     *            Matrix such that <code>C.numRows() == A.numRows()</code> and
     *            <code>B.numColumns() == C.numColumns()</code>
     * @return C
     */
    ComplexMatrix multAdd(ComplexMatrix B, ComplexMatrix C);

    /**
     * <code>C = alpha*A*B + C</code>
     * 
     * @param B
     *            Matrix such that <code>B.numRows() == A.numColumns()</code>
     *            and <code>B.numColumns() == C.numColumns()</code>
     * @param C
     *            Matrix such that <code>C.numRows() == A.numRows()</code> and
     *            <code>B.numColumns() == C.numColumns()</code>
     * @return C
     */
    ComplexMatrix multAdd(double alpha[], ComplexMatrix B, ComplexMatrix C);

    /**
     * <code>C = A<sup>T</sup>*B</code>
     * 
     * @param B
     *            Matrix such that <code>B.numRows() == A.numRows()</code> and
     *            <code>B.numColumns() == C.numColumns()</code>
     * @param C
     *            Matrix such that <code>C.numRows() == A.numColumns()</code>
     *            and <code>B.numColumns() == C.numColumns()</code>
     * @return C
     */
    ComplexMatrix transAmult(ComplexMatrix B, ComplexMatrix C);

    /**
     * <code>C = alpha*A<sup>T</sup>*B</code>
     * 
     * @param B
     *            Matrix such that <code>B.numRows() == A.numRows()</code> and
     *            <code>B.numColumns() == C.numColumns()</code>
     * @param C
     *            Matrix such that <code>C.numRows() == A.numColumns()</code>
     *            and <code>B.numColumns() == C.numColumns()</code>
     * @return C
     */
    ComplexMatrix transAmult(double alpha[], ComplexMatrix B, ComplexMatrix C);

    /**
     * <code>C = A<sup>T</sup>*B + C</code>
     * 
     * @param B
     *            Matrix such that <code>B.numRows() == A.numRows()</code> and
     *            <code>B.numColumns() == C.numColumns()</code>
     * @param C
     *            Matrix such that <code>C.numRows() == A.numColumns()</code>
     *            and <code>B.numColumns() == C.numColumns()</code>
     * @return C
     */
    ComplexMatrix transAmultAdd(ComplexMatrix B, ComplexMatrix C);

    /**
     * <code>C = alpha*A<sup>T</sup>*B + C</code>
     * 
     * @param B
     *            Matrix such that <code>B.numRows() == A.numRows()</code> and
     *            <code>B.numColumns() == C.numColumns()</code>
     * @param C
     *            Matrix such that <code>C.numRows() == A.numColumns()</code>
     *            and <code>B.numColumns() == C.numColumns()</code>
     * @return C
     */
    ComplexMatrix transAmultAdd(double alpha[], ComplexMatrix B, ComplexMatrix C);

    /**
     * <code>C = A*B<sup>T</sup></code>
     * 
     * @param B
     *            Matrix such that <code>B.numRows() == A.numRows()</code> and
     *            <code>B.numColumns() == C.numColumns()</code>
     * @param C
     *            Matrix such that <code>C.numRows() == A.numColumns()</code>
     *            and <code>B.numColumns() == C.numColumns()</code>
     * @return C
     */
    ComplexMatrix transBmult(ComplexMatrix B, ComplexMatrix C);

    /**
     * <code>C = alpha*A*B<sup>T</sup></code>
     * 
     * @param B
     *            Matrix such that <code>B.numRows() == A.numRows()</code> and
     *            <code>B.numColumns() == C.numColumns()</code>
     * @param C
     *            Matrix such that <code>C.numRows() == A.numColumns()</code>
     *            and <code>B.numColumns() == C.numColumns()</code>
     * @return C
     */
    ComplexMatrix transBmult(double alpha[], ComplexMatrix B, ComplexMatrix C);

    /**
     * <code>C = A*B<sup>T</sup> + C</code>
     * 
     * @param B
     *            Matrix such that <code>B.numRows() == A.numRows()</code> and
     *            <code>B.numColumns() == C.numColumns()</code>
     * @param C
     *            Matrix such that <code>C.numRows() == A.numColumns()</code>
     *            and <code>B.numColumns() == C.numColumns()</code>
     * @return C
     */
    ComplexMatrix transBmultAdd(ComplexMatrix B, ComplexMatrix C);

    /**
     * <code>C = alpha*A*B<sup>T</sup> + C</code>
     * 
     * @param B
     *            Matrix such that <code>B.numRows() == A.numRows()</code> and
     *            <code>B.numColumns() == C.numColumns()</code>
     * @param C
     *            Matrix such that <code>C.numRows() == A.numColumns()</code>
     *            and <code>B.numColumns() == C.numColumns()</code>
     * @return C
     */
    ComplexMatrix transBmultAdd(double alpha[], ComplexMatrix B, ComplexMatrix C);

    /**
     * <code>C = A<sup>T</sup>*B<sup>T</sup></code>
     * 
     * @param B
     *            Matrix such that <code>B.numColumns() == A.numRows()</code>
     *            and <code>B.numRows() == C.numColumns()</code>
     * @param C
     *            Matrix such that <code>C.numRows() == A.numColumns()</code>
     *            and <code>B.numRows() == C.numColumns()</code>
     * @return C
     */
    ComplexMatrix transABmult(ComplexMatrix B, ComplexMatrix C);

    /**
     * <code>C = alpha*A<sup>T</sup>*B<sup>T</sup></code>
     * 
     * @param B
     *            Matrix such that <code>B.numColumns() == A.numRows()</code>
     *            and <code>B.numRows() == C.numColumns()</code>
     * @param C
     *            Matrix such that <code>C.numRows() == A.numColumns()</code>
     *            and <code>B.numRows() == C.numColumns()</code>
     * @return C
     */
    ComplexMatrix transABmult(double alpha[], ComplexMatrix B, ComplexMatrix C);

    /**
     * <code>C = A<sup>T</sup>*B<sup>T</sup> + C</code>
     * 
     * @param B
     *            Matrix such that <code>B.numColumns() == A.numRows()</code>
     *            and <code>B.numRows() == C.numColumns()</code>
     * @param C
     *            Matrix such that <code>C.numRows() == A.numColumns()</code>
     *            and <code>B.numRows() == C.numColumns()</code>
     * @return C
     */
    ComplexMatrix transABmultAdd(ComplexMatrix B, ComplexMatrix C);

    /**
     * <code>C = alpha*A<sup>T</sup>*B<sup>T</sup> + C</code>
     * 
     * @param B
     *            Matrix such that <code>B.numColumns() == A.numRows()</code>
     *            and <code>B.numRows() == C.numColumns()</code>
     * @param C
     *            Matrix such that <code>C.numRows() == A.numColumns()</code>
     *            and <code>B.numRows() == C.numColumns()</code>
     * @return C
     */
    ComplexMatrix transABmultAdd(double alpha[], ComplexMatrix B, ComplexMatrix C);

    /**
     * <code>X = A\B</code>. Not all matrices support this operation, those that
     * do not throw <code>UnsupportedOperationException</code>. Note that it is
     * often more efficient to use a matrix decomposition and its associated
     * solver
     * 
     * @param B
     *            Matrix with the same number of rows as <code>A</code>, and the
     *            same number of columns as <code>X</code>
     * @param X
     *            Matrix with a number of rows equal <code>A.numColumns()</code>
     *            , and the same number of columns as <code>B</code>
     * @return X
     * @throws MatrixSingularException
     *             If the matrix is singular
     * @throws MatrixNotSPDException
     *             If the solver assumes that the matrix is symmetrical,
     *             positive definite, but that that property does not hold
     */
    ComplexMatrix solve(ComplexMatrix B, ComplexMatrix X) throws MatrixSingularException,
            MatrixNotSPDException;

    /**
     * <code>X = A<sup>T</sup>\B</code>. Not all matrices support this
     * operation, those that do not throw
     * <code>UnsupportedOperationException</code>. Note that it is often more
     * efficient to use a matrix decomposition and its associated transpose
     * solver
     * 
     * @param B
     *            Matrix with a number of rows equal <code>A.numColumns()</code>
     *            , and the same number of columns as <code>X</code>
     * @param X
     *            Matrix with the same number of rows as <code>A</code>, and the
     *            same number of columns as <code>B</code>
     * @return X
     * @throws MatrixSingularException
     *             If the matrix is singular
     * @throws MatrixNotSPDException
     *             If the solver assumes that the matrix is symmetrical,
     *             positive definite, but that that property does not hold
     */
    ComplexMatrix transSolve(ComplexMatrix B, ComplexMatrix X) throws MatrixSingularException,
            MatrixNotSPDException;

    /**
     * <code>A = C*C<sup>T</sup> + A</code>. The matrices must be square and of
     * the same size
     * 
     * @return A
     */
    ComplexMatrix rank1(ComplexMatrix C);

    /**
     * <code>A = alpha*C*C<sup>T</sup> + A</code>. The matrices must be square
     * and of the same size
     * 
     * @return A
     */
    ComplexMatrix rank1(double alpha[], ComplexMatrix C);

    /**
     * <code>A = C<sup>T</sup>*C + A</code> The matrices must be square and of
     * the same size
     * 
     * @return A
     */
    ComplexMatrix transRank1(ComplexMatrix C);

    /**
     * <code>A = alpha*C<sup>T</sup>*C + A</code> The matrices must be square
     * and of the same size
     * 
     * @return A
     */
    ComplexMatrix transRank1(double alpha[], ComplexMatrix C);

    /**
     * <code>A = B*C<sup>T</sup> + C*B<sup>T</sup> + A</code>. This matrix must
     * be square
     * 
     * @param B
     *            Matrix with the same number of rows as <code>A</code> and the
     *            same number of columns as <code>C</code>
     * @param C
     *            Matrix with the same number of rows as <code>A</code> and the
     *            same number of columns as <code>B</code>
     * @return A
     */
    ComplexMatrix rank2(ComplexMatrix B, ComplexMatrix C);

    /**
     * <code>A = alpha*B*C<sup>T</sup> + alpha*C*B<sup>T</sup> + A</code>. This
     * matrix must be square
     * 
     * @param B
     *            Matrix with the same number of rows as <code>A</code> and the
     *            same number of columns as <code>C</code>
     * @param C
     *            Matrix with the same number of rows as <code>A</code> and the
     *            same number of columns as <code>B</code>
     * @return A
     */
    ComplexMatrix rank2(double alpha[], ComplexMatrix B, ComplexMatrix C);

    /**
     * <code>A = B<sup>T</sup>*C + C<sup>T</sup>*B + A</code>. This matrix must
     * be square
     * 
     * @param B
     *            Matrix with the same number of rows as <code>C</code> and the
     *            same number of columns as <code>A</code>
     * @param C
     *            Matrix with the same number of rows as <code>B</code> and the
     *            same number of columns as <code>A</code>
     * @return A
     */
    ComplexMatrix transRank2(ComplexMatrix B, ComplexMatrix C);

    /**
     * <code>A = alpha*B<sup>T</sup>*C + alpha*C<sup>T</sup>*B + A</code>. This
     * matrix must be square
     * 
     * @param B
     *            Matrix with the same number of rows as <code>C</code> and the
     *            same number of columns as <code>A</code>
     * @param C
     *            Matrix with the same number of rows as <code>B</code> and the
     *            same number of columns as <code>A</code>
     * @return A
     */
    ComplexMatrix transRank2(double alpha[], ComplexMatrix B, ComplexMatrix C);

    /**
     * <code>A = alpha*A</code>
     * 
     * @return A
     */
    ComplexMatrix scale(double alpha[]);

    /**
     * <code>A=B</code>. The matrices must be of the same size
     * 
     * @return A
     */
    ComplexMatrix set(ComplexMatrix B);

    /**
     * <code>A=alpha*B</code>. The matrices must be of the same size
     * 
     * @return A
     */
    ComplexMatrix set(double alpha[], ComplexMatrix B);

    /**
     * <code>A = B + A</code>. The matrices must be of the same size
     * 
     * @return A
     */
    ComplexMatrix add(ComplexMatrix B);

    /**
     * <code>A = alpha*B + A</code>. The matrices must be of the same size
     * 
     * @return A
     */
    ComplexMatrix add(double alpha[], ComplexMatrix B);

    /**
     * Transposes the matrix in-place. In most cases, the matrix must be square
     * for this to work.
     * 
     * @return This matrix
     */
    ComplexMatrix transpose();

    /**
     * Sets the transpose of this matrix into <code>B</code>. Matrix dimensions
     * must be compatible
     * 
     * @param B
     *            Matrix with as many rows as this matrix has columns, and as
     *            many columns as this matrix has rows
     * @return The matrix <code>B=A<sup>T</sup></code>
     */
    ComplexMatrix transpose(ComplexMatrix B);

    /**
     * Hermitianly transposes the matrix in-place. In most cases, the matrix
     * must be square for this to work.
     * 
     * @throws ComplexMatrixNotSPDException
     */
    ComplexMatrix hermitianTranspose();

    /**
     * Sets the Hermitian transpose of this matrix into <code>B</code>.
     * 
     * @throws ComplexMatrixNotSPDException
     */
    ComplexMatrix hermitianTranspose(ComplexMatrix B);

    ComplexMatrix inverse() throws ComplexMatrixNotSPDException;

    /**
     * Computes the given norm of the matrix
     * 
     * @param type
     *            The type of norm to compute
     */
    double norm(Norm type);

    /**
     * Supported matrix-norms. Note that <code>Maxvalue</code> is not a proper
     * matrix norm
     */
    enum Norm {

        /**
         * Maximum absolute row sum
         */
        One,

        /**
         * The root of sum of the sum of squares
         */
        Frobenius,

        /**
         * Maximum column sum
         */
        Infinity,

        /**
         * Largest entry in absolute value. Not a proper matrix norm
         */
        Maxvalue;

        /**
         * @return the String as required by the netlib libraries to represent
         *         this norm.
         */
        public String netlib() {
            // TODO: this is a bit of a hack
            // shouldn't need to know about the internals of netlib
            if (this == One)
                return "1";
            else if (this == Infinity)
                return "I";
            else
                throw new IllegalArgumentException("Norm must be the 1 or the Infinity norm");
        }

    }

    /**
     * 
     * @return trace of the matrix
     * @throws ComplexMatrixNotSPDException
     */
    double[] trace() throws ComplexMatrixNotSPDException;

    /**
     * 
     * @return determinant squared of the complex matrix
     */
    double det2();
}
