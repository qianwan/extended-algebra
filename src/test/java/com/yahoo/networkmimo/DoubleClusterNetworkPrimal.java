package com.yahoo.networkmimo;

import static java.lang.Math.sqrt;

import org.testng.annotations.Test;

import com.yahoo.algebra.matrix.ComplexVector;
import com.yahoo.algebra.matrix.DenseComplexVector;

public class DoubleClusterNetworkPrimal {
    @Test
    public void testIt() {
        Network network = new Network(2100);
        int Q = 20;
        int I = 40;
        double SNRdB = 5;
        double SNR = Math.pow(10, SNRdB / 10);
        double P = SNR / Q;
        Cluster[] clusters = new Cluster[] { new Cluster(0, 0), new Cluster(0, 2000) };
        for (Cluster cluster : clusters) {
            cluster.generateRandomBSs(4, P, Q, 2000 / sqrt(3));
            cluster.generateRandomUEs(2, I, 2000 / sqrt(3));
            network.addCluster(cluster);
        }

        network.brownianMotion(2000 / sqrt(3));
        ComplexVector ueAxis = new DenseComplexVector(I * clusters.length);
        int index = 0;
        for (UE ue : network.getUEs()) {
            ueAxis.set(index++, ue.getXY());
        }
        System.out.println(ueAxis);
        ComplexVector bsAxis = new DenseComplexVector(Q * clusters.length);
        index = 0;
        for (BaseStation bs : network.getBSs()) {
            bsAxis.set(index++, bs.getXY());
        }
        System.out.println(bsAxis);

        double total = 0.0;
        int numCases = 100;
        for (int i = 0; i < numCases; i++) {
            network.refresh();
            network.optimizePrimalProblem();
            network.brownianMotion(2000 / sqrt(3));
            System.out.println("Case #" + (i + 1) + ": " + network.getSumRate());
            total += network.getSumRate();
        }
        System.out.println("Avg sum rate is " + total / numCases);
    }
}
