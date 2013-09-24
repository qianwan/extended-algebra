package com.yahoo.networkmimo;

import java.util.List;

import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.yahoo.algebra.matrix.ComplexVector.Norm;

public class SimpleSingleClusterNetworkOptimizeFromBSsTest {
    @Test
    public void testIt() {
        Network network = new Network(2100);
        network.addCluster(new Cluster(0, 0));
        network.addBaseStation(new BaseStation(-2600, -1400, 4, 2, "1"));
        network.addBaseStation(new BaseStation(-1700, -1350, 4, 2, "2"));
        network.addBaseStation(new BaseStation(-1740, -1050, 4, 2, "3"));
        network.addBaseStation(new BaseStation(-1500, -1200, 4, 2, "4"));
        network.addBaseStation(new BaseStation(-1350, -1000, 4, 2, "5"));
        network.addUE(new UE(-2250, -500, 2, "1"));
        network.addUE(new UE(-1500, -650, 2, "2"));
        network.addUE(new UE(-1250, -1250, 2, "3"));
        for (int i = 0; i < 3; i++) {
            network.refresh();
            network.optimizeFromBSs();
            System.out.println("Case #" + (i + 1) + ": " + network.getSumRate());
            for (UE ue : network.getUEs()) {
                List<BaseStation> servingBSs = Lists.newArrayList();
                for (BaseStation bs : network.getBSs()) {
                    if (bs.getTxPreVector(ue).norm(Norm.Two) > 1e-3)
                        servingBSs.add(bs);
                }
                System.out.println("Serving BSs of " + ue + " are " + servingBSs);
            }
        }
    }
}
