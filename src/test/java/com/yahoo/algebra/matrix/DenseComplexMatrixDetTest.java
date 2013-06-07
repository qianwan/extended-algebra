package com.yahoo.algebra.matrix;

import org.testng.annotations.Test;

public class DenseComplexMatrixDetTest {
    @Test
    public void testIt() {
        DenseComplexMatrix B = new DenseComplexMatrix(2, 2);
        B.set(0, 0, new double[] { 2.29309, 0.12325 });
        B.set(0, 1, new double[] { 0.43952, 0.73916 });
        B.set(1, 0, new double[] { -0.82307, 1.24527 });
        B.set(1, 1, new double[] { 1.23955, -0.08308 });

        double sqrtDet = B.det2();
        System.out.println(sqrtDet);
    }
}
