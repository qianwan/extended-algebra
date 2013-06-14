package com.yahoo.algebra;

import org.junit.Assert;

import no.uib.cipr.matrix.DenseMatrix;

public class ComplexDenseMatrix {
    protected DenseMatrix ltA;

    protected DenseMatrix rtA;

    protected DenseMatrix lbA;

    protected DenseMatrix rbA;

    protected DenseMatrix Ahat;

    public ComplexDenseMatrix(int numRows, int numColumns) {
        this.ltA = new DenseMatrix(numRows, numColumns);
        this.rtA = new DenseMatrix(numRows, numColumns);
        this.lbA = new DenseMatrix(numRows, numColumns);
        this.rbA = new DenseMatrix(numRows, numColumns);
        this.Ahat = new DenseMatrix(2 * numRows, 2 * numColumns);
    }

    public void set(int row, int column, double[] complexValue) {
        Assert.assertTrue(complexValue.length == 2);
        ltA.set(row, column, complexValue[0]);
        rtA.set(row, column, -complexValue[1]);
        lbA.set(row, column, complexValue[1]);
        rbA.set(row, column, complexValue[0]);
    }

    public double[] get(int row, int column) {
        return new double[] { rbA.get(row, column), lbA.get(row, column) };
    }

    public ComplexDenseMatrix add(ComplexDenseMatrix B, ComplexDenseMatrix C) {
        return null;
    }
}
