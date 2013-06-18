package com.yahoo.networkmimo;

import org.testng.annotations.Test;

public class NetworkTest {
    @Test
    public void testIt() {
        Cluster cluster = new Cluster(-1750, -1000);
        cluster.addBaseStation(new BaseStation(-2600, -1400, 4, 20, 2));
        cluster.addBaseStation(new BaseStation(-1700, -1350, 4, 20, 2));
        cluster.addBaseStation(new BaseStation(-1740, -1050, 4, 20, 2));
        cluster.addBaseStation(new BaseStation(-1500, -1200, 4, 20, 2));
        cluster.addBaseStation(new BaseStation(-1350, -1000, 4, 20, 2));
        cluster.addUE(new UE(-2250, -500, 2, 2));
        cluster.addUE(new UE(-1500, -650, 2, 2));
        cluster.addUE(new UE(-1250, -1250, 2, 2));

        Network network = new Network();
        network.addCluster(cluster);

        network.alloc();
    }
}
