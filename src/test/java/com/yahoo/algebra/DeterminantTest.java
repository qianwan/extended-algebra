package com.yahoo.algebra;

import org.junit.Assert;
import org.testng.annotations.Test;

import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.Matrix;

public final class DeterminantTest {
    @Test
    public void testIt() {
        for (int j = 0; j < 10; j++) {
            Matrix A = Matrices.random(3, 3);
            double det = 0.0;
            det += A.get(0, 0) * A.get(1, 1) * A.get(2, 2);
            det += A.get(0, 1) * A.get(1, 2) * A.get(2, 0);
            det += A.get(1, 0) * A.get(2, 1) * A.get(0, 2);
            det -= A.get(0, 2) * A.get(1, 1) * A.get(2, 0);
            det -= A.get(0, 1) * A.get(1, 0) * A.get(2, 2);
            det -= A.get(1, 2) * A.get(2, 1) * A.get(0, 0);
            Assert.assertTrue((Determinant.det(A) - det) < 1e-6);
        }
    }
}