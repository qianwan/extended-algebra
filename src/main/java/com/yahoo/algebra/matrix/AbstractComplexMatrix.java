package com.yahoo.algebra.matrix;

import java.util.Formatter;
import java.util.Iterator;

import com.yahoo.networkmimo.exception.ComplexMatrixNotSPDException;

public abstract class AbstractComplexMatrix implements ComplexMatrix {

    private static double equalThreshold = 1e-6;
    /**
     * Number of rows
     */
    protected int numRows;

    /**
     * Number of columns
     */
    protected int numColumns;

    /**
     * Constructor for AbstractComplexMatrix
     */
    protected AbstractComplexMatrix(int numRows, int numColumns) {
        if (numRows < 0 || numColumns < 0)
            throw new IndexOutOfBoundsException("Matrix size cannot be negative");
        this.numRows = numRows;
        this.numColumns = numColumns;
    }

    /**
     * Constructor for AbstractComplexMatrix, same size as A. The invoking
     * constructor should set this matrix equal the argument matrix
     */
    protected AbstractComplexMatrix(ComplexMatrix A) {
        this(A.numRows(), A.numColumns());
    }

    @Override
    public boolean equals(Object A) {
        if (null == A)
            return false;
        if (this == A)
            return true;
        else {
            if (A instanceof AbstractComplexMatrix) {
                AbstractComplexMatrix B = (AbstractComplexMatrix) A;
                if (numRows() != B.numRows() || numColumns() != B.numColumns()) {
                    return false;
                }
                for (ComplexMatrixEntry e : this) {
                    double[] v = e.get();
                    double[] vc = B.get(e.row(), e.column());
                    if (Math.abs(v[0] - vc[0]) > getEqualThreshold()
                            || Math.abs(v[1] - vc[1]) > getEqualThreshold()) {
                        return false;
                    }
                }
            } else {
                throw new UnsupportedOperationException();
            }
            return true;
        }
    }

    public int numRows() {
        return numRows;
    }

    public int numColumns() {
        return numColumns;
    }

    public boolean isSquare() {
        return numRows == numColumns;
    }

    public void set(int row, int column, double value[]) {
        throw new UnsupportedOperationException();
    }

    public void add(int row, int column, double value[]) {
        double[] adder = get(row, column);
        set(row, column, new double[] { value[0] + adder[0], value[1] + adder[1] });
    }

    public double[] get(int row, int column) {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks the passed row and column indices
     */
    protected void check(int row, int column) {
        if (row < 0)
            throw new IndexOutOfBoundsException("row index is negative (" + row + ")");
        if (column < 0)
            throw new IndexOutOfBoundsException("column index is negative (" + column + ")");
        if (row >= numRows)
            throw new IndexOutOfBoundsException("row index >= numRows (" + row + " >= " + numRows
                    + ")");
        if (column >= numColumns)
            throw new IndexOutOfBoundsException("column index >= numColumns (" + column + " >= "
                    + numColumns + ")");
    }

    public ComplexMatrix copy() {
        throw new UnsupportedOperationException();
    }

    public ComplexMatrix zero() {
        for (ComplexMatrixEntry e : this)
            e.set(new double[] { 0, 0 });
        return this;
    }

    public ComplexVector mult(ComplexVector x, ComplexVector y) {
        return mult(new double[] { 1, 0 }, x, y);
    }

    public ComplexVector mult(double alpha[], ComplexVector x, ComplexVector y) {
        return multAdd(alpha, x, y.zero());
    }

    public ComplexVector multAdd(ComplexVector x, ComplexVector y) {
        return multAdd(new double[] { 1, 0 }, x, y);
    }

    public ComplexVector multAdd(double alpha[], ComplexVector x, ComplexVector y) {
        checkMultAdd(x, y);

        if (alpha[0] != 0 && alpha[1] != 0)
            for (ComplexMatrixEntry e : this) {
                double[] tmp = Complexes.mult(alpha, e.get());
                tmp = Complexes.mult(tmp, x.get(e.column()));
                y.add(e.row(), tmp);
            }
        return y;
    }

    /**
     * Checks the arguments to <code>mult</code> and <code>multAdd</code>
     */
    protected void checkMultAdd(ComplexVector x, ComplexVector y) {
        if (numColumns != x.size())
            throw new IndexOutOfBoundsException("A.numColumns != x.size (" + numColumns + " != "
                    + x.size() + ")");
        if (numRows != y.size())
            throw new IndexOutOfBoundsException("A.numRows != y.size (" + numRows + " != "
                    + y.size() + ")");
    }

    public ComplexVector transMult(ComplexVector x, ComplexVector y) {
        return transMult(new double[] { 1, 0 }, x, y);
    }

    public ComplexVector transMult(double alpha[], ComplexVector x, ComplexVector y) {
        return transMultAdd(alpha, x, y.zero());
    }

    public ComplexVector transMultAdd(ComplexVector x, ComplexVector y) {
        return transMultAdd(new double[] { 1, 0 }, x, y);
    }

    public ComplexVector transMultAdd(double alpha[], ComplexVector x, ComplexVector y) {
        checkTransMultAdd(x, y);

        if (alpha[0] != 0 && alpha[1] != 0)
            for (ComplexMatrixEntry e : this)
                y.add(e.column(), Complexes.mult(Complexes.mult(alpha, e.get()), x.get(e.row())));

        return y;
    }

    /**
     * Checks the arguments to <code>transMult</code> and
     * <code>transMultAdd</code>
     */
    protected void checkTransMultAdd(ComplexVector x, ComplexVector y) {
        if (numRows != x.size())
            throw new IndexOutOfBoundsException("A.numRows != x.size (" + numRows + " != "
                    + x.size() + ")");
        if (numColumns != y.size())
            throw new IndexOutOfBoundsException("A.numColumns != y.size (" + numColumns + " != "
                    + y.size() + ")");
    }

    public ComplexVector solve(ComplexVector b, ComplexVector x) {
        throw new UnsupportedOperationException();
    }

    public ComplexVector transSolve(ComplexVector b, ComplexVector x) {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks that a matrix inversion is legal for the given arguments. This is
     * for the square case, not for least-squares problems
     */
    protected void checkSolve(ComplexVector b, ComplexVector x) {
        if (!isSquare())
            throw new IndexOutOfBoundsException("!A.isSquare");
        if (numRows != b.size())
            throw new IndexOutOfBoundsException("numRows != b.size (" + numRows + " != " + b.size()
                    + ")");
        if (numColumns != x.size())
            throw new IndexOutOfBoundsException("numColumns != x.size (" + numColumns + " != "
                    + x.size() + ")");
    }

    public ComplexMatrix rank1(ComplexVector x) {
        return rank1(new double[] { 1, 0 }, x);
    }

    public ComplexMatrix rank1(double alpha[], ComplexVector x) {
        return rank1(alpha, x, x);
    }

    public ComplexMatrix rank1(ComplexVector x, ComplexVector y) {
        return rank1(new double[] { 1, 0 }, x, y);
    }

    public ComplexMatrix rank1(double alpha[], ComplexVector x, ComplexVector y) {
        checkRank1(x, y);

        if (alpha[0] == 0 && alpha[1] == 0)
            return this;

        for (ComplexVectorEntry ei : x)
            if (ei.get()[0] != 0 && ei.get()[1] != 0)
                for (ComplexVectorEntry ej : y)
                    if (ej.get()[0] != 0 && ej.get()[1] != 0) {
                        double[] tmp = Complexes.mult(alpha, ei.get());
                        tmp = Complexes.mult(tmp, ej.get());
                        add(ei.index(), ej.index(), tmp);
                    }
        return this;
    }

    /**
     * Checks that a vector rank1 update is possible for the given vectors
     */
    protected void checkRank1(ComplexVector x, ComplexVector y) {
        if (!isSquare())
            throw new IndexOutOfBoundsException("!A.isSquare");
        if (x.size() != numRows)
            throw new IndexOutOfBoundsException("x.size != A.numRows (" + x.size() + " != "
                    + numRows + ")");
        if (y.size() != numColumns)
            throw new IndexOutOfBoundsException("y.size != A.numColumns (" + y.size() + " != "
                    + numColumns + ")");
    }

    public ComplexMatrix rank2(ComplexVector x, ComplexVector y) {
        return rank2(new double[] { 1, 0 }, x, y);
    }

    public ComplexMatrix rank2(double alpha[], ComplexVector x, ComplexVector y) {
        checkRank2(x, y);

        if (alpha[0] == 0 && alpha[1] == 0)
            return this;

        for (ComplexVectorEntry ei : x)
            for (ComplexVectorEntry ej : y) {
                double[] tmp = Complexes.mult(Complexes.mult(alpha, ei.get()), ej.get());
                add(ei.index(), ej.index(), tmp);
                tmp = Complexes.mult(Complexes.mult(alpha, ei.get()), ej.get());
                add(ej.index(), ei.index(), tmp);
            }

        return this;
    }

    /**
     * Checks that a vector rank2 update is legal with the given vectors
     */
    protected void checkRank2(ComplexVector x, ComplexVector y) {
        if (!isSquare())
            throw new IndexOutOfBoundsException("!A.isSquare");
        if (x.size() != numRows)
            throw new IndexOutOfBoundsException("x.size != A.numRows (" + x.size() + " != "
                    + numRows + ")");
        if (y.size() != numRows)
            throw new IndexOutOfBoundsException("y.size != A.numRows (" + y.size() + " != "
                    + numRows + ")");
    }

    public ComplexMatrix mult(ComplexMatrix B, ComplexMatrix C) {
        return mult(new double[] { 1, 0 }, B, C);
    }

    public ComplexMatrix mult(double alpha[], ComplexMatrix B, ComplexMatrix C) {
        return multAdd(alpha, B, C.zero());
    }

    public ComplexMatrix multAdd(ComplexMatrix B, ComplexMatrix C) {
        return multAdd(new double[] { 1, 0 }, B, C);
    }

    public ComplexMatrix multAdd(double alpha[], ComplexMatrix B, ComplexMatrix C) {
        checkMultAdd(B, C);

        if (alpha[0] != 0 || alpha[1] != 0)
            for (int i = 0; i < numRows; ++i)
                for (int j = 0; j < C.numColumns(); ++j) {
                    double[] dot = new double[] { 0, 0 };
                    for (int k = 0; k < numColumns; ++k) {
                        dot = Complexes.add(dot, Complexes.mult(get(i, k), B.get(k, j)));
                    }
                    C.add(i, j, Complexes.mult(alpha, dot));
                }

        return C;
    }

    /**
     * Checks the arguments to <code>mult</code> and <code>multAdd</code>
     */
    protected void checkMultAdd(ComplexMatrix B, ComplexMatrix C) {
        if (numRows != C.numRows())
            throw new IndexOutOfBoundsException("A.numRows != C.numRows (" + numRows + " != "
                    + C.numRows() + ")");
        if (numColumns != B.numRows())
            throw new IndexOutOfBoundsException("A.numColumns != B.numRows (" + numColumns + " != "
                    + B.numRows() + ")");
        if (B.numColumns() != C.numColumns())
            throw new IndexOutOfBoundsException("B.numColumns != C.numColumns (" + B.numRows()
                    + " != " + C.numColumns() + ")");
    }

    public ComplexMatrix transAmult(ComplexMatrix B, ComplexMatrix C) {
        return transAmult(new double[] { 1, 0 }, B, C);
    }

    public ComplexMatrix transAmult(double alpha[], ComplexMatrix B, ComplexMatrix C) {
        return transAmultAdd(alpha, B, C.zero());
    }

    public ComplexMatrix transAmultAdd(ComplexMatrix B, ComplexMatrix C) {
        return transAmultAdd(new double[] { 1, 0 }, B, C);
    }

    public ComplexMatrix transAmultAdd(double alpha[], ComplexMatrix B, ComplexMatrix C) {
        checkTransAmultAdd(B, C);

        if (alpha[0] != 0 && alpha[1] != 0)
            for (int i = 0; i < numColumns; ++i)
                for (int j = 0; j < C.numColumns(); ++j) {
                    double[] dot = new double[] { 0, 0 };
                    for (int k = 0; k < numRows; ++k)
                        dot = Complexes.add(dot, Complexes.mult(get(k, i), B.get(k, j)));
                    C.add(i, j, Complexes.mult(alpha, dot));
                }

        return C;
    }

    /**
     * Checks the arguments to <code>transAmult</code> and
     * <code>transAmultAdd</code>
     */
    protected void checkTransAmultAdd(ComplexMatrix B, ComplexMatrix C) {
        if (numRows != B.numRows())
            throw new IndexOutOfBoundsException("A.numRows != B.numRows (" + numRows + " != "
                    + B.numRows() + ")");
        if (numColumns != C.numRows())
            throw new IndexOutOfBoundsException("A.numColumns != C.numRows (" + numColumns + " != "
                    + C.numRows() + ")");
        if (B.numColumns() != C.numColumns())
            throw new IndexOutOfBoundsException("B.numColumns != C.numColumns (" + B.numColumns()
                    + " != " + C.numColumns() + ")");
    }

    public ComplexMatrix transBmult(ComplexMatrix B, ComplexMatrix C) {
        return transBmult(new double[] { 1, 0 }, B, C);
    }

    public ComplexMatrix transBmult(double alpha[], ComplexMatrix B, ComplexMatrix C) {
        return transBmultAdd(alpha, B, C.zero());
    }

    public ComplexMatrix transBmultAdd(ComplexMatrix B, ComplexMatrix C) {
        return transBmultAdd(new double[] { 1, 0 }, B, C);
    }

    public ComplexMatrix transBmultAdd(double alpha[], ComplexMatrix B, ComplexMatrix C) {
        checkTransBmultAdd(B, C);

        if (alpha[0] != 0 && alpha[1] != 0)
            for (int i = 0; i < numRows; ++i)
                for (int j = 0; j < C.numColumns(); ++j) {
                    double[] dot = new double[] { 0, 0 };
                    for (int k = 0; k < numColumns; ++k)
                        dot = Complexes.add(dot, Complexes.mult(get(i, k), B.get(j, k)));
                    C.add(i, j, Complexes.mult(alpha, dot));
                }

        return C;
    }

    /**
     * Checks the arguments to <code>transBmult</code> and
     * <code>transBmultAdd</code>
     */
    protected void checkTransBmultAdd(ComplexMatrix B, ComplexMatrix C) {
        if (numColumns != B.numColumns())
            throw new IndexOutOfBoundsException("A.numColumns != B.numColumns (" + numColumns
                    + " != " + B.numColumns() + ")");
        if (numRows != C.numRows())
            throw new IndexOutOfBoundsException("A.numRows != C.numRows (" + numRows + " != "
                    + C.numRows() + ")");
        if (B.numRows() != C.numColumns())
            throw new IndexOutOfBoundsException("B.numRows != C.numColumns (" + B.numRows()
                    + " != " + C.numColumns() + ")");
    }

    public ComplexMatrix transABmult(ComplexMatrix B, ComplexMatrix C) {
        return transABmult(new double[] { 1, 0 }, B, C);
    }

    public ComplexMatrix transABmult(double alpha[], ComplexMatrix B, ComplexMatrix C) {
        return transABmultAdd(alpha, B, C.zero());
    }

    public ComplexMatrix transABmultAdd(ComplexMatrix B, ComplexMatrix C) {
        return transABmultAdd(new double[] { 1, 0 }, B, C);
    }

    public ComplexMatrix transABmultAdd(double alpha[], ComplexMatrix B, ComplexMatrix C) {
        checkTransABmultAdd(B, C);

        if (alpha[0] != 0 && alpha[1] != 0)
            for (int i = 0; i < numColumns; ++i)
                for (int j = 0; j < C.numColumns(); ++j) {
                    double[] dot = new double[] { 0, 0 };
                    for (int k = 0; k < numRows; ++k) {
                        dot = Complexes.add(dot, Complexes.mult(get(k, i), B.get(j, k)));
                    }
                    C.add(i, j, Complexes.mult(alpha, dot));
                }

        return C;
    }

    /**
     * Checks the arguments to <code>transABmultAdd</code> and
     * <code>transABmultAdd</code>
     */
    protected void checkTransABmultAdd(ComplexMatrix B, ComplexMatrix C) {
        if (numRows != B.numColumns())
            throw new IndexOutOfBoundsException("A.numRows != B.numColumns (" + numRows + " != "
                    + B.numColumns() + ")");
        if (numColumns != C.numRows())
            throw new IndexOutOfBoundsException("A.numColumns != C.numRows (" + numColumns + " != "
                    + C.numRows() + ")");
        if (B.numRows() != C.numColumns())
            throw new IndexOutOfBoundsException("B.numRows != C.numColumns (" + B.numRows()
                    + " != " + C.numColumns() + ")");
    }

    public ComplexMatrix solve(ComplexMatrix B, ComplexMatrix X) {
        throw new UnsupportedOperationException();
    }

    public ComplexMatrix transSolve(ComplexMatrix B, ComplexMatrix X) {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks that a matrix inversion is legal for the given arguments. This is
     * for the square case, not for least-squares problems
     */
    protected void checkSolve(ComplexMatrix B, ComplexMatrix X) {
        if (!isSquare())
            throw new IndexOutOfBoundsException("!A.isSquare");
        if (B.numRows() != numRows)
            throw new IndexOutOfBoundsException("B.numRows != A.numRows (" + B.numRows() + " != "
                    + numRows + ")");
        if (B.numColumns() != X.numColumns())
            throw new IndexOutOfBoundsException("B.numColumns != X.numColumns (" + B.numColumns()
                    + " != " + X.numColumns() + ")");
        if (X.numRows() != numColumns)
            throw new IndexOutOfBoundsException("X.numRows != A.numColumns (" + X.numRows()
                    + " != " + numColumns + ")");
    }

    public ComplexMatrix rank1(ComplexMatrix C) {
        return rank1(new double[] { 1, 0 }, C);
    }

    public ComplexMatrix rank1(double alpha[], ComplexMatrix C) {
        checkRank1(C);

        if (alpha[0] == 0 && alpha[1] == 0)
            return this;

        return C.transBmultAdd(alpha, C, this);
    }

    /**
     * Checks that a matrix rank1 update is possible for the given matrix
     */
    protected void checkRank1(ComplexMatrix C) {
        if (!isSquare())
            throw new IndexOutOfBoundsException("!A.isSquare");
        if (numRows != C.numRows())
            throw new IndexOutOfBoundsException("A.numRows != C.numRows (" + numRows + " != "
                    + C.numRows() + ")");
    }

    public ComplexMatrix transRank1(ComplexMatrix C) {
        return transRank1(new double[] { 1, 0 }, C);
    }

    public ComplexMatrix transRank1(double alpha[], ComplexMatrix C) {
        checkTransRank1(C);

        if (alpha[0] == 0 && alpha[1] == 0)
            return this;

        return C.transAmultAdd(alpha, C, this);
    }

    /**
     * Checks that a transposed rank1 update is leagal with the given argument
     */
    protected void checkTransRank1(ComplexMatrix C) {
        if (!isSquare())
            throw new IndexOutOfBoundsException("!A.isSquare");
        if (numRows != C.numColumns())
            throw new IndexOutOfBoundsException("A.numRows != C.numColumns (" + numRows + " != "
                    + C.numColumns() + ")");
    }

    public ComplexMatrix rank2(ComplexMatrix B, ComplexMatrix C) {
        return rank2(new double[] { 1, 0 }, B, C);
    }

    public ComplexMatrix rank2(double alpha[], ComplexMatrix B, ComplexMatrix C) {
        checkRank2(B, C);

        if (alpha[0] == 0 && alpha[1] == 0)
            return this;

        return B.transBmultAdd(alpha, C, C.transBmultAdd(alpha, B, this));
    }

    /**
     * Checks that a rank2 update is legal for the given arguments
     */
    protected void checkRank2(ComplexMatrix B, ComplexMatrix C) {
        if (!isSquare())
            throw new IndexOutOfBoundsException("!A.isSquare");
        if (B.numRows() != C.numRows())
            throw new IndexOutOfBoundsException("B.numRows != C.numRows (" + B.numRows() + " != "
                    + C.numRows() + ")");
        if (B.numColumns() != C.numColumns())
            throw new IndexOutOfBoundsException("B.numColumns != C.numColumns (" + B.numColumns()
                    + " != " + C.numColumns() + ")");
    }

    public ComplexMatrix transRank2(ComplexMatrix B, ComplexMatrix C) {
        return transRank2(new double[] { 1, 0 }, B, C);
    }

    public ComplexMatrix transRank2(double alpha[], ComplexMatrix B, ComplexMatrix C) {
        checkTransRank2(B, C);

        if (alpha[0] == 0 && alpha[1] == 0)
            return this;

        return B.transAmultAdd(alpha, C, C.transAmultAdd(alpha, B, this));
    }

    /**
     * Checks that a transposed rank2 update is leagal with the given arguments
     */
    protected void checkTransRank2(ComplexMatrix B, ComplexMatrix C) {
        if (!isSquare())
            throw new IndexOutOfBoundsException("!A.isSquare");
        if (numRows != B.numColumns())
            throw new IndexOutOfBoundsException("A.numRows != B.numColumns (" + numRows + " != "
                    + B.numColumns() + ")");
        if (B.numRows() != C.numRows())
            throw new IndexOutOfBoundsException("B.numRows != C.numRows (" + B.numRows() + " != "
                    + C.numRows() + ")");
        if (B.numColumns() != C.numColumns())
            throw new IndexOutOfBoundsException("B.numColumns != C.numColumns (" + B.numColumns()
                    + " != " + C.numColumns() + ")");
    }

    public ComplexMatrix scale(double alpha[]) {
        if (alpha[0] == 1 && alpha[1] == 0)
            return this;
        else if (alpha[0] == 0 && alpha[1] == 0)
            return zero();

        for (ComplexMatrixEntry e : this)
            e.set(Complexes.mult(alpha, e.get()));

        return this;
    }

    public ComplexMatrix set(ComplexMatrix B) {
        return set(new double[] { 1, 0 }, B);
    }

    public ComplexMatrix set(double alpha[], ComplexMatrix B) {
        checkSize(B);

        if (alpha[0] == 0 && alpha[1] == 0.)
            return zero();
        if (B == this)
            return scale(alpha);

        zero();
        for (ComplexMatrixEntry e : B)
            set(e.row(), e.column(), Complexes.mult(alpha, e.get()));

        return this;
    }

    public ComplexMatrix add(ComplexMatrix B) {
        return add(new double[] { 1, 0 }, B);
    }

    public ComplexMatrix add(double alpha[], ComplexMatrix B) {
        checkSize(B);

        if (alpha[0] != 0 && alpha[1] != 0)
            for (ComplexMatrixEntry e : B)
                add(e.row(), e.column(), Complexes.mult(alpha, e.get()));

        return this;
    }

    /**
     * Checks that the sizes of this matrix and the given conform
     */
    protected void checkSize(ComplexMatrix B) {
        if (numRows != B.numRows())
            throw new IndexOutOfBoundsException("A.numRows != B.numRows (" + numRows + " != "
                    + B.numRows() + ")");
        if (numColumns != B.numColumns())
            throw new IndexOutOfBoundsException("A.numColumns != B.numColumns (" + numColumns
                    + " != " + B.numColumns() + ")");
    }

    public ComplexMatrix transpose() {
        checkTranspose();

        for (int j = 0; j < numColumns; ++j)
            for (int i = j + 1; i < numRows; ++i) {
                double value[] = get(i, j);
                set(i, j, get(j, i));
                set(j, i, value);
            }

        return this;
    }

    public ComplexMatrix hermitianTranspose() {
        throw new ComplexMatrixNotSPDException("in-place Hermitian transpose is not supported now");
    }

    /**
     * Checks that the matrix may be transposed
     */
    protected void checkTranspose() {
        if (!isSquare())
            throw new IndexOutOfBoundsException("!A.isSquare");
    }

    public ComplexMatrix transpose(ComplexMatrix B) {
        checkTranspose(B);

        if (B == this)
            return transpose();

        B.zero();
        for (ComplexMatrixEntry e : this)
            B.set(e.column(), e.row(), e.get());

        return B;
    }

    public ComplexMatrix hermitianTranspose(ComplexMatrix B) {
        checkTranspose(B);

        if (B == this) {
            return hermitianTranspose();
        }

        B.zero();
        for (ComplexMatrixEntry e : this) {
            B.set(e.column(), e.row(), Complexes.conjugate(e.get()));
        }
        return B;
    }

    /**
     * Checks that this matrix can be transposed into the given matrix
     */
    protected void checkTranspose(ComplexMatrix B) {
        if (numRows != B.numColumns())
            throw new IndexOutOfBoundsException("A.numRows != B.numColumns (" + numRows + " != "
                    + B.numColumns() + ")");
        if (numColumns != B.numRows())
            throw new IndexOutOfBoundsException("A.numColumns != B.numRows (" + numColumns + " != "
                    + B.numRows() + ")");
    }

    public double norm(Norm type) {
        if (type == Norm.One)
            return norm1();
        else if (type == Norm.Frobenius)
            return normF();
        else if (type == Norm.Infinity)
            return normInf();
        else
            // Maxvalue
            return max();
    }

    /**
     * Computes the 1 norm
     */
    protected double norm1() {
        double[] rowSum = new double[numRows];
        for (ComplexMatrixEntry e : this)
            rowSum[e.row()] += Complexes.abs(e.get());
        return max(rowSum);
    }

    /**
     * Computes the Frobenius norm. This implementation is overflow resistant
     */
    protected double normF() {
        double scale = 0, ssq = 1;
        for (ComplexMatrixEntry e : this) {
            double Aval[] = e.get();
            if (Aval[0] != 0 || Aval[1] != 0) {
                double absxi = Complexes.abs(Aval);
                if (scale < absxi) {
                    ssq = 1 + ssq * Math.pow(scale / absxi, 2);
                    scale = absxi;
                } else
                    ssq = ssq + Math.pow(absxi / scale, 2);
            }
        }
        return scale * Math.sqrt(ssq);
    }

    /**
     * Computes the infinity norm
     */
    protected double normInf() {
        double[] columnSum = new double[numColumns];
        for (ComplexMatrixEntry e : this)
            columnSum[e.column()] += Complexes.abs(e.get());
        return max(columnSum);
    }

    /**
     * Returns the largest absolute value
     */
    protected double max() {
        double max = 0;
        for (ComplexMatrixEntry e : this)
            max = Math.max(Complexes.abs(e.get()), max);
        return max;
    }

    /**
     * Returns the largest element of the passed array
     */
    protected double max(double[] x) {
        double max = 0;
        for (int i = 0; i < x.length; ++i)
            max = Math.max(x[i], max);
        return max;
    }

    @Override
    public String toString() {
        // Output into coordinate format. Indices start from 1 instead of 0
        @SuppressWarnings("resource")
        Formatter out = new Formatter();

        out.format("%10d %10d %19d\n", numRows, numColumns, ComplexMatrices.cardinality(this));

        for (ComplexMatrixEntry e : this)
            if (e.get()[0] != 0 || e.get()[1] != 0)
                out.format("%10d %10d % .12e\n", e.row() + 1, e.column() + 1, e.get());

        return out.toString();
    }

    public Iterator<ComplexMatrixEntry> iterator() {
        return new RefMatrixIterator();
    }

    /**
     * @return the equalThreshold
     */
    public static double getEqualThreshold() {
        return equalThreshold;
    }

    /**
     * @param equalThreshold
     *            the equalThreshold to set
     */
    public static void setEqualThreshold(double equalThreshold) {
        AbstractComplexMatrix.equalThreshold = equalThreshold;
    }

    public double[] trace() {
        if (!isSquare()) {
            throw new ComplexMatrixNotSPDException(
                    "trace operation is not supported for non-square matrix");
        }

        double[] trace = new double[] { 0.0, 0.0 };
        for (int i = 0; i < numRows; i++) {
            trace = Complexes.add(trace, get(i, i));
        }
        return trace;
    }

    /**
     * Iterator over a general matrix. Uses column-major traversal
     */
    class RefMatrixIterator implements Iterator<ComplexMatrixEntry> {

        /**
         * Matrix cursor
         */
        int row, column;

        /**
         * Matrix entry
         */
        final RefMatrixEntry entry = new RefMatrixEntry();

        public boolean hasNext() {
            return (row < numRows) && (column < numColumns);
        }

        public ComplexMatrixEntry next() {
            entry.update(row, column);

            // Traversal first down the columns, then the rows
            if (row < numRows - 1)
                row++;
            else {
                column++;
                row = 0;
            }

            return entry;
        }

        public void remove() {
            entry.set(new double[] { 0, 0 });
        }

    }

    /**
     * Matrix entry backed by the matrix. May be reused for higher performance
     */
    class RefMatrixEntry implements ComplexMatrixEntry {

        /**
         * Matrix position
         */
        private int row, column;

        /**
         * Updates the entry
         */
        public void update(int row, int column) {
            this.row = row;
            this.column = column;
        }

        public int row() {
            return row;
        }

        public int column() {
            return column;
        }

        public double[] get() {
            return AbstractComplexMatrix.this.get(row, column);
        }

        public void set(double value[]) {
            AbstractComplexMatrix.this.set(row, column, value);
        }
    }
}
