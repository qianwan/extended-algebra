package com.yahoo.networkmimo;

import static java.lang.Math.sqrt;

import java.util.List;

import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.yahoo.algebra.matrix.ComplexVector.Norm;

public class LargeSingleClusterNetworkPrimal {
    @Test
    public void testIt() {
        Network network = new Network(2100);
        Cluster cluster = new Cluster(0, 0, "1");
        network.addCluster(cluster);
        int K = 1;
        int Q = 20;
        int I = 40;
        double SNRdB = 1;
        double SNR = Math.pow(10, SNRdB / 10);
        double P = SNR / Q;
        System.out.println("P = " + P);
        double lambda = Q / sqrt(SNR) / I / K;
        System.out.println("Lambda = " + lambda);
        cluster.generateRandomBSs(4, P, Q, 2000 / sqrt(3));
        cluster.generateRandomUEs(2, I, 2000 / sqrt(3));

        double totalSumRate = 0.0;
        double totalIterations = 0.0;
        double totalServingBSs = 0.0;
        double numServingBSs = 0.0;
        double numCases = 10;
        network.refresh();
        for (int i = 0; i < numCases; i++) {
            network.refresh();
            network.optimizePrimalProblem();
            network.brownianMotion(2000 / sqrt(3));
            System.out.println("Case #" + (i + 1) + ": " + network.getSumRate());
            totalSumRate += network.getSumRate();
            totalIterations += (BaseStation.iteration - 1);
            numServingBSs = 0.0;
            for (UE ue : network.getUEs()) {
                List<BaseStation> servingBSs = Lists.newArrayList();
                servingBSs.clear();
                for (BaseStation bs : network.getBSs()) {
                    if (bs.getTxPreVector(ue).norm(Norm.Two) > 1e-6)
                        servingBSs.add(bs);
                }
                numServingBSs += servingBSs.size();
            }
            totalServingBSs += numServingBSs;
            System.out.println("Avg number of serving BSs " + (numServingBSs / I / K));
        }
        System.out.println("Avg sum rate: " + totalSumRate / numCases);
        System.out.println("Avg iterations: " + totalIterations / numCases);
        System.out.println("Avg serving BSs: " + totalServingBSs / I / K / numCases);
    }
}
