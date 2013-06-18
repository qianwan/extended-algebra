package com.yahoo.networkmimo;

import org.testng.annotations.Test;

public class NetworkTest {
    @Test
    public void testIt() {
        Network network = new Network();

        network.addCluster(new Cluster(-1750, -1000));
        network.getClusters().get(0).addBaseStation(new BaseStation(-2600, -1400, 4, 100, 2))
                .addBaseStation(new BaseStation(-1700, -1350, 4, 100, 2))
                .addBaseStation(new BaseStation(-1740, -1050, 4, 100, 2))
                .addBaseStation(new BaseStation(-1500, -1200, 4, 100, 2))
                .addBaseStation(new BaseStation(-1350, -1000, 4, 100, 2))
                .addUE(new UE(-2250, -500, 2, 1))
                .addUE(new UE(-1500, -650, 2, 1))
                .addUE(new UE(-1250, -1250, 2, 1));

        network.alloc();
        network.init();
        long tic = System.currentTimeMillis();
        for (int i = 0; i < 2; i++) {
            for (UE ue : network.getUEs()) {
                ue.calcRxPreMatrixAndRate();
            }
            for (UE ue : network.getUEs()) {
                ue.updateMMSEWeigh();
            }
            for (Cluster cluster : network.getClusters()) {
                cluster.calcJMatrix();
                for (UE ue : cluster.getUEs()) {
                    ue.calcDMatrix();
                }
                for (int j = 0; j < 10; j++) {
                    for (BaseStation bs : cluster.getBSs()) {
                        
                    }
                }
            }
        }
        long toc = System.currentTimeMillis();
        System.out.println(toc - tic);
    }
}
