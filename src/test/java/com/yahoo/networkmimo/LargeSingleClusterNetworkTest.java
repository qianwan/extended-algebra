package com.yahoo.networkmimo;

import java.util.List;

import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.yahoo.algebra.matrix.ComplexVector;
import com.yahoo.algebra.matrix.ComplexVector.Norm;
import com.yahoo.algebra.matrix.DenseComplexVector;

import static java.lang.Math.*;

public class LargeSingleClusterNetworkTest {
    @Test
    public void testIt() {
        Network network = new Network(2100);
        Cluster cluster = new Cluster(0, 0);
        int Q = 4;
        int I = 8;
        int K = 1;
        double P = 1.253;
        double SNR = P * Q;
        System.out.println((double) Q * K / I / Math.sqrt(SNR));
        cluster.generateRandomBSs(4, P, Q, 2000 / sqrt(3));
        cluster.generateRandomUEs(2, I, 2000 / sqrt(3));
        network.addCluster(cluster);
        ComplexVector ueAxis = new DenseComplexVector(I);
        int index = 0;
        for (UE ue : network.getUEs()) {
            ueAxis.set(index++, ue.getXY());
        }
        System.out.println(ueAxis);
        ComplexVector bsAxis = new DenseComplexVector(Q);
        index = 0;
        for (BaseStation bs : network.getBSs()) {
            bsAxis.set(index++, bs.getXY());
        }
        System.out.println(bsAxis);
        for (int i = 0; i < 1; i++) {
            network.refresh();
            network.optimizeFromUEs();
            System.out.println("Case #" + (i + 1) + ": " + network.getSumRate());
            for (UE ue : network.getUEs()) {
                List<BaseStation> servingBSs = Lists.newArrayList();
                for (BaseStation bs : network.getBSs()) {
                    if (bs.getTxPreVector(ue).norm(Norm.Two) > 1e-3)
                        servingBSs.add(bs);
                }
                System.out.println("Serving BSs of " + ue + " are " + servingBSs);
            }
        }
    }
}
