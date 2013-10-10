package com.yahoo.networkmimo;

import static java.lang.Math.sqrt;

import org.testng.annotations.Test;

public class MIMOIFCWMMSETest {
    /**
     * K = 3, T = R = 2
     */
    @Test
    public void testIt() {
        Network network = new Network(1000);
        int Q = 1;
        int I = 1;
        double SNRdB = 20;
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
        int numCases = 1000;
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

    /**
     * K = 10, T = 3, R = 2
     */
    //@Test
    public void testIt2() {
        Network network = new Network(1000);
        int Q = 1;
        int I = 1;
        double SNRdB = 30;
        double SNR = Math.pow(10, SNRdB / 10);
        double P = SNR;
        Cluster[] clusters = new Cluster[] { new Cluster(0, 0, "1"), new Cluster(0, 2000, "2"),
                new Cluster(0, -2000, "3"), new Cluster(0, 4000, "4"), new Cluster(0, -4000, "5"),
                new Cluster(0, 6000, "6"), new Cluster(0, -6000, "7"), new Cluster(0, 8000, "8"),
                new Cluster(0, -8000, "9"), new Cluster(0, 10000, "10")};
        for (Cluster cluster : clusters) {
            cluster.generateRandomBSs(3, P, Q, 2000 / sqrt(3));
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
