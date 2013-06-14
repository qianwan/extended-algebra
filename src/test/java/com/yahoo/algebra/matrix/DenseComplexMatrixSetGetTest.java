package com.yahoo.algebra.matrix;

import org.testng.annotations.Test;

public class DenseComplexMatrixSetGetTest {
    @Test
    public void testIt() {
        DenseComplexMatrix A = new DenseComplexMatrix(2, 2);
        A.set(0, 0, new double[] { 2.29309, 0.12325 });
        A.set(0, 1, new double[] { 0.43952, 0.73916 });
        A.set(1, 0, new double[] { -0.82307, 1.24527 });
        A.set(1, 1, new double[] { 1.23955, -0.08308 });

        double[] data = A.getData();
        for (int i = 0; i < 2 * A.numColumns(); ++i) {
            for (int j = 0; j < 2 * A.numRows(); ++j) {
                System.out.format("%9.4f", data[i + j * 2 * A.numRows()]);
            }
            System.out.println();
        }
    }
}
