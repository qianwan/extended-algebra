package com.yahoo.algebra.matrix;

import org.junit.Assert;
import org.testng.annotations.Test;

import com.yahoo.algebra.matrix.ComplexVector.Norm;

public class DenseComplexVectorTest {
    @Test
    public void getSetTest() {
        ComplexVector v = new DenseComplexVector(2);
        v.set(0, new double[]{1.0, 2.0});
        v.set(1, new double[]{2.0, 1.0});
        Assert.assertTrue(v.get(0)[0]==1.0);
        Assert.assertTrue(v.get(0)[1]==2.0);
        Assert.assertTrue(v.get(1)[0]==2.0);
        Assert.assertTrue(v.get(1)[1]==1.0);

        ComplexVector x = new DenseComplexVector(2);
        x.set(new double[]{2, 0}, v);
        Assert.assertTrue(Complexes.equals(x.get(0), new double[]{2, 4}, 1e-10));
        Assert.assertTrue(Complexes.equals(x.get(1), new double[]{4, 2}, 1e-10));
        x.set(new double[]{0, 2}, v);
        Assert.assertTrue(Complexes.equals(x.get(0), new double[]{-4, 2}, 1e-10));
        Assert.assertTrue(Complexes.equals(x.get(1), new double[]{-2, 4}, 1e-10));
        x.set(new double[]{0, 0}, v);
        Assert.assertTrue(Complexes.equals(x.get(0), new double[]{0, 0}, 1e-10));
        Assert.assertTrue(Complexes.equals(x.get(1), new double[]{0, 0}, 1e-10));
        x.set(new double[]{1, 2}, v);
        Assert.assertTrue(Complexes.equals(x.get(0), new double[]{-3, 4}, 1e-10));
        Assert.assertTrue(Complexes.equals(x.get(1), new double[]{0, 5}, 1e-10));
        x.set(v);
        Assert.assertTrue(Complexes.equals(x.get(0), v.get(0), 1e-10));
        Assert.assertTrue(Complexes.equals(x.get(1), v.get(1), 1e-10));
    }

    @Test
    public void zeroTest() {
        ComplexVector v = new DenseComplexVector(2);
        Assert.assertTrue(Complexes.equals(v.get(0), new double[]{0, 0}, 1e-10));
        Assert.assertTrue(Complexes.equals(v.get(1), new double[]{0, 0}, 1e-10));
        v.zero();
        Assert.assertTrue(Complexes.equals(v.get(0), new double[]{0, 0}, 1e-10));
        Assert.assertTrue(Complexes.equals(v.get(1), new double[]{0, 0}, 1e-10));
    }

    @Test
    public void scaleTest() {
        ComplexVector v = new DenseComplexVector(2);
        v.set(0, new double[]{1.0, 2.0});
        v.set(1, new double[]{2.0, 1.0});
        v.scale(new double[]{2, 0});
        Assert.assertTrue(Complexes.equals(v.get(0), new double[]{2, 4}, 1e-10));
        Assert.assertTrue(Complexes.equals(v.get(1), new double[]{4, 2}, 1e-10));
        v.set(0, new double[]{1.0, 2.0});
        v.set(1, new double[]{2.0, 1.0});
        v.scale(new double[]{0, 2});
        Assert.assertTrue(Complexes.equals(v.get(0), new double[]{-4, 2}, 1e-10));
        Assert.assertTrue(Complexes.equals(v.get(1), new double[]{-2, 4}, 1e-10));
        v.set(0, new double[]{1.0, 2.0});
        v.set(1, new double[]{2.0, 1.0});
        v.scale(new double[]{1, 2});
        Assert.assertTrue(Complexes.equals(v.get(0), new double[]{-3, 4}, 1e-10));
        Assert.assertTrue(Complexes.equals(v.get(1), new double[]{0, 5}, 1e-10));
    }

    @Test
    public void addTest() {
        ComplexVector x = new DenseComplexVector(2);
        x.set(0, new double[]{1, -2});
        x.set(1, new double[]{3, 4});
        ComplexVector y = new DenseComplexVector(2);
        y.set(0, new double[]{5, 6});
        y.set(1, new double[]{-7, 8});
        x.add(y);
        Assert.assertTrue(Complexes.equals(x.get(0), new double[]{6, 4}, 1e-10));
        Assert.assertTrue(Complexes.equals(x.get(1), new double[]{-4, 12}, 1e-10));

        x.set(0, new double[]{1, -2});
        x.set(1, new double[]{3, 4});
        x.add(new double[]{0, 0}, y);
        Assert.assertTrue(Complexes.equals(x.get(0), new double[]{1, -2}, 1e-10));
        Assert.assertTrue(Complexes.equals(x.get(1), new double[]{3, 4}, 1e-10));

        x.set(0, new double[]{1, -2});
        x.set(1, new double[]{3, 4});
        x.add(new double[]{2, 0}, y);
        Assert.assertTrue(Complexes.equals(x.get(0), new double[]{11, 10}, 1e-10));
        Assert.assertTrue(Complexes.equals(x.get(1), new double[]{-11, 20}, 1e-10));

        x.set(0, new double[]{1, -2});
        x.set(1, new double[]{3, 4});
        x.add(new double[]{0, 2}, y);
        Assert.assertTrue(Complexes.equals(x.get(0), new double[]{-11, 8}, 1e-10));
        Assert.assertTrue(Complexes.equals(x.get(1), new double[]{-13, -10}, 1e-10));

        x.set(0, new double[]{1, -2});
        x.set(1, new double[]{3, 4});
        x.add(new double[]{1, 2}, y);
        Assert.assertTrue(Complexes.equals(x.get(0), new double[]{-6, 14}, 1e-10));
        Assert.assertTrue(Complexes.equals(x.get(1), new double[]{-20, -2}, 1e-10));
    }

    @Test
    public void dotTest() {
        ComplexVector x = new DenseComplexVector(2);
        x.set(0, new double[]{1, -2});
        x.set(1, new double[]{3, 4});
        ComplexVector y = new DenseComplexVector(2);
        y.set(0, new double[]{5, 6});
        y.set(1, new double[]{-7, 8});
        Assert.assertTrue(Complexes.equals(x.dot(y), new double[]{4, 68}, 1e-10));
    }

    @Test
    public void normTest() {
        ComplexVector x = new DenseComplexVector(2);
        x.set(0, new double[]{1, -2});
        x.set(1, new double[]{3, 4});
        Assert.assertEquals(x.norm(Norm.One), 7.236067977499790, 1e-10);
        Assert.assertEquals(x.norm(Norm.Two), 5.477225575051661, 1e-10);
    }

    @Test
    public void multTest() {
        ComplexVector x = new DenseComplexVector(2);
        x.set(0, new double[]{1, -2});
        x.set(1, new double[]{3, 4});
        ComplexVector y = new DenseComplexVector(2);
        y.set(0, new double[]{5, 6});
        y.set(1, new double[]{-7, 8});
        ComplexMatrix A = new DenseComplexMatrix(x.size(), x.size());
        x.mult(y, A);
        Assert.assertTrue(Complexes.equals(A.get(0, 0), new double[]{17, - 4}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(0, 1), new double[]{9, 22}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(1, 0), new double[]{-9, 38}, 1e-10));
        Assert.assertTrue(Complexes.equals(A.get(1, 1), new double[]{-53, -4}, 1e-10));
    }

    @Test
    public void conjugateTest() {
        ComplexVector x = new DenseComplexVector(2);
        x.set(0, new double[]{1, -2});
        x.set(1, new double[]{3, 4});
        x.conjugate();
        Assert.assertTrue(Complexes.equals(x.get(0), new double[]{1, 2}, 1e-10));
        Assert.assertTrue(Complexes.equals(x.get(1), new double[]{3, -4}, 1e-10));

        x.set(0, new double[]{1, -2});
        x.set(1, new double[]{3, 4});
        ComplexVector y = new DenseComplexVector(2);
        y.set(0, new double[]{5, 6});
        y.set(1, new double[]{-7, 8});
        x.conjugate(y);
        Assert.assertTrue(Complexes.equals(x.get(0), new double[]{1, -2}, 1e-10));
        Assert.assertTrue(Complexes.equals(x.get(1), new double[]{3, 4}, 1e-10));
        Assert.assertTrue(Complexes.equals(y.get(0), new double[]{1, 2}, 1e-10));
        Assert.assertTrue(Complexes.equals(y.get(1), new double[]{3, -4}, 1e-10));
    }
}







