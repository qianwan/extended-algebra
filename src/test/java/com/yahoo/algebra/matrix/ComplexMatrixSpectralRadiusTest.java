package com.yahoo.algebra.matrix;

import no.uib.cipr.matrix.NotConvergedException;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ComplexMatrixSpectralRadiusTest {
    @Test
    public void testIt() throws NotConvergedException {
        ComplexMatrix A = new DenseComplexMatrix(2, 2);
        A.set(0, 0, new double[] {1, 2});
        A.set(0, 1, new double[] {3, 4});
        A.set(1, 0, new double[] {5, 6});
        A.set(1, 1, new double[] {7, 8});
        Assert.assertTrue(Math.abs(ComplexMatrices.spectralRadius(A) - 13.932638827739055) < 1e-10);
    }
}
