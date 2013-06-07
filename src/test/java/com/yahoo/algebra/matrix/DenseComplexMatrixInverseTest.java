package com.yahoo.algebra.matrix;

import org.testng.annotations.Test;

public class DenseComplexMatrixInverseTest {
    @Test
    public void testIt() {
        DenseComplexMatrix B = new DenseComplexMatrix(2, 2);
        B.set(0, 0, new double[] { 2.29309, 0.12325 });
        B.set(0, 1, new double[] { 0.43952, 0.73916 });
        B.set(1, 0, new double[] { -0.82307, 1.24527 });
        B.set(1, 1, new double[] { 1.23955, -0.08308 });

        DenseComplexMatrix A = B.inverse();

        double[] data = A.getData();
        for (int i = 0; i < 2 * A.numColumns(); ++i) {
            for (int j = 0; j < 2 * A.numRows(); ++j) {
                System.out.format("%10.6f", data[i + j * 2 * A.numRows()]);
            }
            System.out.println();
        }
    }
}
