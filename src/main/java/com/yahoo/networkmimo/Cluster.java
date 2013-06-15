package com.yahoo.networkmimo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.internal.Maps;
import com.yahoo.algebra.matrix.ComplexMatrixEntry;
import com.yahoo.algebra.matrix.DenseComplexMatrix;

public class Cluster extends Entity {
    private final static Logger logger = LoggerFactory.getLogger(Cluster.class);

    private final List<BaseStation> bss = new ArrayList<BaseStation>();

    private final List<UE> ues = new ArrayList<UE>();

    private final Map<UE, DenseComplexMatrix> txPrecodingMatrix = Maps.newHashMap();

    public Cluster() {
        super();
        setType(Entity.Type.CLUSTER);
    }

    public Cluster(double x, double y) {
        super(x, y);
        setType(Entity.Type.CLUSTER);
    }

    public void addBaseStation(BaseStation bs) {
        getBSs().add(bs);
        bs.setCluster(this);
        setNumAntennas(getNumAntennas() + bs.getNumAntennas());
    }

    public void addUE(UE ue) {
        ues.add(ue);
        ue.setCluster(this);
    }

    @Override
    public DenseComplexMatrix getMIMOChannel(Entity e) {
        DenseComplexMatrix H = null;
        if (e instanceof UE) {
            H = new DenseComplexMatrix(e.getNumAntennas(), getNumAntennas());
            int columnOffset = 0;
            for (BaseStation bs : getBSs()) {
                DenseComplexMatrix bsH = bs.getMIMOChannel(e);
                for (ComplexMatrixEntry entry : bsH) {
                    H.set(entry.row(), entry.column() + columnOffset, entry.get());
                }
                columnOffset += bs.getNumAntennas();
            }
            setMIMOChannel(e, H);
        } else {
            logger.info("only downlink supported");
        }
        return H;
    }

    public void initTxPrecodingMatrix() {
        for (UE ue : ues) {
            if (txPrecodingMatrix.get(ue) == null) {
                txPrecodingMatrix.put(ue,
                        new DenseComplexMatrix(getNumAntennas(), ue.getNumStreams()));
            }
        }
    }

    public void updateTxPrecodingMatrix() {
        for (UE ue : ues) {
            DenseComplexMatrix vik = txPrecodingMatrix.get(ue);
            int rowOffset = 0;
            for (BaseStation bs : getBSs()) {
                DenseComplexMatrix vikq = bs.getTxPrecodingMatrix(ue);
                for (ComplexMatrixEntry entry : vikq) {
                    vik.set(entry.row() + rowOffset, entry.column(), entry.get());
                }
                rowOffset += bs.getNumAntennas();
            }
        }
    }

    /**
     * @return the bss
     */
    public List<BaseStation> getBSs() {
        return bss;
    }
}
