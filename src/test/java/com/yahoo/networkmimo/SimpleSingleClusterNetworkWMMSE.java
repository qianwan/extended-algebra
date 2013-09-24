package com.yahoo.networkmimo;

import org.testng.annotations.Test;

/**
 * Q = 5, I = 3, SNR = 10dB, single cluster, avg sum capacity is 11.776029675918394 over 1000 cases
 * Q = 5, I = 3, SNR =  5dB, single cluster, avg sum capacity is  8.52284548164977  over 1000 cases
 * @author qianwan
 *
 */

public class SimpleSingleClusterNetworkWMMSE {
    @Test
    public void testIt() {
        Network network = new Network(2100);
        network.addCluster(new Cluster(0, 0));
        network.addBaseStation(new BaseStation(-2600, -1400, 4, 0.6324644680, "1"));
        network.addBaseStation(new BaseStation(-1700, -1350, 4, 0.6324644680, "2"));
        network.addBaseStation(new BaseStation(-1740, -1050, 4, 0.6324644680, "3"));
        network.addBaseStation(new BaseStation(-1500, -1200, 4, 0.6324644680, "4"));
        network.addBaseStation(new BaseStation(-1350, -1000, 4, 0.6324644680, "5"));
        network.addUE(new UE(-2250, -500, 2, "1"));
        network.addUE(new UE(-1500, -650, 2, "2"));
        network.addUE(new UE(-1250, -1250, 2, "3"));
        network.brownianMotion(2000 / Math.sqrt(3));

        double total = 0.0;
        int numCases = 1000;
        for (int i = 0; i < numCases; i++) {
            network.refresh();
            network.optimizeWMMSE();
            network.brownianMotion(2000 / Math.sqrt(3));
            System.out.println("Case #" + (i + 1) + ": " + network.getSumRate());
            total += network.getSumRate();
        }
        System.out.println("Avg sum rate is " + total / numCases);
    }
}
