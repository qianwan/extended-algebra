package com.yahoo.algebra.matrix;

import org.netlib.lapack.LAPACK;
import org.netlib.util.intW;

import com.yahoo.networkmimo.exception.ComplexMatrixNotSPDException;

import no.uib.cipr.matrix.DenseLU;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.MatrixEntry;
import no.uib.cipr.matrix.MatrixNotSPDException;
import no.uib.cipr.matrix.UpperTriangDenseMatrix;

public class DenseComplexMatrix extends AbstractDenseComplexMatrix {

    /**
     * Constructor for DenseComplexMatrix
     * 
     * @param numRows
     *            Number of rows
     * @param numColumns
     *            Number of columns
     */
    public DenseComplexMatrix(int numRows, int numColumns) {
        super(numRows, numColumns);
    }

    /**
     * Constructor for DenseComplexMatrix
     * 
     * @param A
     *            ComplexMatrix to copy. A deep copy is made
     */
    public DenseComplexMatrix(ComplexMatrix A) {
        super(A);
    }

    /**
     * Constructor for DenseComplexMatrix
     * 
     * @param A
     *            ComplexMatrix to copy contents from
     * @param deep
     *            If true, <code>A</code> is copied, else a shallow copy is made
     *            and the ComplexMatrices share underlying storage. For this,
     *            <code>A</code> must be a dense ComplexMatrix
     */
    public DenseComplexMatrix(ComplexMatrix A, boolean deep) {
        super(A, deep);
    }

    /**
     * Constructor for DenseComplexMatrix. Builds the ComplexMatrix from a
     * ComplexVector
     * 
     * @param x
     *            ComplexVector to copy from. This will form this ComplexMatrix'
     *            single column
     * @param deep
     *            If true, x is copied, if false, the internal storage of this
     *            ComplexMatrix is the same as that of the ComplexVector. In
     *            that case, <code>x</code> must be a
     *            <code>DenseComplexVector</code>
     */
    public DenseComplexMatrix(ComplexVector x, boolean deep) {
        super(x.size(), 1);

        if (deep)
            for (ComplexVectorEntry e : x)
                set(e.index(), 0, e.get());
        else {
            if (!(x instanceof DenseComplexVector))
                throw new IllegalArgumentException("x must be a DenseComplexVector");
            data = ((DenseComplexVector) x).getData();
        }
    }

    /**
     * Constructor for DenseComplexMatrix. Builds the ComplexMatrix from a
     * ComplexVector
     * 
     * @param x
     *            The ComplexVector which forms this ComplexMatrix' single
     *            column. It is copied, not referenced
     */
    public DenseComplexMatrix(ComplexVector x) {
        this(x, true);
    }

    /**
     * Constructor for DenseComplexMatrix. Builds the ComplexMatrix from
     * ComplexVectors. Each ComplexVector will correspond to a column of the
     * ComplexMatrix
     * 
     * @param x
     *            ComplexVectors which forms the columns of this ComplexMatrix.
     *            Every ComplexVector must have the same size
     */
    public DenseComplexMatrix(ComplexVector[] x) {
        super(x[0].size(), x.length);

        // Ensure correct sizes
        for (ComplexVector v : x)
            if (v.size() != numRows)
                throw new IllegalArgumentException("All ComplexVectors must be of the same size");

        // Copy the contents
        for (int j = 0; j < x.length; ++j)
            for (ComplexVectorEntry e : x[j])
                set(e.index(), j, e.get());
    }

    /**
     * Constructor for DenseComplexMatrix. Copies from the passed array
     * 
     * @param values
     *            Arrays to copy from. Every sub-array must have the same size
     */
    public DenseComplexMatrix(double[][][] values) {
        super(values.length, values[0].length);

        // Copy the contents
        for (int i = 0; i < values.length; ++i) {
            if (values[i].length != numColumns)
                throw new IllegalArgumentException("Array cannot be jagged");
            for (int j = 0; j < values[i].length; ++j)
                set(i, j, values[i][j]);
        }
    }

    @Override
    public DenseComplexMatrix copy() {
        return new DenseComplexMatrix(this);
    }

    @Override
    void copy(ComplexMatrix A) {
        if (A instanceof DenseComplexMatrix) {
            double[] data_ = ((DenseComplexMatrix) A).getData();
            for (int i = 0; i < data.length; i++) {
                data[i] = data_[i];
            }
        } else {
            for (ComplexMatrixEntry e : A)
                set(e.row(), e.column(), e.get());
        }
    }

    /**
     * Inverse
     */
    @Override
    public ComplexMatrix inverse() throws ComplexMatrixNotSPDException {
        if (!isSquare()) {
            throw new ComplexMatrixNotSPDException();
        }

        DenseComplexMatrix A = copy();
        double[] aData = A.getData();

        intW info = new intW(0);
        int m = numRows() * 2;
        int[] piv = new int[m * 2];

        LAPACK.getInstance().dgetrf(m, m, aData, m, piv, info);

        if (info.val != 0)
            throw new ComplexMatrixNotSPDException("matrix is not valid");
        int lwork = m;
        double[] work = new double[m];

        LAPACK.getInstance().dgetri(m, aData, m, piv, work, lwork, info);

        return A;
    }

    /**
     * extended DenseMatrix
     */
    public DenseMatrix getDenseMatrix() {
        DenseMatrix A = new DenseMatrix(2 * numRows(), 2 * numColumns());
        for (MatrixEntry e : A) {
            int row = e.row();
            int col = e.column();
            A.set(row, col, data[row + col * 2 * numRows()]);
        }
        return A;
    }

    /**
     * sqr of determinant
     */
    public double det2() {
        if (!isSquare()) {
            throw new MatrixNotSPDException();
        }
        DenseMatrix A = getDenseMatrix();
        DenseLU denseLU = DenseLU.factorize(A);
        UpperTriangDenseMatrix U = denseLU.getU();
        double ret = 1.0;
        for (int i = 0; i < U.numRows(); i++) {
            ret *= U.get(i, i);
        }
        ret = Math.abs(ret);
        return ret;
    }

    /**
     * multplying chain
     */
    @Override
    public ComplexMatrix mult(ComplexMatrix B) {
        ComplexMatrix C = new DenseComplexMatrix(numRows(), B.numColumns());
        checkMultAdd(B, C);
        return mult(B, C);
    }

    @Override
    public ComplexMatrix hermitianTranspose() {
        ComplexMatrix B = new DenseComplexMatrix(numColumns(), numRows());
        hermitianTranspose(B);
        return B;
    }
}
