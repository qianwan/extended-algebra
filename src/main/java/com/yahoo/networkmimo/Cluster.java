package com.yahoo.networkmimo;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yahoo.algebra.matrix.ComplexMatrixEntry;
import com.yahoo.algebra.matrix.DenseComplexMatrix;

public class Cluster extends Entity {
    private final static Logger logger = LoggerFactory.getLogger(Cluster.class);
    private List<BaseStation> bss;
    private List<UE> ues;

    public Cluster() {
        super();
        setType(Entity.Type.CLUSTER);
        bss = new ArrayList<BaseStation>();
        ues = new ArrayList<UE>();
    }

    public Cluster(double x, double y) {
        super(x, y);
        setType(Entity.Type.CLUSTER);
        bss = new ArrayList<BaseStation>();
        ues = new ArrayList<UE>();
    }

    public void addBaseStation(BaseStation bs) {
        bss.add(bs);
        bs.setCluster(this);
        setNumAntennas(getNumAntennas() + bs.getNumAntennas());
    }

    public void addUE(UE ue) {
        ues.add(ue);
        ue.setCluster(this);
    }

    public DenseComplexMatrix getMIMOChannel(Entity e) {
        DenseComplexMatrix H = null;
        if (e instanceof UE) {
            H = new DenseComplexMatrix(e.getNumAntennas(), getNumAntennas());
            int columnOffset = 0;
            for (BaseStation bs : bss) {
                DenseComplexMatrix bsH = bs.getMIMOChannel(e);
                for (ComplexMatrixEntry entry : bsH) {
                    H.set(entry.row(), entry.column() + columnOffset, entry.get());
                }
                columnOffset += bs.getNumAntennas();
            }
        } else {
            logger.info("only downlink supported");
        }
        return H;
    }
}
