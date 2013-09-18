package com.yahoo.networkmimo;

import org.testng.annotations.Test;

public class SingleClusterNetworkTest {
    @Test
    public void singleClusterTest() {
        Network network = new Network(2100);
        network.addCluster(new Cluster(0, 0));
        network.addBaseStation(new BaseStation(-2600, -1400, 4, 2, "1"));
        network.addBaseStation(new BaseStation(-1700, -1350, 4, 2, "2"));
        network.addBaseStation(new BaseStation(-1740, -1050, 4, 2, "3"));
        network.addBaseStation(new BaseStation(-1500, -1200, 4, 2, "4"));
        network.addBaseStation(new BaseStation(-1350, -1000, 4, 2, "5"));
        double lambda = 0.3;
        network.addUE(new UE(-2250, -500, 2, lambda, "1"));
        network.addUE(new UE(-1500, -650, 2, lambda, "2"));
        network.addUE(new UE(-1250, -1250, 2, lambda, "3"));
        for (int i = 0; i < 100; i++) {
            System.out.println("Case #%d: " + (i + 1));
            network.refresh();
            network.optimize();
        }
    }
}
