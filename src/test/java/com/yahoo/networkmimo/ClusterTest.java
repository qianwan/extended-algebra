package com.yahoo.networkmimo;

import junit.framework.Assert;

import org.testng.annotations.Test;

import com.yahoo.algebra.matrix.DenseComplexMatrix;

public class ClusterTest {
    @Test
    public void testIt() {
        Cluster cluster = new Cluster();
        Assert.assertEquals(cluster.getNumAntennas(), 0);
        Assert.assertEquals(cluster.getType(), Entity.Type.CLUSTER);
        Assert.assertEquals(cluster.getXY()[0], 0.0);
        Assert.assertEquals(cluster.getXY()[1], 0.0);
        cluster = new Cluster(1000.0, 1000.0);
        Assert.assertEquals(cluster.getXY()[0], 1000.0);
        Assert.assertEquals(cluster.getXY()[1], 1000.0);

        BaseStation bs1 = new BaseStation(801, 801, 4);
        cluster.addBaseStation(bs1);
        BaseStation bs2 = new BaseStation(1000, 1000, 2);
        cluster.addBaseStation(bs2);

        UE ue = new UE(800.0, 800.0, 4);
        Assert.assertEquals(cluster.getNumAntennas(), 6);

        DenseComplexMatrix H = cluster.getMIMOChannel(ue);
        System.out.println(H);
        System.out.println(bs1.getMIMOChannel(ue));
        System.out.println(bs2.getMIMOChannel(ue));
    }
}
