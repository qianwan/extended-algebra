package com.yahoo.networkmimo;

import org.testng.annotations.Test;

import com.yahoo.algebra.matrix.ComplexMatrices;
import com.yahoo.algebra.matrix.ComplexMatrix;
import com.yahoo.algebra.matrix.ComplexVector;
import com.yahoo.algebra.matrix.ComplexVector.Norm;
import com.yahoo.algebra.matrix.DenseComplexMatrix;
import com.yahoo.algebra.matrix.DenseComplexVector;

public class BisectionTargetFunctionTest {
    @Test
    public void testIt() {
        ComplexMatrix M = new DenseComplexMatrix(4, 4);
        M.set(0, 0, new double[] { 9.466855e-02, 1.519076e-17 });
        M.set(0, 1, new double[] { -1.458888e-02, -9.537129e-03 });
        M.set(0, 2, new double[] { -1.864617e-03, 9.769442e-03 });
        M.set(0, 3, new double[] { 4.210633e-03, -2.474126e-02 });
        M.set(1, 0, new double[] { -1.458888e-02, 9.537129e-03 });
        M.set(1, 1, new double[] { 7.472233e-03, -1.252775e-20 });
        M.set(1, 2, new double[] { -7.314038e-04, -1.609631e-03 });
        M.set(1, 3, new double[] { 1.325077e-03, 9.212538e-03 });
        M.set(2, 0, new double[] { -1.864617e-03, -9.769442e-03 });
        M.set(2, 1, new double[] { -7.314038e-04, 1.609631e-03 });
        M.set(2, 2, new double[] { 2.389497e-03, -1.084356e-19 });
        M.set(2, 3, new double[] { -1.980823e-03, -2.042637e-04 });
        M.set(3, 0, new double[] { 4.210633e-03, 2.474126e-02 });
        M.set(3, 1, new double[] { 1.325077e-03, -9.212538e-03 });
        M.set(3, 2, new double[] { -1.980823e-03, 2.042637e-04 });
        M.set(3, 3, new double[] { 1.278983e-02, -4.611308e-19 });

        ComplexVector c = new DenseComplexVector(4);
        c.set(0, new double[] { -2.539555e-01, 2.534184e-01 });
        c.set(1, new double[] { 9.869303e-03, -7.542196e-02 });
        c.set(2, new double[] { 3.757818e-02, 2.239566e-02 });
        c.set(3, new double[] { -8.710739e-02, -4.788983e-02 });

        UE ue = new UE(0, 0, 0, 0.3);
        double power = 5.0;
        double theta = Math.sqrt(1.0 / power);
        theta = 1 / Math.sqrt(5);
        System.out.println(ue.bisectionTarget(-0.3 * theta / 2, theta, M, c));
    }

    public static double gradient(double miu, double theta, ComplexMatrix M, ComplexVector c) {
        ComplexMatrix B = ComplexMatrices.eye(4).scale(new double[] { 0.3 * theta / 2 + miu, 0 })
                .add(M);
        double a = -theta
                / B.inverse().mult(c, new DenseComplexVector(B.numColumns())).norm(Norm.Two);
        ComplexMatrix Binv = B.inverse();
        ComplexMatrix BinvH = Binv.hermitianTranspose(new DenseComplexMatrix(4, 4));
        ComplexMatrix middle = BinvH.mult(Binv).mult(Binv);
        double dot[] = c.dot(middle.mult(c, new DenseComplexVector(middle.numRows())));
        System.out.println(String.format("dot = [%f%+fj]", dot[0], dot[1]));
        return dot[0] * a;
    }
}
