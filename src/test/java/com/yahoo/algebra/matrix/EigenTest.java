package com.yahoo.algebra.matrix;

import no.uib.cipr.matrix.NotConvergedException;

import org.junit.Assert;
import org.testng.annotations.Test;

import com.yahoo.algebra.matrix.ComplexMatrix.Norm;

public class EigenTest {
    @Test
    public void testIt() throws NotConvergedException {
        ComplexMatrix A = new DenseComplexMatrix(5, 5);
        ComplexMatrix V = new DenseComplexMatrix(A.numRows(), A.numColumns());
        ComplexVector lambda = new DenseComplexVector(A.numRows());

        ComplexVector v = new DenseComplexVector(5);
        for (int i = 0; i < v.size(); i++) {
            v.set(i, new double[] { (Math.random() > 0.5 ? 1 : -1) * Math.random(),
                    (Math.random() > 0.5 ? 1 : -1) * Math.random() });
        }
        A = v.mult(v.conjugate(new DenseComplexVector(v.size())), new DenseComplexMatrix(v.size(),
                v.size()));
        ComplexMatrices.eig(A, V, lambda);
        ComplexMatrix diff = V.mult(ComplexMatrices.diag(lambda))
                .mult(V.hermitianTranspose(new DenseComplexMatrix(V.numColumns(), V.numRows())))
                .add(new double[] { -1, 0 }, A);
        Assert.assertTrue(diff.norm(Norm.One) < 1e-10);
    }
}
