package com.yahoo.algebra.matrix;

import org.netlib.lapack.LAPACK;
import org.netlib.util.intW;

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
    public DenseComplexMatrix inverse() {
        if (!isSquare()) {
            throw new MatrixNotSPDException();
        }

        DenseComplexMatrix A = copy();
        double[] aData = A.getData();

        intW info = new intW(0);
        int m = numRows() * 2;
        int[] piv = new int[m * 2];

        LAPACK.getInstance().dgetrf(m, m, aData, m, piv, info);

        if (info.val != 0)
            return null;
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

    // @Override
    // public ComplexMatrix multAdd(double alpha[], ComplexMatrix B,
    // ComplexMatrix C) {
    // if (!(B instanceof DenseComplexMatrix) || !(C instanceof
    // DenseComplexMatrix))
    // return super.multAdd(alpha, B, C);
    //
    // checkMultAdd(B, C);
    //
    // double[] Bd = ((DenseComplexMatrix) B).getData(), Cd =
    // ((DenseComplexMatrix) C)
    // .getData();
    //
    // BLAS.getInstance().dgemm(Transpose.NoTranspose.netlib(),
    // Transpose.NoTranspose.netlib(),
    // C.numRows(), C.numColumns(), numColumns, alpha, data,
    // Math.max(1, numRows), Bd, Math.max(1, B.numRows()), 1, Cd,
    // Math.max(1, C.numRows()));
    //
    // return C;
    // }

    // @Override
    // public ComplexMatrix transAmultAdd(double alpha[], ComplexMatrix B,
    // ComplexMatrix C) {
    // if (!(B instanceof DenseComplexMatrix) || !(C instanceof
    // DenseComplexMatrix))
    // return super.transAmultAdd(alpha, B, C);
    //
    // checkTransAmultAdd(B, C);
    //
    // double[] Bd = ((DenseComplexMatrix) B).getData(), Cd =
    // ((DenseComplexMatrix) C)
    // .getData();
    //
    // BLAS.getInstance().dgemm(Transpose.Transpose.netlib(),
    // Transpose.NoTranspose.netlib(),
    // C.numRows(), C.numColumns(), numRows, alpha, data,
    // Math.max(1, numRows), Bd, Math.max(1, B.numRows()), 1, Cd,
    // Math.max(1, C.numRows()));
    //
    // return C;
    // }

    // @Override
    // public ComplexMatrix transBmultAdd(double alpha[], ComplexMatrix B,
    // ComplexMatrix C) {
    // if (!(B instanceof DenseComplexMatrix) || !(C instanceof
    // DenseComplexMatrix))
    // return super.transBmultAdd(alpha, B, C);
    //
    // checkTransBmultAdd(B, C);
    //
    // double[] Bd = ((DenseComplexMatrix) B).getData(), Cd =
    // ((DenseComplexMatrix) C)
    // .getData();
    //
    // BLAS.getInstance().dgemm(Transpose.NoTranspose.netlib(),
    // Transpose.Transpose.netlib(),
    // C.numRows(), C.numColumns(), numColumns, alpha, data,
    // Math.max(1, numRows), Bd, Math.max(1, B.numRows()), 1, Cd,
    // Math.max(1, C.numRows()));
    //
    // return C;
    // }

    // @Override
    // public ComplexMatrix transABmultAdd(double alpha[], ComplexMatrix B,
    // ComplexMatrix C) {
    // if (!(B instanceof DenseComplexMatrix) || !(C instanceof
    // DenseComplexMatrix))
    // return super.transABmultAdd(alpha, B, C);
    //
    // checkTransABmultAdd(B, C);
    //
    // double[] Bd = ((DenseComplexMatrix) B).getData(), Cd =
    // ((DenseComplexMatrix) C)
    // .getData();
    //
    // BLAS.getInstance().dgemm(Transpose.Transpose.netlib(),
    // Transpose.Transpose.netlib(),
    // C.numRows(), C.numColumns(), numRows, alpha, data,
    // Math.max(1, numRows), Bd, Math.max(1, B.numRows()), 1, Cd,
    // Math.max(1, C.numRows()));
    //
    // return C;
    // }

    // @Override
    // public ComplexMatrix rank1(double alpha[], ComplexVector x, ComplexVector
    // y) {
    // if (!(x instanceof DenseComplexVector) || !(y instanceof
    // DenseComplexVector))
    // return super.rank1(alpha, x, y);
    //
    // checkRank1(x, y);
    //
    // double[] xd = ((DenseComplexVector) x).getData(), yd =
    // ((DenseComplexVector) y)
    // .getData();
    //
    // BLAS.getInstance().dger(numRows, numColumns, alpha, xd, 1, yd, 1, data,
    // Math.max(1, numRows));
    //
    // return this;
    // }

    // @Override
    // public ComplexVector multAdd(double alpha[], ComplexVector x,
    // ComplexVector y) {
    // if (!(x instanceof DenseComplexVector) || !(y instanceof
    // DenseComplexVector))
    // return super.multAdd(alpha, x, y);
    //
    // checkMultAdd(x, y);
    //
    // double[] xd = ((DenseComplexVector) x).getData(), yd =
    // ((DenseComplexVector) y)
    // .getData();
    //
    // BLAS.getInstance().dgemv(Transpose.NoTranspose.netlib(), numRows,
    // numColumns,
    // alpha, data, Math.max(numRows, 1), xd, 1, 1, yd, 1);
    //
    // return y;
    // }

    // @Override
    // public ComplexVector transMultAdd(double alpha[], ComplexVector x,
    // ComplexVector y) {
    // if (!(x instanceof DenseComplexVector) || !(y instanceof
    // DenseComplexVector))
    // return super.transMultAdd(alpha, x, y);
    //
    // checkTransMultAdd(x, y);
    //
    // double[] xd = ((DenseComplexVector) x).getData(), yd =
    // ((DenseComplexVector) y)
    // .getData();
    //
    // BLAS.getInstance().dgemv(Transpose.Transpose.netlib(), numRows,
    // numColumns, alpha,
    // data, Math.max(numRows, 1), xd, 1, 1, yd, 1);
    //
    // return y;
    // }

    // @Override
    // public ComplexMatrix solve(ComplexMatrix B, ComplexMatrix X) {
    // // We allow non-square ComplexMatrices, as we then use a least-squares
    // solver
    // if (numRows != B.numRows())
    // throw new IllegalArgumentException("numRows != B.numRows() ("
    // + numRows + " != " + B.numRows() + ")");
    // if (numColumns != X.numRows())
    // throw new IllegalArgumentException("numColumns != X.numRows() ("
    // + numColumns + " != " + X.numRows() + ")");
    // if (X.numColumns() != B.numColumns())
    // throw new IllegalArgumentException(
    // "X.numColumns() != B.numColumns() (" + X.numColumns()
    // + " != " + B.numColumns() + ")");
    //
    // if (isSquare())
    // return LUsolve(B, X);
    // else
    // return QRsolve(B, X, Transpose.NoTranspose);
    // }

    // @Override
    // public ComplexVector solve(ComplexVector b, ComplexVector x) {
    // DenseComplexMatrix B = new DenseComplexMatrix(b, false), X = new
    // DenseComplexMatrix(x, false);
    // solve(B, X);
    // return x;
    // }

    // @Override
    // public ComplexMatrix transSolve(ComplexMatrix B, ComplexMatrix X) {
    // // We allow non-square ComplexMatrices, as we then use a least-squares
    // solver
    // if (numColumns != B.numRows())
    // throw new IllegalArgumentException("numColumns != B.numRows() ("
    // + numColumns + " != " + B.numRows() + ")");
    // if (numRows != X.numRows())
    // throw new IllegalArgumentException("numRows != X.numRows() ("
    // + numRows + " != " + X.numRows() + ")");
    // if (X.numColumns() != B.numColumns())
    // throw new IllegalArgumentException(
    // "X.numColumns() != B.numColumns() (" + X.numColumns()
    // + " != " + B.numColumns() + ")");
    //
    // return QRsolve(B, X, Transpose.Transpose);
    // }

    // @Override
    // public ComplexVector transSolve(ComplexVector b, ComplexVector x) {
    // DenseComplexMatrix B = new DenseComplexMatrix(b, false), X = new
    // DenseComplexMatrix(x, false);
    // transSolve(B, X);
    // return x;
    // }

    // ComplexMatrix LUsolve(ComplexMatrix B, ComplexMatrix X) {
    // if (!(X instanceof DenseComplexMatrix))
    // throw new
    // UnsupportedOperationException("X must be a DenseComplexMatrix");
    //
    // double[] Xd = ((DenseComplexMatrix) X).getData();
    //
    // X.set(B);
    //
    // int[] piv = new int[numRows];
    //
    // intW info = new intW(0);
    // LAPACK.getInstance().dgesv(numRows, B.numColumns(),
    // data.clone(), ComplexMatrices.ld(numRows), piv, Xd,
    // ComplexMatrices.ld(numRows), info);
    //
    // if (info.val > 0)
    // throw new MatrixSingularException();
    // else if (info.val < 0)
    // throw new IllegalArgumentException();
    //
    // return X;
    // }

    // ComplexMatrix QRsolve(ComplexMatrix B, ComplexMatrix X, Transpose trans)
    // {
    // int nrhs = B.numColumns();
    //
    // // Allocate temporary solution ComplexMatrix
    // DenseComplexMatrix Xtmp = new DenseComplexMatrix(Math.max(numRows,
    // numColumns), nrhs);
    // int M = trans == Transpose.NoTranspose ? numRows : numColumns;
    // for (int j = 0; j < nrhs; ++j)
    // for (int i = 0; i < M; ++i)
    // Xtmp.set(i, j, B.get(i, j));
    // double[] newData = data.clone();
    //
    // // Query optimal workspace
    // double[] work = new double[1];
    // intW info = new intW(0);
    // LAPACK.getInstance().dgels(trans.netlib(), numRows, numColumns, nrhs,
    // newData, ComplexMatrices.ld(numRows), Xtmp.getData(),
    // ComplexMatrices.ld(numRows, numColumns),
    // work, -1, info);
    //
    // // Allocate workspace
    // int lwork = -1;
    // if (info.val != 0)
    // lwork = Math.max(1, Math.min(numRows, numColumns)
    // + Math.max(Math.min(numRows, numColumns), nrhs));
    // else
    // lwork = Math.max((int) work[0], 1);
    // work = new double[lwork];
    //
    // // Compute the factorization
    // info.val = 0;
    // LAPACK.getInstance().dgels(trans.netlib(), numRows, numColumns, nrhs,
    // newData, ComplexMatrices.ld(numRows), Xtmp.getData(),
    // ComplexMatrices.ld(numRows, numColumns),
    // work, lwork, info);
    //
    // if (info.val < 0)
    // throw new IllegalArgumentException();
    //
    // // Extract the solution
    // int N = trans == Transpose.NoTranspose ? numColumns : numRows;
    // for (int j = 0; j < nrhs; ++j)
    // for (int i = 0; i < N; ++i)
    // X.set(i, j, Xtmp.get(i, j));
    // return X;
    // }
}
