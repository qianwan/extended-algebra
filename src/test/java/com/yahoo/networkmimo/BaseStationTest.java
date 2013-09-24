package com.yahoo.networkmimo;

import junit.framework.Assert;

import org.testng.annotations.Test;

import com.yahoo.algebra.matrix.DenseComplexMatrix;

public class BaseStationTest {
    @Test
    public void testIt() {
        BaseStation bs = new BaseStation(0.5, 1.4, 4);
        Assert.assertEquals(bs.getXY()[0], 0.5);
        Assert.assertEquals(bs.getXY()[1], 1.4);
        Assert.assertEquals(bs.getNumAntennas(), 4);
        Assert.assertEquals(bs.getType(), Entity.Type.BS);
        bs.setXY(1.2, 2.3);
        bs.setNumAntennas(8);
        Assert.assertEquals(bs.getXY()[0], 1.2);
        Assert.assertEquals(bs.getXY()[1], 2.3);
        Assert.assertEquals(bs.getNumAntennas(), 8);
        bs.setXY(0.0, 0.0);
        bs.setNumAntennas(4);
        Assert.assertEquals(bs.getXY()[0], 0.0);
        Assert.assertEquals(bs.getXY()[1], 0.0);
        Assert.assertEquals(bs.getNumAntennas(), 4);

        UE ue1 = new UE(500, 500, 4);
        DenseComplexMatrix H1 = new DenseComplexMatrix(bs.getMIMOChannel(ue1));
        Assert.assertTrue(H1.equals(bs.getMIMOChannel(ue1)));
    }
}
