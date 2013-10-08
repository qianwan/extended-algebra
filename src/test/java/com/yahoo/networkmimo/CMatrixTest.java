package com.yahoo.networkmimo;

import static java.lang.Math.sqrt;

import java.util.Iterator;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.yahoo.algebra.matrix.ComplexMatrix;
import com.yahoo.algebra.matrix.ComplexMatrix.Norm;

public class CMatrixTest {
    @Test
    public void testIt() {
        Network network = new Network(2100);
        Cluster cluster = new Cluster(0, 0);
        int Q = 20;
        int I = 40;
        double P = 0.5;
        cluster.generateRandomBSs(4, P, Q, 2000 / sqrt(3));
        cluster.generateRandomUEs(2, I, 2000 / sqrt(3));
        network.addCluster(cluster);
        network.refresh();
        Iterator<UE> iter = network.getUEs().iterator();
        UE ue = iter.next();
        ComplexMatrix c1 = ue.calculateCMatrix1();
        ComplexMatrix c2 = ue.calculateCMatrix2();
        ComplexMatrix diff = c1.add(new double[] { -1, 0 }, c2);
        Assert.assertTrue(diff.norm(Norm.One) < 1e-6);
    }
}
