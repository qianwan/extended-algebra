package com.yahoo.networkmimo;

import org.testng.Assert;
import org.testng.annotations.Test;

public class NetworkTest {
    @Test
    public void testIt() {
        Network network = new Network();

        initNetwork(network);

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

    @Test
    public void networkOrganizationTest() {
        Network network = new Network();
        initNetwork(network);

        int expected = 0;
        for (Cluster cluster : network.getClusters()) {
            expected += cluster.getBSs().size();
        }
        Assert.assertTrue(network.getBSs().size() == expected);

        expected = 0;
        for (Cluster cluster : network.getClusters()) {
            expected += cluster.getUEs().size();
        }
        Assert.assertTrue(network.getUEs().size() == expected);

        for (Cluster cluster : network.getClusters()) {
            Assert.assertTrue(network.getBSs().containsAll(cluster.getBSs()));
            Assert.assertTrue(network.getUEs().containsAll(cluster.getUEs()));
        }
    }

    public void initNetwork(Network network) {
        network.addCluster(new Cluster(0, 0));
        network.addCluster(new Cluster(0, 2000));
        network.addCluster(new Cluster(0, -2000));
        for (Cluster cluster : network.getClusters()) {
            for (int i = 0; i < 4; i++) {
                cluster.addBaseStation(new BaseStation(cluster.getXY()[0] + (0.5 - Math.random()) * 2000, cluster
                        .getXY()[1] + (0.5 - Math.random()) * 2000, 4, 100, 2));
            }
            for (int i = 0; i < 4; i++) {
                cluster.addUE(new UE(cluster.getXY()[0] + (0.5 - Math.random()) * 2000, cluster.getXY()[1]
                        + (0.5 - Math.random()) * 2000, 2, 1));
            }
        }
    }
}
