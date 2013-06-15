package com.yahoo.algebra.matrix;

import junit.framework.Assert;

import org.testng.annotations.Test;

public class DenseComplexMatrixMultTest {
    @Test
    public void testIt() {
        DenseComplexMatrix A = new DenseComplexMatrix(2, 3);
        A.set(0, 0, new double[] { 0.537667139546100, -0.433592022305684 });
        A.set(0, 1, new double[] { -2.25884686100365, 3.57839693972576 });
        A.set(0, 2, new double[] { 0.318765239858981, -1.34988694015652 });
        A.set(1, 0, new double[] { 1.83388501459509, 0.342624466538650 });
        A.set(1, 1, new double[] { 0.862173320368121, 2.76943702988488 });
        A.set(1, 2, new double[] { -1.30768829630527, 3.03492346633185 });

        DenseComplexMatrix B = new DenseComplexMatrix(3, 4);
        B.set(0, 0, new double[] { 0.725404224946106, 0.488893770311789 });
        B.set(0, 1, new double[] { -0.204966058299775, -0.303440924786016 });
        B.set(0, 2, new double[] { 1.40903448980048, 0.888395631757642 });
        B.set(0, 3, new double[] { -1.20748692268504, -0.809498694424876 });
        B.set(1, 0, new double[] { -0.0630548731896562, 1.03469300991786 });
        B.set(1, 1, new double[] { -0.124144348216312, 0.293871467096658 });
        B.set(1, 2, new double[] { 1.41719241342961, -1.14707010696915 });
        B.set(1, 3, new double[] { 0.717238651328839, -2.94428416199490 });
        B.set(2, 0, new double[] { 0.714742903826096, 0.726885133383238 });
        B.set(2, 1, new double[] { 1.48969760778547, -0.787282803758638 });
        B.set(2, 2, new double[] { 0.671497133608081, -1.06887045816803 });
        B.set(2, 3, new double[] { 1.63023528916473, 1.43838029281510 });
        DenseComplexMatrix C = new DenseComplexMatrix(2, 4);
        C = (DenseComplexMatrix) A.mult(B, C);

        DenseComplexMatrix E = new DenseComplexMatrix(2, 4);
        E.set(0, 0, new double[] { -1.74905660294311, -3.34763219022162 });
        E.set(0, 1, new double[] { -1.60081772549051, -3.44420870036900 });
        E.set(0, 2, new double[] { 0.817440000808849, 6.28188377089262 });
        E.set(0, 3, new double[] { 10.3767800583188, 7.56343979423190 });
        E.set(1, 0, new double[] { -4.89778207404058, 3.08122523142959 });
        E.set(1, 1, new double[] { -0.751527436714841, 4.83349476834767 });
        E.set(1, 2, new double[] { 9.04405616862921, 8.48352882869234 });
        E.set(1, 3, new double[] { 0.338141787715813, 0.616308200600818 });

        Assert.assertEquals(E, C);
    }
}
