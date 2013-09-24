package com.yahoo.networkmimo;

import static java.lang.Math.sqrt;

import java.util.List;
import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.yahoo.algebra.matrix.ComplexVector.Norm;

public class LargeSingleClusterNetworkPrimal {
    @Test
    public void testIt() {
        Network network = new Network(2100);
        Cluster cluster = new Cluster(0, 0);
        int Q = 20;
        int I = 40;
        double SNRdB = 5;
        double SNR = Math.pow(10, SNRdB / 10);
        double P = SNR / Q;
        System.out.println("P = " + P);
        cluster.generateRandomBSs(4, P, Q, 2000 / sqrt(3));
        cluster.generateRandomUEs(2, I, 2000 / sqrt(3));
        network.addCluster(cluster);

        double total = 0.0;
        int numCases = 100;
        for (int i = 0; i < numCases; i++) {
            network.refresh();
            network.optimizePrimalProblem();
            network.brownianMotion(2000 / sqrt(3));
            System.out.println("Case #" + (i + 1) + ": " + network.getSumRate());
            total += network.getSumRate();
            int numServedUEs = 0;
            Set<BaseStation> serverBSs = Sets.newHashSet();
            for (UE ue : network.getUEs()) {
                List<BaseStation> servingBSs = Lists.newArrayList();
                for (BaseStation bs : network.getBSs()) {
                    if (bs.getTxPreVector(ue).norm(Norm.Two) > 1e-6)
                        servingBSs.add(bs);
                }
                if (!servingBSs.isEmpty()) {
                    numServedUEs++;
                    serverBSs.addAll(servingBSs);
                }
            }
            System.out.println("Avg serving BS is " + ((double) serverBSs.size() / numServedUEs));
        }
        System.out.println("Avg sum rate is " + total / numCases);
    }
}
