package com.yahoo.networkmimo;

import static java.lang.Math.sqrt;

import org.testng.annotations.Test;

public class MIMOIFCWMMSETest {
    @Test
    public void testIt() {
        Network network = new Network(1000);
        int Q = 1;
        int I = 1;
        double SNRdB = 25;
        double SNR = Math.pow(10, SNRdB / 10);
        double P = SNR;
        Cluster[] clusters = new Cluster[] { new Cluster(0, 0, "1"), new Cluster(0, 2000, "2"),
                new Cluster(0, -2000, "3") };
        for (Cluster cluster : clusters) {
            cluster.generateRandomBSs(2, P, Q, 2000 / sqrt(3));
            cluster.generateRandomUEs(2, I, 2000 / sqrt(3));
            network.addCluster(cluster);
        }

        double totalSumRate = 0.0;
        double totalIterations = 0.0;
        int numCases = 100;
        for (int i = 0; i < numCases; i++) {
            network.refresh();
            network.optimizeWMMSE();
            System.out.println("Case #" + (i + 1) + ": " + network.getSumRate());
            totalSumRate += network.getSumRate();
            totalIterations += BaseStation.iteration - 1;
        }
        System.out.println("Avg sum rate: " + totalSumRate / numCases);
        System.out.println("Avg iterations: " + totalIterations / numCases);
    }
}
