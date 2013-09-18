package com.yahoo.algebra.matrix;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DenseComplexMatrixTest {
    @Test
    public void addTest() {
        ComplexMatrix A = new DenseComplexMatrix(2, 3);
        A.set(0, 0, new double[]{1, -2});
        A.set(0, 1, new double[]{-3, 4});
        A.set(0, 2, new double[]{5, -6});
        A.set(1, 0, new double[]{-7, 8});
        A.set(1, 1, new double[]{9, -10});
        A.set(1, 2, new double[]{-11, 12});
        ComplexMatrix B = new DenseComplexMatrix(2, 3);
        B.set(0, 0, new double[]{3, 5});
        B.set(0, 1, new double[]{-2, 1});
        B.set(0, 2, new double[]{4, -9});
        B.set(1, 0, new double[]{-7, 6});
        B.set(1, 1, new double[]{12, -10});
        B.set(1, 2, new double[]{-11, 8});
        A.add(B);
        Assert.assertTrue(Complexes.equals(A.get(0, 0), new double[]{4, 3}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(0, 1), new double[]{-5, 5}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(0, 2), new double[]{9, -15}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(1, 0), new double[]{-14, 14}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(1, 1), new double[]{21, -20}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(1, 2), new double[]{-22, 20}, 1e-10));

        A.set(0, 0, new double[]{1, -2});
        A.set(0, 1, new double[]{-3, 4});
        A.set(0, 2, new double[]{5, -6});
        A.set(1, 0, new double[]{-7, 8});
        A.set(1, 1, new double[]{9, -10});
        A.set(1, 2, new double[]{-11, 12});
        A.add(new double[]{0, 0}, B);
        Assert.assertTrue(Complexes.equals(A.get(0, 0), new double[]{1, -2}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(0, 1), new double[]{-3, 4}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(0, 2), new double[]{5, -6}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(1, 0), new double[]{-7, 8}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(1, 1), new double[]{9, -10}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(1, 2), new double[]{-11, 12}, 1e-10));

        A.set(0, 0, new double[]{1, -2});
        A.set(0, 1, new double[]{-3, 4});
        A.set(0, 2, new double[]{5, -6});
        A.set(1, 0, new double[]{-7, 8});
        A.set(1, 1, new double[]{9, -10});
        A.set(1, 2, new double[]{-11, 12});
        A.add(new double[]{2, 0}, B);
        Assert.assertTrue(Complexes.equals(A.get(0, 0), new double[]{7, 8}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(0, 1), new double[]{-7, 6}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(0, 2), new double[]{13, -24}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(1, 0), new double[]{-21, 20}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(1, 1), new double[]{33, -30}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(1, 2), new double[]{-33, 28}, 1e-10));

        A.set(0, 0, new double[]{1, -2});
        A.set(0, 1, new double[]{-3, 4});
        A.set(0, 2, new double[]{5, -6});
        A.set(1, 0, new double[]{-7, 8});
        A.set(1, 1, new double[]{9, -10});
        A.set(1, 2, new double[]{-11, 12});
        A.add(new double[]{0, -2}, B);
        Assert.assertTrue(Complexes.equals(A.get(0, 0), new double[]{11, -8}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(0, 1), new double[]{-1, 8}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(0, 2), new double[]{-13, -14}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(1, 0), new double[]{5, 22}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(1, 1), new double[]{-11, -34}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(1, 2), new double[]{5, 34}, 1e-10));

        A.set(0, 0, new double[]{1, -2});
        A.set(0, 1, new double[]{-3, 4});
        A.set(0, 2, new double[]{5, -6});
        A.set(1, 0, new double[]{-7, 8});
        A.set(1, 1, new double[]{9, -10});
        A.set(1, 2, new double[]{-11, 12});
        A.add(new double[]{1, -2}, B);
        Assert.assertTrue(Complexes.equals(A.get(0, 0), new double[]{14, -3}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(0, 1), new double[]{-3, 9}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(0, 2), new double[]{-9, -23}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(1, 0), new double[]{-2, 28}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(1, 1), new double[]{1, -44}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(1, 2), new double[]{-6, 42}, 1e-10));
    }

    @Test
    public void scaleTest() {
        ComplexMatrix A = new DenseComplexMatrix(2, 3);

        A.set(0, 0, new double[]{1, -2});
        A.set(0, 1, new double[]{-3, 4});
        A.set(0, 2, new double[]{5, -6});
        A.set(1, 0, new double[]{-7, 8});
        A.set(1, 1, new double[]{9, -10});
        A.set(1, 2, new double[]{-11, 12});
        A.scale(new double[]{0, 0});
        Assert.assertTrue(Complexes.equals(A.get(0, 0), new double[]{0, 0}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(0, 1), new double[]{0, 0}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(0, 2), new double[]{0, 0}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(1, 0), new double[]{0, 0}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(1, 1), new double[]{0, 0}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(1, 2), new double[]{0, 0}, 1e-10));

        A.set(0, 0, new double[]{1, -2});
        A.set(0, 1, new double[]{-3, 4});
        A.set(0, 2, new double[]{5, -6});
        A.set(1, 0, new double[]{-7, 8});
        A.set(1, 1, new double[]{9, -10});
        A.set(1, 2, new double[]{-11, 12});
        A.scale(new double[]{2, 0});
        Assert.assertTrue(Complexes.equals(A.get(0, 0), new double[]{2, -4}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(0, 1), new double[]{-6, 8}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(0, 2), new double[]{10, -12}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(1, 0), new double[]{-14, 16}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(1, 1), new double[]{18, -20}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(1, 2), new double[]{-22, 24}, 1e-10));

        A.set(0, 0, new double[]{1, -2});
        A.set(0, 1, new double[]{-3, 4});
        A.set(0, 2, new double[]{5, -6});
        A.set(1, 0, new double[]{-7, 8});
        A.set(1, 1, new double[]{9, -10});
        A.set(1, 2, new double[]{-11, 12});
        A.scale(new double[]{0, -1});
        Assert.assertTrue(Complexes.equals(A.get(0, 0), new double[]{-2, -1}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(0, 1), new double[]{4, 3}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(0, 2), new double[]{-6, -5}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(1, 0), new double[]{8, 7}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(1, 1), new double[]{-10, -9}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(1, 2), new double[]{12, 11}, 1e-10));

        A.set(0, 0, new double[]{1, -2});
        A.set(0, 1, new double[]{-3, 4});
        A.set(0, 2, new double[]{5, -6});
        A.set(1, 0, new double[]{-7, 8});
        A.set(1, 1, new double[]{9, -10});
        A.set(1, 2, new double[]{-11, 12});
        A.scale(new double[]{2, -1});
        Assert.assertTrue(Complexes.equals(A.get(0, 0), new double[]{0, -5}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(0, 1), new double[]{-2, 11}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(0, 2), new double[]{4, -17}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(1, 0), new double[]{-6, 23}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(1, 1), new double[]{8, -29}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(1, 2), new double[]{-10, 35}, 1e-10));
    }
}
