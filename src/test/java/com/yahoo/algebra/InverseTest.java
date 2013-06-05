package com.yahoo.algebra;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.Matrix;

import org.testng.annotations.Test;

public class InverseTest {
    @Test
    public void testIt() {
        DenseMatrix A = new DenseMatrix(Matrices.random(4, 4));
        Matrix B = Inverse.inv(A);
        Matrix C = Matrices.random(4, 4);
        C = A.mult(B, C);
        for (int i=0; i<C.numRows(); i++) {
            for (int j=0; j<C.numColumns(); j++) {
                System.out.printf("%8.2f", C.get(i, j));
            }
            System.out.println();
        }
    }
}
