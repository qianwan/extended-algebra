package com.yahoo.algebra.matrix;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.yahoo.networkmimo.exception.ComplexMatrixNotSPDException;

public class DenseComplexMatrixInverseTest {
    @Test
    public void testIt() throws ComplexMatrixNotSPDException {
        ComplexMatrix B = new DenseComplexMatrix(2, 2);
        B.set(0, 0, new double[] { 2.29309, 0.12325 });
        B.set(0, 1, new double[] { 0.43952, 0.73916 });
        B.set(1, 0, new double[] { -0.82307, 1.24527 });
        B.set(1, 1, new double[] { 1.23955, -0.08308 });

        ComplexMatrix A = B.inverse();

        ComplexMatrix C = new DenseComplexMatrix(B.numRows(), B.numColumns());
        C.set(0, 0, new double[] { 0.2996583404, -0.0217829542 });
        C.set(0, 1, new double[] { -0.1073014702, -0.1781582195 });
        C.set(1, 0, new double[] { 0.1973517574, -0.3022778054 });
        C.set(1, 1, new double[] { 0.55472699219, 0.02667849987 });

        ComplexMatrix I = ComplexMatrices.eye(B.numRows());

        Assert.assertTrue(A.mult(B, new DenseComplexMatrix(A.numRows(), A.numRows())).equals(I));
        Assert.assertTrue(B.mult(A, new DenseComplexMatrix(A.numRows(), A.numRows())).equals(I));

        B = new DenseComplexMatrix(1, 1);
        B.set(0, 0, new double[]{2, 3});
        System.out.println(B.inverse());
    }
}
