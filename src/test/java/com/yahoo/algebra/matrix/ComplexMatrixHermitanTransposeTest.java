package com.yahoo.algebra.matrix;

import junit.framework.Assert;

import org.testng.annotations.Test;

import com.yahoo.networkmimo.exception.ComplexMatrixNotSPDException;

public class ComplexMatrixHermitanTransposeTest {
    @Test
    public void testIt() throws ComplexMatrixNotSPDException {
        ComplexMatrix A = ComplexMatrices.random(new DenseComplexMatrix(3, 30));

        ComplexMatrix B = new DenseComplexMatrix(A.numColumns(), A.numRows());
        A.hermitianTranspose(B);

        ComplexMatrix C = new DenseComplexMatrix(B.numColumns(), B.numRows());
        B.hermitianTranspose(C);

        Assert.assertEquals(A, C);
    }
}
