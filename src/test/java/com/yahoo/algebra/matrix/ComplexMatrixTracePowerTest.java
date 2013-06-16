package com.yahoo.algebra.matrix;

import org.junit.Assert;
import org.testng.annotations.Test;

public class ComplexMatrixTracePowerTest {
    @Test
    public void traceTest() {
        ComplexMatrix A = ComplexMatrices.random(new DenseComplexMatrix(40, 40));

        double[] trace = new double[] { 0, 0 };
        for (int i = 0; i < A.numRows(); i++) {
            trace = Complexes.add(trace, A.get(i, i));
        }
        Assert.assertTrue(Math.abs(trace[0] - A.trace()[0]) < AbstractComplexMatrix
                .getEqualThreshold());
        Assert.assertTrue(Math.abs(trace[1] - A.trace()[1]) < AbstractComplexMatrix
                .getEqualThreshold());
    }

    @Test
    public void powerTest() {
        ComplexMatrix A = ComplexMatrices.random(new DenseComplexMatrix(40, 50));

        ComplexMatrix B = A.hermitianTranspose(new DenseComplexMatrix(A.numColumns(), A.numRows()));

        ComplexMatrix C = B.mult(A, new DenseComplexMatrix(B.numRows(), A.numColumns()));

        Assert.assertTrue(C.isSquare());
        double[] trace = C.trace();
        Assert.assertTrue(trace[0] >= 0);
        Assert.assertTrue(Math.abs(trace[1]) < AbstractComplexMatrix.getEqualThreshold());

        double power = ComplexMatrices.getPower(A);
        Assert.assertTrue(Math.abs(power - trace[0]) < AbstractComplexMatrix.getEqualThreshold());

        A = new DenseComplexMatrix(2, 3);
        A.set(0, 0, new double[] { 0.537667139546100, -0.433592022305684 });
        A.set(0, 1, new double[] { -2.25884686100365, 3.57839693972576 });
        A.set(0, 2, new double[] { 0.318765239858981, -1.34988694015652 });
        A.set(1, 0, new double[] { 1.83388501459509, 0.342624466538650 });
        A.set(1, 1, new double[] { 0.862173320368121, 2.76943702988488 });
        A.set(1, 2, new double[] { -1.30768829630527, 3.03492346633185 });
        power = ComplexMatrices.getPower(A);

        Assert.assertTrue(Math.abs(power - 43.1226670219272) < AbstractComplexMatrix.getEqualThreshold());

        ComplexMatrices.setPower(A, 1.0);
        power = ComplexMatrices.getPower(A);
        Assert.assertTrue(Math.abs(power - 1.0) < AbstractComplexMatrix.getEqualThreshold());
    }
}
