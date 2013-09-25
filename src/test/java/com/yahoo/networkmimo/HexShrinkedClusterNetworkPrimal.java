package com.yahoo.networkmimo;

import static java.lang.Math.sqrt;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.PI;

import org.testng.annotations.Test;

public class HexShrinkedClusterNetworkPrimal {
    @Test
    public void testIt() {
        Network network = new Network(1100);
        int Q = 5;
        int I = 10;
        double SNRdB = 0;
        double SNR = Math.pow(10, SNRdB / 10) / 4;
        double P = SNR / Q;
        System.out.println("P = " + P);
        double[][] coordinates = { { 0, 0 }, { 0, 1000 }, { 0, 2000 }, { 0, -1000 },
                { 1000 * cos(PI / 6), 1000 * sin(PI / 6) },
                { 1000 * cos(PI / 6), 1000 * sin(PI / 6) + 1000 },
                { 1000 * cos(PI / 6), 1000 * sin(PI / 6) - 1000 },
                { 1000 * cos(PI / 6), 1000 * sin(PI / 6) - 2000 },
                { -1000 * cos(PI / 6), 1000 * sin(PI / 6) },
                { -1000 * cos(PI / 6), 1000 * sin(PI / 6) + 1000 },
                { -1000 * cos(PI / 6), 1000 * sin(PI / 6) - 1000 },
                { -1000 * cos(PI / 6), 1000 * sin(PI / 6) - 2000 }, { 1000 * sqrt(3), 0 },
                { 1000 * sqrt(3), -1000 }, { -1000 * sqrt(3), 0 }, { -1000 * sqrt(3), -1000 } };
        Cluster[] clusters = new Cluster[coordinates.length];
        for (int i = 0; i < clusters.length; i++) {
            clusters[i] = new Cluster(coordinates[i][0], coordinates[i][1]);
        }
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
