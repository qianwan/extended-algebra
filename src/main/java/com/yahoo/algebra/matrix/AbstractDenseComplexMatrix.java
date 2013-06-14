package com.yahoo.algebra.matrix;

import java.util.Arrays;

public abstract class AbstractDenseComplexMatrix extends AbstractComplexMatrix {
    double data[];

    /**
     * Constructor for AbstractDenseMatrix. The matrix contents will be set to
     * zero
     * 
     * @param numRows
     *            Number of rows
     * @param numColumns
     *            Number of columns
     */
    public AbstractDenseComplexMatrix(int numRows, int numColumns) {
        super(numRows, numColumns);

        data = new double[numRows * 2 * numColumns * 2];
    }
    
    /**
     * Constructor for AbstractDenseMatrix. Matrix is copied from the supplied
     * matrix
     * 
     * @param A
     *            Matrix to copy from
     */
    public AbstractDenseComplexMatrix(ComplexMatrix A) {
        this(A, true);
    }

    /**
     * Constructor for AbstractDenseMatrix. Matrix is copied from the supplied
     * matrix
     * 
     * @param A
     *            Matrix to copy from
     * @param deep
     *            True for deep copy, false for reference
     */
    public AbstractDenseComplexMatrix(ComplexMatrix A, boolean deep) {
        super(A);

        if (deep) {
            data = new double[2 * numRows * 2 * numColumns];
            copy(A);
        } else
            this.data = ((AbstractDenseComplexMatrix) A).getData();
    }

    /**
     * Set this matrix equal to the given matrix
     */
    abstract void copy(ComplexMatrix A);

    /**
     * Returns the matrix contents. Ordering depends on the underlying storage
     * assumptions
     */
    public double[] getData() {
        return data;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<numRows(); i++) {
            for (int j=0; j<numColumns(); j++) {
                sb.append(String.format("%6.4g", get(i, j)[0]));
                sb.append(String.format("%+6.4gj\t", get(i, j)[1]));
            }
            if (i!=numRows()-1) {
                sb.append("\n");
            }
        }
        return sb.toString().trim();
    }

    @Override
    public void add(int row, int column, double value[]) {
        int[] index = getIndex(row, column);
        data[index[0]] += value[0];
        data[index[1]] += value[1];
        int offset = data.length / 2;
        data[offset + index[0]] = -data[index[1]];
        data[offset + index[1]] = data[index[0]];
    }

    @Override
    public void set(int row, int column, double value[]) {
        int[] index = getIndex(row, column);
        data[index[0]] = value[0];
        data[index[1]] = value[1];
        int offset = data.length / 2;
        data[offset + index[0]] = -data[index[1]];
        data[offset + index[1]] = data[index[0]];
    }

    @Override
    public double[] get(int row, int column) {
        int[] index = getIndex(row, column);
        return new double[]{data[index[0]], data[index[1]]};
    }

    /**
     * Checks the row and column indices, and returns the linear data index
     */
    int[] getIndex(int row, int column) {
        check(row, column);
        int re = row + numRows * 2 * column;
        int im = row + numRows + numRows * 2 * column;
        return new int[]{re, im};
    }

    @Override
    public ComplexMatrix set(ComplexMatrix B) {
        if (!(B instanceof AbstractDenseComplexMatrix))
            return super.set(B);

        checkSize(B);

        double[] Bd = ((AbstractDenseComplexMatrix) B).getData();

        if (Bd == data)
            return this;

        System.arraycopy(Bd, 0, data, 0, data.length);

        return this;
    }

    @Override
    public ComplexMatrix zero() {
        Arrays.fill(data, 0);
        return this;
    }
}
