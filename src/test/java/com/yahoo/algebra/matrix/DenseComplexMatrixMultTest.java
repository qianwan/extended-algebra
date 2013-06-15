package com.yahoo.algebra.matrix;

import org.testng.annotations.Test;

public class DenseComplexMatrixMultTest {
    @Test
    public void testIt() {
        DenseComplexMatrix A = new DenseComplexMatrix(2, 3);
        A.set(0, 0, new double[]{0.537667139546100 , -0.433592022305684});
        A.set(0, 1, new double[]{-2.25884686100365 , 3.57839693972576});
        A.set(0, 2, new double[]{0.318765239858981 , - 1.34988694015652});
        A.set(1, 0, new double[]{1.83388501459509 , 0.342624466538650});
        A.set(1, 1, new double[]{0.862173320368121 , 2.76943702988488});
        A.set(1, 2, new double[]{-1.30768829630527 , 3.03492346633185});
        DenseComplexMatrix B = new DenseComplexMatrix(3, 4);
        B.set(0, 0, new double[]{0.725404224946106 , 0.488893770311789});
        B.set(0, 1, new double[]{-0.204966058299775 , -0.303440924786016});
        B.set(0, 2, new double[]{1.40903448980048 , 0.888395631757642});
        B.set(0, 3, new double[]{-1.20748692268504 , -0.809498694424876});
        B.set(1, 0, new double[]{-0.0630548731896562 , 1.03469300991786});
        B.set(1, 1, new double[]{-0.124144348216312 , 0.293871467096658});
        B.set(1, 2, new double[]{1.41719241342961 , -1.14707010696915});
        B.set(1, 3, new double[]{0.717238651328839 , -2.94428416199490});
        B.set(2, 0, new double[]{0.714742903826096 , 0.726885133383238});
        B.set(2, 1, new double[]{1.48969760778547 , -0.787282803758638});
        B.set(2, 2, new double[]{0.671497133608081 , -1.06887045816803});
        B.set(2, 3, new double[]{1.63023528916473 , 1.43838029281510});
        DenseComplexMatrix C = new DenseComplexMatrix(2, 4);
        C = (DenseComplexMatrix) A.mult(B, C);
        System.out.println(C);
    }
}
