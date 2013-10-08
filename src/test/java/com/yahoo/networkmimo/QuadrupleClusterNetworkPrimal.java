package com.yahoo.networkmimo;

import static java.lang.Math.sqrt;

import org.testng.annotations.Test;

public class QuadrupleClusterNetworkPrimal {
    @Test
    public void testIt() {
        Network network = new Network(1100);
        int Q = 5;
        int I = 10;
        double SNRdB = 0;
        double SNR = Math.pow(10, SNRdB / 10) / 4;
        double P = SNR / Q;
        System.out.println("P = " + P);
        Cluster[] clusters = new Cluster[] { new Cluster(0, 0), new Cluster(0, 1000),
                new Cluster(1000 * Math.cos(Math.PI / 6), 1000 * Math.sin(Math.PI / 6)),
                new Cluster(-1000 * Math.cos(Math.PI / 6), 1000 * Math.sin(Math.PI / 6)) };
        for (Cluster cluster : clusters) {
            cluster.generateRandomBSs(4, P, Q, 1000 / sqrt(3));
            cluster.generateRandomUEs(2, I, 1000 / sqrt(3));
            network.addCluster(cluster);
        }

        network.brownianMotion(1000 / sqrt(3));

        double total = 0.0;
        int numCases = 100;
        for (int i = 0; i < numCases; i++) {
            network.refresh();
            network.optimizePrimalProblem();
            network.brownianMotion(1000 / sqrt(3));
            System.out.println("Case #" + (i + 1) + ": " + network.getSumRate());
            total += network.getSumRate();
        }
        System.out.println("Avg sum rate is " + total / numCases);
    }
}
