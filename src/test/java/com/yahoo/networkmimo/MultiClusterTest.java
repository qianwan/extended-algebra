package com.yahoo.networkmimo;

import org.testng.annotations.Test;

public class MultiClusterTest {
    @Test
    public void testIt() {
        Cluster[] network = new Cluster[10];
        network[0] = new Cluster(0, 0);
        network[1] = new Cluster(2000, 0);
        network[2] = new Cluster(-2000, 0);
        network[3] = new Cluster(-4000, 0);
        network[4] = new Cluster(-2000 * Math.sqrt(3.0) / 2, 1000);
        network[5] = new Cluster(-2000 * Math.sqrt(3.0) / 2, -1000);
        network[6] = new Cluster(-2000 * Math.sqrt(3.0) / 2, -3000);
        network[7] = new Cluster(2000 * Math.sqrt(3.0) / 2, 1000);
        network[8] = new Cluster(2000 * Math.sqrt(3.0) / 2, -1000);
        network[9] = new Cluster(2000 * Math.sqrt(3.0) / 2, -3000);
    }
}
