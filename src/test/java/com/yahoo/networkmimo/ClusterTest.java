package com.yahoo.networkmimo;

import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.yahoo.algebra.matrix.ComplexMatrix;
import com.yahoo.algebra.matrix.ComplexMatrixEntry;
import com.yahoo.networkmimo.exception.ClusterNotReadyException;
import com.yahoo.networkmimo.exception.ComplexMatrixNotSPDException;
import com.yahoo.networkmimo.exception.NetworkNotReadyException;

public class ClusterTest {
    @Test
    public void clusterGetMIMOChannel() {
        Cluster cluster = new Cluster();
        Assert.assertEquals(cluster.getNumAntennas(), 0);
        Assert.assertEquals(cluster.getType(), Entity.Type.CLUSTER);
        Assert.assertEquals(cluster.getXY()[0], 0.0);
        Assert.assertEquals(cluster.getXY()[1], 0.0);
        cluster = new Cluster(1000.0, 1000.0);
        Assert.assertEquals(cluster.getXY()[0], 1000.0);
        Assert.assertEquals(cluster.getXY()[1], 1000.0);

        cluster.addBaseStation(new BaseStation(801, 801, 4));
        cluster.addBaseStation(new BaseStation(1000, 1000, 2));
        cluster.addUE(new UE(800.0, 800.0, 4, 2));
        cluster.addUE(new UE(900.0, 900.0, 4, 2));

        Assert.assertEquals(cluster.getNumAntennas(), 6);

        for (UE ue : cluster.getUEs()) {
            ComplexMatrix Hik = cluster.getMIMOChannel(ue);
            int columnOffset = 0;
            for (BaseStation bs : cluster.getBSs()) {
                for (ComplexMatrixEntry entry : bs.getMIMOChannel(ue)) {
                    Assert.assertEquals(entry.get()[0],
                            Hik.get(entry.row(), entry.column() + columnOffset)[0], 0.0);
                    Assert.assertEquals(entry.get()[1],
                            Hik.get(entry.row(), entry.column() + columnOffset)[1], 0.0);
                }
                columnOffset += bs.getMIMOChannel(ue).numColumns();
            }
        }
    }

    @Test
    public void txPrecodingMatrixTest() throws NetworkNotReadyException, ClusterNotReadyException,
            ComplexMatrixNotSPDException {
        Cluster cluster = new Cluster(-1750, -1000);
        cluster.addBaseStation(new BaseStation(-2600, -1400, 4, 20));
        cluster.addBaseStation(new BaseStation(-1700, -1350, 4, 20));
        cluster.addBaseStation(new BaseStation(-1740, -1050, 4, 20));
        cluster.addBaseStation(new BaseStation(-1500, -1200, 4, 20));
        cluster.addBaseStation(new BaseStation(-1350, -1000, 4, 20));
        cluster.addUE(new UE(-2250, -500, 2, 2));
        cluster.addUE(new UE(-1500, -650, 2, 2));
        cluster.addUE(new UE(-1250, -1250, 2, 2));

        // TODO
    }

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

        Set<Cluster> closure1 = cluster1.getClosureCluster();
        Assert.assertTrue(closure1.contains(cluster1));
        Assert.assertTrue(closure1.contains(cluster2));
        Assert.assertTrue(closure1.contains(cluster3));
        Assert.assertTrue(closure1.contains(cluster4));
        Assert.assertTrue(closure1.contains(cluster5));
        Assert.assertTrue(closure1.contains(cluster6));
        Assert.assertTrue(closure1.contains(cluster7));

        Set<Cluster> closure2 = cluster2.getClosureCluster();
        Assert.assertTrue(!closure2.contains(cluster3));
        Assert.assertTrue(!closure2.contains(cluster5));
        Assert.assertTrue(!closure2.contains(cluster7));
        Assert.assertTrue(closure2.contains(cluster2));
        Assert.assertTrue(closure2.contains(cluster1));
        Assert.assertTrue(closure2.contains(cluster4));
        Assert.assertTrue(closure2.contains(cluster6));
    }
}
