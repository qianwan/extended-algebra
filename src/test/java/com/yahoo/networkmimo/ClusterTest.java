package com.yahoo.networkmimo;

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
    public void txPrecodingMatrixTest() throws NetworkNotReadyException, ClusterNotReadyException, ComplexMatrixNotSPDException {
        Cluster cluster = new Cluster(-1750, -1000);
        cluster.addBaseStation(new BaseStation(-2600, -1400, 4, 20, 2));
        cluster.addBaseStation(new BaseStation(-1700, -1350, 4, 20, 2));
        cluster.addBaseStation(new BaseStation(-1740, -1050, 4, 20, 2));
        cluster.addBaseStation(new BaseStation(-1500, -1200, 4, 20, 2));
        cluster.addBaseStation(new BaseStation(-1350, -1000, 4, 20, 2));
        cluster.addUE(new UE(-2250, -500, 2, 2));
        cluster.addUE(new UE(-1500, -650, 2, 2));
        cluster.addUE(new UE(-1250, -1250, 2, 2));

        for (BaseStation bs : cluster.getBSs()) {
            bs.genRandomTxPrecodingMatrix();
        }
        cluster.initTxPrecodingMatrix();
        cluster.updateTxPrecodingMatrix();
        for (UE ue : cluster.getUEs()) {
            ComplexMatrix Vik = cluster.getTxPrecodingMatrix(ue);
            int rowOffset = 0;
            for (BaseStation bs : cluster.getBSs()) {
                for (ComplexMatrixEntry e : bs.getTxPrecodingMatrix(ue)) {
                    Assert.assertEquals(e.get()[0], Vik.get(e.row() + rowOffset, e.column())[0], 0);
                    Assert.assertEquals(e.get()[1], Vik.get(e.row() + rowOffset, e.column())[1], 0);
                }
                rowOffset += bs.getNumAntennas();
            }
        }

        // TODO test

        Network network = new Network();
        network.addCluster(cluster);
        for (UE ue : cluster.getUEs()) {
            ue.calcRxPreMatrixAndRate();
        }

        for (BaseStation bs : cluster.getBSs()) {
            bs.genRandomTxPrecodingMatrix();
        }
        cluster.updateTxPrecodingMatrix();
        for (UE ue : cluster.getUEs()) {
            ComplexMatrix Vik = cluster.getTxPrecodingMatrix(ue);
            int rowOffset = 0;
            for (BaseStation bs : cluster.getBSs()) {
                for (ComplexMatrixEntry e : bs.getTxPrecodingMatrix(ue)) {
                    Assert.assertEquals(e.get()[0], Vik.get(e.row() + rowOffset, e.column())[0], 0);
                    Assert.assertEquals(e.get()[1], Vik.get(e.row() + rowOffset, e.column())[1], 0);
                }
                rowOffset += bs.getNumAntennas();
            }
        }
    }
}
