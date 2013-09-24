package com.yahoo.networkmimo;

import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ClusterTest {
    @Test
    public void closureTest() {
        Network network = new Network(1.1);
        double sqrt3D2 = Math.sqrt(3) / 2;
        Cluster cluster1 = new Cluster(0, 0);
        Cluster cluster2 = new Cluster(0, 1);
        Cluster cluster3 = new Cluster(0, -1);
        Cluster cluster4 = new Cluster(sqrt3D2, 0.5);
        Cluster cluster5 = new Cluster(sqrt3D2, -0.5);
        Cluster cluster6 = new Cluster(-sqrt3D2, 0.5);
        Cluster cluster7 = new Cluster(-sqrt3D2, -0.5);
        network.addCluster(cluster1).addCluster(cluster2).addCluster(cluster3).addCluster(cluster4)
                .addCluster(cluster5).addCluster(cluster6).addCluster(cluster7);

        Set<Cluster> closure1 = cluster1.getClusterClosure();
        Assert.assertTrue(closure1.contains(cluster1));
        Assert.assertTrue(closure1.contains(cluster2));
        Assert.assertTrue(closure1.contains(cluster3));
        Assert.assertTrue(closure1.contains(cluster4));
        Assert.assertTrue(closure1.contains(cluster5));
        Assert.assertTrue(closure1.contains(cluster6));
        Assert.assertTrue(closure1.contains(cluster7));

        Set<Cluster> closure2 = cluster2.getClusterClosure();
        Assert.assertTrue(!closure2.contains(cluster3));
        Assert.assertTrue(!closure2.contains(cluster5));
        Assert.assertTrue(!closure2.contains(cluster7));
        Assert.assertTrue(closure2.contains(cluster2));
        Assert.assertTrue(closure2.contains(cluster1));
        Assert.assertTrue(closure2.contains(cluster4));
        Assert.assertTrue(closure2.contains(cluster6));
    }

    @Test
    public void randomBSandUETest() {
        Network network = new Network(2100);
        Cluster cluster = new Cluster(0, 0);
        cluster.generateRandomBSs(4, 2, 5, 1000);
        cluster.generateRandomUEs(2, 3, 1000);
        network.addCluster(cluster);
        System.out.println(network.getBSs());
        System.out.println(network.getUEs());
        System.out.println(cluster.getBSs());
        System.out.println(cluster.getUEs());
    }
}
