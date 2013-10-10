package com.yahoo.networkmimo;

import static java.lang.Math.sqrt;

import org.testng.annotations.Test;

public class QuadrupleClusterNetworkWMMSE {
    @Test
    public void testIt() {
        Network network = new Network(1000);
        int Q = 20;
        int M = 4;
        int I = 40;
        int N = 2;
        double SNRdB = 7;
        double SNR = Math.pow(10, SNRdB / 10);
        double P = SNR / Q;
        System.out.println("P = " + P);
        Cluster[] clusters = new Cluster[] { new Cluster(0, 0), new Cluster(0, 2000),
                new Cluster(2000 * Math.cos(Math.PI / 6), 2000 * Math.sin(Math.PI / 6)),
                new Cluster(-2000 * Math.cos(Math.PI / 6), 2000 * Math.sin(Math.PI / 6)) };
        for (Cluster cluster : clusters) {
            cluster.generateRandomBSs(M, P, Q, 2000 / sqrt(3));
            cluster.generateRandomUEs(N, I, 2000 / sqrt(3));
            network.addCluster(cluster);
        }

        double totalSumRate = 0.0;
        double totalIterations = 0.0;
        int numCases = 20;
        for (int i = 0; i < numCases; i++) {
            network.refresh();
            network.optimizeWMMSE();
            network.brownianMotion(2000 / sqrt(3));
            System.out.println("Case #" + (i + 1) + ": " + network.getSumRate());
            totalSumRate += network.getSumRate();
            totalIterations += BaseStation.iteration - 1;
        }
        System.out.println("Avg sum rate: " + totalSumRate / numCases);
        System.out.println("Avg iterations: " + totalIterations / numCases);
    }
}
