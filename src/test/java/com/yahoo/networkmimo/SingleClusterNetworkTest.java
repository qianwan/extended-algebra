package com.yahoo.networkmimo;

import org.testng.annotations.Test;

public class SingleClusterNetworkTest {
    @Test
    public void sparseTest() {
        Network network = new Network(2100);
        network.addCluster(new Cluster(0, 0));
        network.addBaseStation(new BaseStation(-2600, -1400, 4, 20));
        network.addBaseStation(new BaseStation(-1700, -1350, 4, 20));
        network.addBaseStation(new BaseStation(-1740, -1050, 4, 20));
        network.addBaseStation(new BaseStation(-1500, -1200, 4, 20));
        network.addBaseStation(new BaseStation(-1350, -1000, 4, 20));
        network.addUE(new UE(-2250, -500, 2, 1));
        network.addUE(new UE(-1500, -650, 2, 1));
        network.addUE(new UE(-1250, -1250, 2, 1));
        network.refresh();
    }
}
